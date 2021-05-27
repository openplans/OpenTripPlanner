package org.opentripplanner.ext.legacygraphqlapi.datafetchers;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import org.opentripplanner.model.Stop;
import org.opentripplanner.routing.graphfinder.PatternAtStop;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;

public class LegacyGraphQLPlaceInterfaceTypeResolver implements TypeResolver {

  @Override
  public GraphQLObjectType getType(TypeResolutionEnvironment environment) {
    Object o = environment.getObject();
    GraphQLSchema schema = environment.getSchema();

    if (o instanceof BikeRentalStation) { return schema.getObjectType("BikeRentalStation"); }
    if (o instanceof PatternAtStop) { return schema.getObjectType("DepartureRow"); }
    if (o instanceof Stop) { return schema.getObjectType("Stop"); }
    if (o instanceof VehicleParking) { return schema.getObjectType("VehicleParking"); }

    return null;
  }
}
