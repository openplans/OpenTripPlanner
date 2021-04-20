package org.opentripplanner.common;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.opentripplanner.common.geometry.Subgraph;
import org.opentripplanner.graph_builder.DataImportIssueStore;
import org.opentripplanner.graph_builder.issues.GraphConnectivity;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.edgetype.ElevatorEdge;
import org.opentripplanner.routing.edgetype.FreeEdge;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTransitStopLink;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.edgetype.StreetTransitEntranceLink;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class StreetUtils {

    private static Logger LOG = LoggerFactory.getLogger(StreetUtils.class);
    private static int islandCounter = 0;

    /* Island pruning strategy:
       - First extract islands without considering noThruTraffic edges
       - Then extract expanded islands by accepting noThruTraffic edges, but - DO NOT JUMP ACROSS ORIGINAL ISLANDS!
         Note: these expanded islands can overlap.
       - Generate also pure noThruTraffic islands
       - Prune small islands away
     */

    public static void pruneFloatingIslands(Graph graph, int maxIslandSize,
            int islandWithStopMaxSize, String islandLogName, DataImportIssueStore issueStore) {
        LOG.debug("pruning");
        PrintWriter islandLog = null;
        if (islandLogName != null && !islandLogName.isEmpty()) {
            try {
                islandLog = new PrintWriter(new File(islandLogName));
            } catch (Exception e) {
                LOG.error("Failed to write islands log file", e);
            }
        }
        if (islandLog != null) {
            islandLog.printf("%s\t%s\t%s\t%s\t%s\n","id","stopCount", "streetCount","wkt" ,"hadRemoved");
        }
        Map<Vertex, Subgraph> subgraphs = new HashMap<Vertex, Subgraph>();
        Map<Vertex, Subgraph> extgraphs = new HashMap<Vertex, Subgraph>();
        Map<Vertex, ArrayList<Vertex>> neighborsForVertex = new HashMap<Vertex, ArrayList<Vertex>>();

        // Establish vertex neighbourhood without thruTrafficEdges
        collectNeighbourVertices(graph, neighborsForVertex, false);

        ArrayList<Subgraph> islands = new ArrayList<Subgraph>();
        int graphCount;

        /* associate each connected node with a subgraph */
        graphCount = collectSubGraphs(graph, neighborsForVertex, subgraphs, null, null);
        LOG.info("Islands without noThruTraffic: " + graphCount);

        // Expand vertex neighbourhood with noThruTrafficEdges
        // Note that we can reuse the original neighbour map here
        // and simply process a smaller set of noThruTrafficEdges
        collectNeighbourVertices(graph, neighborsForVertex, true);

        /* Recompute expanded subgraphs by accepting noThruTraffic edges in graph expansion.
           However, expansion is not allowed to jump from an original island to another one
         */
        graphCount = collectSubGraphs(graph, neighborsForVertex, extgraphs, subgraphs, islands);
        LOG.info("Extended island count: " + graphCount);

        /* Final round: generate noThruTraffic islands if such ones exist */
        graphCount = collectSubGraphs(graph, neighborsForVertex, extgraphs, null, islands);
        LOG.info("noThruTraffic island count: " + graphCount);

        LOG.info("Total " + islands.size() + " sub graphs found");

        /* remove all tiny subgraphs and large subgraphs without stops */
        for (Subgraph island : islands) {
            boolean hadRemoved = false;
            if(island.stopSize() > 0){
            //for islands with stops
                if (island.streetSize() < islandWithStopMaxSize) {
                    depedestrianizeOrRemove(graph, island, issueStore);
                    hadRemoved = true;
                }
            }else{
            //for islands without stops
                if (island.streetSize() < maxIslandSize) {
                    depedestrianizeOrRemove(graph, island, issueStore);
                    hadRemoved = true;
                }
            }
            if (islandLog != null) {
                WriteNodesInSubGraph(island, islandLog, hadRemoved);
            }
        }
        if (graph.removeEdgelessVertices() > 0) {
            LOG.warn("Removed edgeless vertices after pruning islands");
        }
    }

    private static void collectNeighbourVertices(
        Graph graph, Map<Vertex, ArrayList<Vertex>> neighborsForVertex, boolean noThruTraffic) {

        // RoutingRequest options = new RoutingRequest(new TraverseModeSet(TraverseMode.WALK, TraverseMode.TRANSIT));
        RoutingRequest options = new RoutingRequest(new TraverseModeSet(TraverseMode.WALK));

        for (Vertex gv : graph.getVertices()) {
            if (!(gv instanceof StreetVertex)) {
                continue;
            }
            State s0 = new State(gv, options);
            for (Edge e : gv.getOutgoing()) {
                Vertex in = gv;
                if (!(e instanceof StreetEdge || e instanceof StreetTransitStopLink ||
                    e instanceof StreetTransitEntranceLink || e instanceof ElevatorEdge ||
                    e instanceof FreeEdge)
                ) {
                    continue;
                }
                if ((e instanceof StreetEdge && ((StreetEdge)e).isNoThruTraffic()) != noThruTraffic) {
                    continue;
                }
                State s1 = e.traverse(s0);
                if (s1 == null) {
                    continue;
                }
                Vertex out = s1.getVertex();

                ArrayList<Vertex> vertexList = neighborsForVertex.get(in);
                if (vertexList == null) {
                    vertexList = new ArrayList<Vertex>();
                    neighborsForVertex.put(in, vertexList);
                }
                vertexList.add(out);

                vertexList = neighborsForVertex.get(out);
                if (vertexList == null) {
                    vertexList = new ArrayList<Vertex>();
                    neighborsForVertex.put(out, vertexList);
                }
                vertexList.add(in);
            }
        }
    }

    private static int collectSubGraphs(
        Graph graph,
        Map<Vertex, ArrayList<Vertex>> neighborsForVertex,
        Map<Vertex, Subgraph> newgraphs, // put new subgraphs here
        Map<Vertex, Subgraph> subgraphs, // optional isolation map from a previous round
        ArrayList<Subgraph> islands) {   // final list of islands or null

        int count=0;
        for (Vertex gv : graph.getVertices()) {
            if (!(gv instanceof StreetVertex)) {
                continue;
            }
            Vertex vertex = gv;

            if (subgraphs != null && !subgraphs.containsKey(vertex)) {
                // do not start new graph generation from non-classified vertex
                continue;
            }
            if (newgraphs.containsKey(vertex)) { // already processed
                continue;
            }
            if (!neighborsForVertex.containsKey(vertex)) {
                continue;
            }
            Subgraph subgraph = computeConnectedSubgraph(neighborsForVertex, vertex, subgraphs);
            if (subgraph != null){
                for (Iterator<Vertex> vIter = subgraph.streetIterator(); vIter.hasNext();) {
                    Vertex subnode = vIter.next();
                    newgraphs.put(subnode, subgraph);
                }
                if (islands != null) {
                    islands.add(subgraph);
                }
                count++;
            }
        }
        return count;
    }

    private static void depedestrianizeOrRemove(
            Graph graph,
            Subgraph island,
            DataImportIssueStore issueStore
    ) {
        //iterate over the street vertex of the subgraph
        for (Iterator<Vertex> vIter = island.streetIterator(); vIter.hasNext();) {
            Vertex v = vIter.next();
            Collection<Edge> outgoing = new ArrayList<Edge>(v.getOutgoing());
            for (Edge e : outgoing) {
                if (e instanceof StreetEdge) {
                    StreetEdge pse = (StreetEdge) e;
                    StreetTraversalPermission permission = pse.getPermission();
                    permission = permission.remove(StreetTraversalPermission.PEDESTRIAN);
                    permission = permission.remove(StreetTraversalPermission.BICYCLE);
                    if (permission == StreetTraversalPermission.NONE) {
                        graph.removeEdge(pse);
                    } else {
                        pse.setPermission(permission);
                    }
                }
            }
        }

        for (Iterator<Vertex> vIter = island.streetIterator(); vIter.hasNext();) {
            Vertex v = vIter.next();
            if (v.getDegreeOut() + v.getDegreeIn() == 0) {
                graph.remove(v);
            }
        }
        //remove street conncetion form
        for (Iterator<Vertex> vIter = island.stopIterator(); vIter.hasNext();) {
            Vertex v = vIter.next();
            Collection<Edge> edges = new ArrayList<Edge>(v.getOutgoing());
            edges.addAll(v.getIncoming());
            for (Edge e : edges) {
                if (e instanceof StreetTransitStopLink || e instanceof StreetTransitEntranceLink) {
                    graph.removeEdge(e);
                }
            }
        }
        issueStore.add(new GraphConnectivity(island.getRepresentativeVertex(), island.streetSize()));
    }

    private static Subgraph computeConnectedSubgraph(
        Map<Vertex, ArrayList<Vertex>> neighborsForVertex, Vertex startVertex, Map<Vertex, Subgraph> isolated) {
        Subgraph subgraph = new Subgraph();
        Queue<Vertex> q = new LinkedList<Vertex>();
        Subgraph anchor = null;

        if (isolated != null) {
            // anchor subgraph expansion to this subgraph
            anchor = isolated.get(startVertex);
        }
        q.add(startVertex);
        while (!q.isEmpty()) {
            Vertex vertex = q.poll();
            for (Vertex neighbor : neighborsForVertex.get(vertex)) {
                if (!subgraph.contains(neighbor)) {
                    if (anchor != null) {
                        Subgraph compare = isolated.get(neighbor);
                        if ( compare != null && compare != anchor) { // do not enter a new island
                            continue;
                        }
                    }
                    subgraph.addVertex(neighbor);
                    q.add(neighbor);
                }
            }
        }
        return subgraph;
//        if(subgraph.size()>1) return subgraph;
//        return null;
    }

    private static void WriteNodesInSubGraph(Subgraph subgraph, PrintWriter islandLog, boolean hadRemoved){
        Geometry convexHullGeom = subgraph.getConvexHull();
        if (convexHullGeom != null && !(convexHullGeom instanceof Polygon)) {
            convexHullGeom = convexHullGeom.buffer(0.0001,5);
        }
        islandLog.printf("%d\t%d\t%d\t%s\t%b\n", islandCounter, subgraph.stopSize(), 
                subgraph.streetSize(), convexHullGeom, hadRemoved);
        islandCounter++;
    }
}
