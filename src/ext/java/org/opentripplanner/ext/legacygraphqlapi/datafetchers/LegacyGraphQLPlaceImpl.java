package org.opentripplanner.ext.legacygraphqlapi.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.opentripplanner.ext.legacygraphqlapi.LegacyGraphQLRequestContext;
import org.opentripplanner.ext.legacygraphqlapi.generated.LegacyGraphQLDataFetchers;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.plan.Place;
import org.opentripplanner.model.plan.StopArrival;
import org.opentripplanner.model.plan.VertexType;
import org.opentripplanner.routing.RoutingService;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.routing.bike_rental.BikeRentalStationService;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingService;

public class LegacyGraphQLPlaceImpl implements LegacyGraphQLDataFetchers.LegacyGraphQLPlace {

  @Override
  public DataFetcher<String> name() {
    return environment -> getSource(environment).place.name;
  }

  @Override
  public DataFetcher<String> vertexType() {
    return environment -> getSource(environment).place.vertexType.name();
  }

  @Override
  public DataFetcher<Double> lat() {
    return environment -> getSource(environment).place.coordinate.latitude();
  }

  @Override
  public DataFetcher<Double> lon() {
    return environment -> getSource(environment).place.coordinate.longitude();
  }

  @Override
  public DataFetcher<Long> arrivalTime() {
    return environment -> getSource(environment).arrival.getTime().getTime();
  }

  @Override
  public DataFetcher<Long> departureTime() {
    return environment -> getSource(environment).departure.getTime().getTime();
  }

  @Override
  public DataFetcher<Object> stop() {
    return environment -> {
      Place place = getSource(environment).place;
      return place.vertexType.equals(VertexType.TRANSIT) ?
          getRoutingService(environment).getStopForId(place.stopId) : null;
    };
  }

  @Override
  public DataFetcher<BikeRentalStation> bikeRentalStation() {
    return environment -> {
      Place place = getSource(environment).place;

      if (!place.vertexType.equals(VertexType.BIKESHARE)) { return null; }

      BikeRentalStationService bikerentalStationService = getRoutingService(environment)
          .getBikerentalStationService();

      if (bikerentalStationService == null) { return null; }

      return bikerentalStationService
          .getBikeRentalStations()
          .stream()
          .filter(bikeRentalStation -> bikeRentalStation.id.equals(place.bikeShareId))
          .findAny()
          .orElse(null);
    };
  }

  @Override
  public DataFetcher<Object> bikePark() {
    return this::getVehicleParking;
  }

  @Override
  public DataFetcher<Object> carPark() {
    return this::getVehicleParking;
  }

  private VehicleParking getVehicleParking(DataFetchingEnvironment environment) {
    FeedScopedId vehiclePlaceId = getSource(environment).place.stopId;

    VehicleParkingService vehicleParkingService = getRoutingService(environment)
        .getVehicleParkingService();

    if (vehicleParkingService == null) { return null; }

    return vehicleParkingService
        .getVehicleParkings()
        .filter(vehicleParking -> vehicleParking.getId().equals(vehiclePlaceId))
        .findAny()
        .orElse(null);
  }

  private RoutingService getRoutingService(DataFetchingEnvironment environment) {
    return environment.<LegacyGraphQLRequestContext>getContext().getRoutingService();
  }

  private StopArrival getSource(DataFetchingEnvironment environment) {
    return environment.getSource();
  }
}
