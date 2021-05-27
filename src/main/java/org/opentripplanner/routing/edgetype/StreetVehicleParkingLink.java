package org.opentripplanner.routing.edgetype;

import java.util.Locale;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.vertextype.VehicleParkingEntranceVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;

/**
 * This represents the connection between a street vertex and a vehicle parking vertex.
 */
public class StreetVehicleParkingLink extends Edge {

    private final VehicleParkingEntranceVertex vehicleParkingEntranceVertex;

    public StreetVehicleParkingLink(StreetVertex fromv, VehicleParkingEntranceVertex tov) {
        super(fromv, tov);
        vehicleParkingEntranceVertex = tov;
    }

    public StreetVehicleParkingLink(VehicleParkingEntranceVertex fromv, StreetVertex tov) {
        super(fromv, tov);
        vehicleParkingEntranceVertex = fromv;
    }

    public String getDirection() {
        return null;
    }

    public double getDistanceMeters() {
        return 0;
    }

    public LineString getGeometry() {
        return null;
    }

    public String getName() {
        return vehicleParkingEntranceVertex.getName();
    }

    public String getName(Locale locale) {
        return vehicleParkingEntranceVertex.getName(locale);
    }

    public State traverse(State s0) {
        // Do not even consider bike park vertices unless bike P+R is enabled.
        if (!s0.getOptions().parkAndRide) {
            return null;
        }
        // Disallow traversing two StreetBikeParkLinks in a row.
        // Prevents router using bike rental stations as shortcuts to get around
        // turn restrictions.
        if (s0.getBackEdge() instanceof StreetVehicleParkingLink) {
            return null;
        }

        var entrance = vehicleParkingEntranceVertex.getParkingEntrance();
        if (s0.getNonTransitMode() == TraverseMode.CAR) {
            if (!entrance.isCarAccessible()) {
                return null;
            }
        }
        else if (!entrance.isWalkAccessible()) {
            return null;
        }

        StateEditor s1 = s0.edit(this);
        s1.incrementWeight(1);
        s1.setBackMode(null);
        return s1.makeState();
    }

    public String toString() {
        return "StreetVehicleParkingLink(" + fromv + " -> " + tov + ")";
    }
}
