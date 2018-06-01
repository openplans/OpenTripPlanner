package org.opentripplanner.graph_builder.module;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.*;
import org.mapdb.Fun;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.opentripplanner.graph_builder.linking.SimpleStreetSplitter;
import org.opentripplanner.graph_builder.model.GtfsBundle;
import org.opentripplanner.graph_builder.module.osm.DefaultWayPropertySetSource;
import org.opentripplanner.graph_builder.module.osm.OpenStreetMapModule;
import org.opentripplanner.openstreetmap.impl.AnyFileBasedOpenStreetMapProviderImpl;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.TransitStop;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Get fake graphs. TODO clarify what a "fake graph" is and why we are getting them (apparently for testing).
 * This seems to be the only place in OTP that we use GTFS-lib, which just happens to be pulled in transitively from
 * R5. It's questionable whether we should be manually building up GTFS in code like this. I guess it would be a good
 * idea if there were a few more abstractions in use.
 */
public class FakeGraph {

    /** Frequency of trips generated by addTransit, seconds */
    public static final int FREQUENCY = 600;

    /** Travel time between stops of trips generated by addTransit, seconds */
    public static final int TRAVEL_TIME = 500;

    /** Build a graph in Columbus, OH with no transit */
    public static Graph buildGraphNoTransit () throws UnsupportedEncodingException {
        Graph gg = new Graph();

        OpenStreetMapModule loader = new OpenStreetMapModule();
        loader.setDefaultWayPropertySetSource(new DefaultWayPropertySetSource());
        AnyFileBasedOpenStreetMapProviderImpl provider = new AnyFileBasedOpenStreetMapProviderImpl();

        File file = new File(
                URLDecoder.decode(FakeGraph.class.getResource("columbus.osm.pbf").getFile(),
                        "UTF-8"));

        provider.setPath(file);
        loader.setProvider(provider);

        loader.buildGraph(gg, new HashMap<Class<?>, Object>());
        return gg;
    }

    /** Add transit (not just stops) to a Columbus graph */
    public static void addTransit (Graph gg) throws Exception {
        // using conveyal GTFS lib to build GTFS so a lot of code does not have to be rewritten later
        // once we're using the conveyal GTFS lib for everything we ought to be able to do this
        // without even writing out the GTFS to a file.
        GTFSFeed feed = new GTFSFeed();
        Agency a = createDummyAgency("agency", "Agency", "America/New_York");
        feed.agency.put("agency", a);

        Route r = new Route();
        r.route_short_name = "1";
        r.route_long_name = "High Street";
        r.route_type = 3;
        r.agency_id = a.agency_id;
        r.route_id = "route";
        feed.routes.put(r.route_id, r);

        Service s = createDummyService();
        feed.services.put(s.service_id, s);

        com.conveyal.gtfs.model.Stop s1 = new com.conveyal.gtfs.model.Stop();
        s1.stop_id = s1.stop_name = "s1";
        s1.stop_lat = 40.2182;
        s1.stop_lon = -83.0889;
        feed.stops.put(s1.stop_id, s1);

        com.conveyal.gtfs.model.Stop s2 = new com.conveyal.gtfs.model.Stop();
        s2.stop_id = s2.stop_name = "s2";
        s2.stop_lat = 39.9621;
        s2.stop_lon = -83.0007;
        feed.stops.put(s2.stop_id, s2);

        // make timetabled trips
        for (int departure = 7 * 3600; departure < 20 * 3600; departure += FREQUENCY) {
            Trip t = new Trip();
            t.trip_id = "trip" + departure;
            t.service_id = s.service_id;
            t.route_id = r.route_id;
            feed.trips.put(t.trip_id, t);

            StopTime st1 = new StopTime();
            st1.trip_id = t.trip_id;
            st1.arrival_time = departure;
            st1.departure_time = departure;
            st1.stop_id = s1.stop_id;
            st1.stop_sequence = 1;
            feed.stop_times.put(new Fun.Tuple2(st1.trip_id, st1.stop_sequence), st1);

            StopTime st2 = new StopTime();
            st2.trip_id = t.trip_id;
            st2.arrival_time = departure + TRAVEL_TIME;
            st2.departure_time = departure + TRAVEL_TIME;
            st2.stop_sequence = 2;
            st2.stop_id = s2.stop_id;
            feed.stop_times.put(new Fun.Tuple2(st2.trip_id, st2.stop_sequence), st2);
        }

        File tempFile = File.createTempFile("gtfs", ".zip");
        feed.toFile(tempFile.getAbsolutePath());

        // phew. load it into the graph.
        GtfsModule gtfs = new GtfsModule(Arrays.asList(new GtfsBundle(tempFile)));
        gtfs.buildGraph(gg, new HashMap<>());
    }

    /** Add many transit lines to a lot of stops */
    public static void addTransitMultipleLines (Graph g) throws Exception {
        // using conveyal GTFS lib to build GTFS so a lot of code does not have to be rewritten later
        // once we're using the conveyal GTFS lib for everything we ought to be able to do this
        // without even writing out the GTFS to a file.
        GTFSFeed feed = new GTFSFeed();
        Agency a = createDummyAgency("agency", "Agency", "America/New_York");
        feed.agency.put("agency", a);

        Route r = new Route();
        r.route_short_name = "1";
        r.route_long_name = "High Street";
        r.route_type = 3;
        r.agency_id = a.agency_id;
        r.route_id = "route";
        feed.routes.put(r.route_id, r);

        Service s = createDummyService();
        feed.services.put(s.service_id, s);

        int stopIdx = 0;
        while (stopIdx < 10000) {
            com.conveyal.gtfs.model.Stop s1 = new com.conveyal.gtfs.model.Stop();
            s1.stop_id = s1.stop_name = "s" + stopIdx++;
            s1.stop_lat = 39.9354 + (stopIdx % 100) * 1e-3;
            s1.stop_lon = -83.0589 + (stopIdx / 100) * 1e-3;
            feed.stops.put(s1.stop_id, s1);

            com.conveyal.gtfs.model.Stop s2 = new com.conveyal.gtfs.model.Stop();
            s2.stop_id = s2.stop_name = "s" + stopIdx++;
            s2.stop_lat = 39.9354 + (stopIdx % 100) * 1e-3;
            s2.stop_lon = -83.0589 + (stopIdx / 100) * 1e-3;
            feed.stops.put(s2.stop_id, s2);

            // make timetabled trips
            int departure = 8 * 3600;
            Trip t = new Trip();
            t.trip_id = "trip" + departure + "_" + stopIdx;
            t.service_id = s.service_id;
            t.route_id = r.route_id;
            feed.trips.put(t.trip_id, t);

            StopTime st1 = new StopTime();
            st1.trip_id = t.trip_id;
            st1.arrival_time = departure;
            st1.departure_time = departure;
            st1.stop_id = s1.stop_id;
            st1.stop_sequence = 1;
            feed.stop_times.put(new Fun.Tuple2(st1.trip_id, st1.stop_sequence), st1);

            StopTime st2 = new StopTime();
            st2.trip_id = t.trip_id;
            st2.arrival_time = departure + 500;
            st2.departure_time = departure + 500;
            st2.stop_sequence = 2;
            st2.stop_id = s2.stop_id;
            feed.stop_times.put(new Fun.Tuple2(st2.trip_id, st2.stop_sequence), st2);

        }

        File tempFile = File.createTempFile("gtfs", ".zip");
        feed.toFile(tempFile.getAbsolutePath());

        // phew. load it into the graph.
        GtfsModule gtfs = new GtfsModule(Arrays.asList(new GtfsBundle(tempFile)));
        gtfs.buildGraph(g, new HashMap<>());
    }

    public static void addPerpendicularRoutes(Graph graph) throws Exception {
        GTFSFeed feed = new GTFSFeed();
        Agency agencyA = createDummyAgency("agencyA", "Agency A", "America/New_York");
        feed.agency.put("agencyA", agencyA);
        Agency agencyB = createDummyAgency("agencyB", "Agency B", "America/New_York");
        feed.agency.put("agencyB", agencyB);
        Service s = createDummyService();
        feed.services.put(s.service_id, s);

        int stopIdX = 0;
        int stopIdY = 0;
        for (double lat = 39.9058; lat < 40.0281; lat += 0.005) {
            stopIdX = 0;
            for (double lon = -83.1341; lon < -82.8646; lon += 0.005) {
                com.conveyal.gtfs.model.Stop stop = new com.conveyal.gtfs.model.Stop();
                stop.stop_id = stop.stop_name = String.format("s-%d-%d", stopIdX, stopIdY);
                stop.stop_lat = lat;
                stop.stop_lon = lon;
                feed.stops.put(stop.stop_id, stop);
                stopIdX++;
            }
            stopIdY++;
        }

        for (int i = 0; i < stopIdY; i++) {
            Route route = new Route();
            route.route_short_name = "hr" + i;
            route.route_long_name = i + "th Horizontal Street";
            route.route_type = Route.BUS;
            route.agency_id = agencyA.agency_id;
            route.route_id = "horizontalroute" + i;
            feed.routes.put(route.route_id, route);
        }
        for (int i = 0; i < stopIdX; i++) {
            Route route = new Route();
            route.route_short_name = "vr" + i;
            route.route_long_name = i + "th Vertical Street";
            route.route_type = Route.TRAM;
            route.agency_id = agencyB.agency_id;
            route.route_id = "verticalroute" + i;
            feed.routes.put(route.route_id, route);
        }

        Map<String, Route> routes = feed.routes;
        com.conveyal.gtfs.model.Stop stop;
        for (Route route : routes.values()) {
            int routeId = Integer.parseInt(route.route_short_name.substring(2));
            int x, y;
            boolean isHorizontal = route.route_short_name.startsWith("hr");
            for (int departure = 7 * 3600; departure < 20 * 3600; departure += FREQUENCY) {
                Trip t = new Trip();
                t.trip_id = "trip:" + route.route_id + ":" + departure;
                t.service_id = s.service_id;
                t.route_id = route.route_id;
                feed.trips.put(t.trip_id, t);

                int departureTime = departure;
                int nrOfStops = (isHorizontal ? stopIdX : stopIdY);
                for (int stopSequenceNr = 0; stopSequenceNr < nrOfStops; stopSequenceNr++) {
                    x = (isHorizontal ? stopSequenceNr : routeId);
                    y = (isHorizontal ? routeId : stopSequenceNr);
                    stop = feed.stops.get(String.format("s-%d-%d", x, y));
                    StopTime st1 = new StopTime();
                    st1.trip_id = t.trip_id;
                    st1.arrival_time = departureTime;
                    st1.departure_time = departureTime;
                    st1.stop_id = stop.stop_id;
                    st1.stop_sequence = stopSequenceNr + 1;
                    feed.stop_times.put(new Fun.Tuple2(st1.trip_id, st1.stop_sequence), st1);
                    //connect last stop to first so graph is fully reachable
                    if (stopSequenceNr == 0) {
                        StopTime stopTime = new StopTime();
                        stopTime.trip_id = t.trip_id;
                        stopTime.arrival_time = departureTime + nrOfStops * 120 + 30 * 60;
                        stopTime.departure_time = departureTime + nrOfStops * 120 + 30 * 60;
                        stopTime.stop_id = stop.stop_id;
                        stopTime.stop_sequence = stopSequenceNr + 1 + nrOfStops;
                        feed.stop_times.put(new Fun.Tuple2(stopTime.trip_id, stopTime.stop_sequence), stopTime);
                    }
                    departureTime += 120;
                }
            }
        }
        File tempFile = File.createTempFile("gtfs", ".zip");
        feed.toFile(tempFile.getAbsolutePath());

        // phew. load it into the graph.
        GtfsModule gtfs = new GtfsModule(Arrays.asList(new GtfsBundle(tempFile)));
        gtfs.buildGraph(graph, new HashMap<>());
    }

    private static Service createDummyService() {
        Service s = new Service("service");
        s.calendar = new Calendar();
        s.calendar.service_id = s.service_id;
        s.calendar.monday = s.calendar.tuesday = s.calendar.wednesday = s.calendar.thursday = s.calendar.friday =
                s.calendar.saturday = s.calendar.sunday = 1;
        s.calendar.start_date = 19991231;
        s.calendar.end_date = 21001231;
        return s;
    }

    private static Agency createDummyAgency(String id, String name, String timeZone) {
        Agency a = new Agency();
        a.agency_id = id;
        a.agency_name = name;
        a.agency_timezone = timeZone;
        try {
            a.agency_url = new URL("http://www.example.com/" + id);
        } catch (MalformedURLException e) {
            // This really can't happen
            assert false;
            a.agency_url = null;
        }
        return a;
    }

    /** Add a transit line with multiple patterns to a Columbus graph. Most trips serve stops s1, s2, s3 but some serve only s1, s3 */
    public static void addMultiplePatterns (Graph gg) throws Exception {
        // using conveyal GTFS lib to build GTFS so a lot of code does not have to be rewritten later
        // once we're using the conveyal GTFS lib for everything we ought to be able to do this
        // without even writing out the GTFS to a file.
        GTFSFeed feed = new GTFSFeed();
        Agency a = createDummyAgency("agency", "Agency", "America/New_York");
        feed.agency.put("agency", a);

        Route r = new Route();
        r.route_short_name = "1";
        r.route_long_name = "High Street";
        r.route_type = 3;
        r.agency_id = a.agency_id;
        r.route_id = "route";
        feed.routes.put(r.route_id, r);

        Service s = createDummyService();
        feed.services.put(s.service_id, s);

        com.conveyal.gtfs.model.Stop s1 = new com.conveyal.gtfs.model.Stop();
        s1.stop_id = s1.stop_name = "s1";
        s1.stop_lat = 40.2182;
        s1.stop_lon = -83.0889;
        feed.stops.put(s1.stop_id, s1);

        com.conveyal.gtfs.model.Stop s2 = new com.conveyal.gtfs.model.Stop();
        s2.stop_id = s2.stop_name = "s2";
        s2.stop_lat = 39.9621;
        s2.stop_lon = -83.0007;
        feed.stops.put(s2.stop_id, s2);

        com.conveyal.gtfs.model.Stop s3 = new com.conveyal.gtfs.model.Stop();
        s3.stop_id = s3.stop_name = "s3";
        s3.stop_lat = 39.9510;
        s3.stop_lon = -83.0007;
        feed.stops.put(s3.stop_id, s3);

        // make timetabled trips
        for (int departure = 7 * 3600, dcount = 0; departure < 20 * 3600; departure += FREQUENCY) {
            Trip t = new Trip();
            t.trip_id = "trip" + departure;
            t.service_id = s.service_id;
            t.route_id = r.route_id;
            feed.trips.put(t.trip_id, t);

            StopTime st1 = new StopTime();
            st1.trip_id = t.trip_id;
            st1.arrival_time = departure;
            st1.departure_time = departure;
            st1.stop_id = s1.stop_id;
            st1.stop_sequence = 1;
            feed.stop_times.put(new Fun.Tuple2(st1.trip_id, st1.stop_sequence), st1);

            // occasionally skip second stop
            boolean secondStop = dcount++ % 10 != 0;
            if (secondStop) {
                StopTime st2 = new StopTime();
                st2.trip_id = t.trip_id;
                st2.arrival_time = departure + TRAVEL_TIME;
                st2.departure_time = departure + TRAVEL_TIME;
                st2.stop_sequence = 2;
                st2.stop_id = s2.stop_id;
                feed.stop_times.put(new Fun.Tuple2(st2.trip_id, st2.stop_sequence), st2);
            }

            StopTime st3 = new StopTime();
            st3.trip_id = t.trip_id;
            st3.arrival_time = departure + (secondStop ? 2 : 1) * TRAVEL_TIME;
            st3.departure_time = departure + (secondStop ? 2 : 1) * TRAVEL_TIME;
            st3.stop_sequence = 3;
            st3.stop_id = s3.stop_id;
            feed.stop_times.put(new Fun.Tuple2(st3.trip_id, st3.stop_sequence), st3);
        }

        File tempFile = File.createTempFile("gtfs", ".zip");
        feed.toFile(tempFile.getAbsolutePath());

        // phew. load it into the graph.
        GtfsModule gtfs = new GtfsModule(Arrays.asList(new GtfsBundle(tempFile)));
        gtfs.buildGraph(gg, new HashMap<>());
    }

    /** Add a regular grid of stops to the graph */
    public static void addRegularStopGrid(Graph g) {
        int count = 0;
        for (double lat = 39.9058; lat < 40.0281; lat += 0.005) {
            for (double lon = -83.1341; lon < -82.8646; lon += 0.005) {
                String id = "" + count++;
                AgencyAndId aid = new AgencyAndId("TEST", id);
                Stop stop = new Stop();
                stop.setLat(lat);
                stop.setLon(lon);
                stop.setName(id);
                stop.setCode(id);
                stop.setId(aid);

                new TransitStop(g, stop);
                count++;
            }
        }
    }

    /** add some extra stops to the graph */
    public static void addExtraStops (Graph g) {
        int count = 0;
        double lon = -83;
        for (double lat = 40; lat < 40.01; lat += 0.005) {
            String id = "EXTRA_" + count++;
            AgencyAndId aid = new AgencyAndId("EXTRA", id);
            Stop stop = new Stop();
            stop.setLat(lat);
            stop.setLon(lon);
            stop.setName(id);
            stop.setCode(id);
            stop.setId(aid);

            new TransitStop(g, stop);
            count++;
        }

        // add some duplicate stops
        lon = -83.1341 + 0.1;

        for (double lat = 39.9058; lat < 40.0281; lat += 0.005) {
            String id = "" + count++;
            AgencyAndId aid = new AgencyAndId("EXTRA", id);
            Stop stop = new Stop();
            stop.setLat(lat);
            stop.setLon(lon);
            stop.setName(id);
            stop.setCode(id);
            stop.setId(aid);

            new TransitStop(g, stop);
            count++;
        }

        // add some almost duplicate stops
        lon = -83.1341 + 0.15;

        for (double lat = 39.9059; lat < 40.0281; lat += 0.005) {
            String id = "" + count++;
            AgencyAndId aid = new AgencyAndId("EXTRA", id);
            Stop stop = new Stop();
            stop.setLat(lat);
            stop.setLon(lon);
            stop.setName(id);
            stop.setCode(id);
            stop.setId(aid);

            new TransitStop(g, stop);
            count++;
        }
    }

    /** Add transit (in both directions) to a Columbus graph */
    public static void addTransitBidirectional (Graph gg) throws Exception {
        // using conveyal GTFS lib to build GTFS so a lot of code does not have to be rewritten later
        // once we're using the conveyal GTFS lib for everything we ought to be able to do this
        // without even writing out the GTFS to a file.
        GTFSFeed feed = new GTFSFeed();
        Agency a = createDummyAgency("agency", "Agency", "America/New_York");
        feed.agency.put("agency", a);

        Route r = new Route();
        r.route_short_name = "1";
        r.route_long_name = "High Street";
        r.route_type = 3;
        r.agency_id = a.agency_id;
        r.route_id = "route";
        feed.routes.put(r.route_id, r);

        Service s = createDummyService();
        feed.services.put(s.service_id, s);

        com.conveyal.gtfs.model.Stop s1 = new com.conveyal.gtfs.model.Stop();
        s1.stop_id = s1.stop_name = "s1";
        s1.stop_lat = 40.2182;
        s1.stop_lon = -83.0889;
        feed.stops.put(s1.stop_id, s1);

        com.conveyal.gtfs.model.Stop s2 = new com.conveyal.gtfs.model.Stop();
        s2.stop_id = s2.stop_name = "s2";
        s2.stop_lat = 39.9621;
        s2.stop_lon = -83.0007;
        feed.stops.put(s2.stop_id, s2);

        // make timetabled trips
        for (int departure = 7 * 3600; departure < 20 * 3600; departure += FREQUENCY) {
            Trip t = new Trip();
            t.trip_id = "trip" + departure;
            t.service_id = s.service_id;
            t.route_id = r.route_id;
            t.direction_id = 0;
            feed.trips.put(t.trip_id, t);

            StopTime st1 = new StopTime();
            st1.trip_id = t.trip_id;
            st1.arrival_time = departure;
            st1.departure_time = departure;
            st1.stop_id = s1.stop_id;
            st1.stop_sequence = 1;
            feed.stop_times.put(new Fun.Tuple2(st1.trip_id, st1.stop_sequence), st1);

            StopTime st2 = new StopTime();
            st2.trip_id = t.trip_id;
            st2.arrival_time = departure + TRAVEL_TIME;
            st2.departure_time = departure + TRAVEL_TIME;
            st2.stop_sequence = 2;
            st2.stop_id = s2.stop_id;
            feed.stop_times.put(new Fun.Tuple2(st2.trip_id, st2.stop_sequence), st2);

            // opposite direction
            t = new Trip();
            t.trip_id = "trip_back" + departure;
            t.service_id = s.service_id;
            t.route_id = r.route_id;
            t.direction_id = 1;
            feed.trips.put(t.trip_id, t);

            st1 = new StopTime();
            st1.trip_id = t.trip_id;
            st1.arrival_time = departure;
            st1.departure_time = departure;
            st1.stop_id = s2.stop_id;
            st1.stop_sequence = 1;
            feed.stop_times.put(new Fun.Tuple2(st1.trip_id, st1.stop_sequence), st1);

            st2 = new StopTime();
            st2.trip_id = t.trip_id;
            st2.arrival_time = departure + TRAVEL_TIME;
            st2.departure_time = departure + TRAVEL_TIME;
            st2.stop_sequence = 2;
            st2.stop_id = s1.stop_id;
            feed.stop_times.put(new Fun.Tuple2(st2.trip_id, st2.stop_sequence), st2);
        }

        File tempFile = File.createTempFile("gtfs", ".zip");
        feed.toFile(tempFile.getAbsolutePath());

        // phew. load it into the graph.
        GtfsModule gtfs = new GtfsModule(Arrays.asList(new GtfsBundle(tempFile)));
        gtfs.buildGraph(gg, new HashMap<>());
    }

    /** link the stops in the graph */
    public static void link (Graph g) {
        SimpleStreetSplitter linker = new SimpleStreetSplitter(g);
        linker.linkAllStationsToGraph();
    }

}
