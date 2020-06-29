package org.opentripplanner.routing.core.vehicle_sharing;

import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.StreetEdge;

public class CarDescription extends VehicleDescription {

    private static final double MAX_SPEED_IN_METERS_PER_SECOND = 40;

    private static final TraverseMode TRAVERSE_MODE = TraverseMode.CAR;

    private static final VehicleType VEHICLE_TYPE = VehicleType.CAR;

    private static final double DEFAULT_RANGE_IN_METERS = 200 * 1000;

    public CarDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                          Gearbox gearbox, Provider provider, Double rangeInMeters) {
        super(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, rangeInMeters);
    }

    public CarDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                          Gearbox gearbox, Provider provider) {
        super(providerVehicleId, longitude, latitude, fuelType, gearbox, provider);
    }

    @Override
    public double getMaxSpeedInMetersPerSecond(StreetEdge streetEdge) {
        return MAX_SPEED_IN_METERS_PER_SECOND;
    }

    @Override
    public TraverseMode getTraverseMode() {
        return TRAVERSE_MODE;
    }

    @Override
    public VehicleType getVehicleType() {
        return VEHICLE_TYPE;
    }

    @Override
    protected double getDefaultRangeInMeters() {
        return DEFAULT_RANGE_IN_METERS;
    }
}