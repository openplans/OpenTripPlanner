package org.opentripplanner.routing.vehicle_parking;

import org.opentripplanner.routing.edgetype.StreetVehicleParkingLink;
import org.opentripplanner.routing.edgetype.VehicleParkingEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.VehicleParkingEntranceVertex;

import java.util.List;
import java.util.stream.Collectors;

public class VehicleParkingHelper {

  private VehicleParkingHelper() {}

  public static void linkVehicleParkingToGraph(Graph graph, VehicleParking vehicleParking) {
    var vehicleParkingVertices =VehicleParkingHelper.createVehicleParkingVertices(graph, vehicleParking);
    VehicleParkingHelper.linkVehicleParkingEntrances(vehicleParkingVertices, vehicleParking);
  }

  public static List<VehicleParkingEntranceVertex> createVehicleParkingVertices(Graph graph, VehicleParking vehicleParking) {
    return vehicleParking
        .getEntrances()
        .stream()
        .map(entrance -> new VehicleParkingEntranceVertex(graph, entrance))
        .collect(Collectors.toList());
  }

  public static void linkVehicleParkingEntrances(List<VehicleParkingEntranceVertex> vehicleParkingVertices, VehicleParking vehicleParking) {
    for (int i = 0; i < vehicleParkingVertices.size(); i++) {
      var currentVertex = vehicleParkingVertices.get(i);
      if (isUsableForParking(vehicleParking, currentVertex, currentVertex)) {
        new VehicleParkingEdge(currentVertex, vehicleParking);
      }
      for (int j = i + 1; j < vehicleParkingVertices.size(); j++) {
        var nextVertex = vehicleParkingVertices.get(j);
        if (isUsableForParking(vehicleParking, currentVertex, nextVertex)) {
          new VehicleParkingEdge(currentVertex, nextVertex, vehicleParking);
          new VehicleParkingEdge(nextVertex, currentVertex, vehicleParking);
        }
      }
    }
  }

  private static boolean isUsableForParking(
      VehicleParking vehicleParking,
      VehicleParkingEntranceVertex from,
      VehicleParkingEntranceVertex to
  ) {
    var usableForBikeParking = vehicleParking.hasBicyclePlaces() &&
        from.isWalkAccessible() && to.isWalkAccessible();

    var usableForCarParking = vehicleParking.hasAnyCarPlaces() &&
        (
            (from.isCarAccessible() && to.isWalkAccessible())
                || (from.isWalkAccessible() && to.isCarAccessible())
        );

    return usableForBikeParking || usableForCarParking;
  }

  public static void linkToGraph(VehicleParkingEntranceVertex vehicleParkingEntrance) {
    new StreetVehicleParkingLink(vehicleParkingEntrance, vehicleParkingEntrance.getParkingEntrance().getVertex());
    new StreetVehicleParkingLink(vehicleParkingEntrance.getParkingEntrance().getVertex(), vehicleParkingEntrance);
  }
}
