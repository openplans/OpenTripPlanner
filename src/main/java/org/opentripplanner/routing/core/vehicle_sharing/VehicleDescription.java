package org.opentripplanner.routing.core.vehicle_sharing;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.StreetEdge;

import static java.lang.Double.min;

public abstract class VehicleDescription {

    private final String providerVehicleId;
    private final double longitude;
    private final double latitude;
    private final double rangeInMeters;

    @JsonSerialize
    private final FuelType fuelType;

    @JsonSerialize
    private final Gearbox gearbox;

    @JsonIgnore
    private final Provider provider;

    public VehicleDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                              Gearbox gearbox, Provider provider) {
        this(providerVehicleId, longitude, latitude, fuelType, gearbox, provider, null);
    }

    public VehicleDescription(String providerVehicleId, double longitude, double latitude, FuelType fuelType,
                              Gearbox gearbox, Provider provider, Double rangeInMeters) {
        if (rangeInMeters == null)
            rangeInMeters = this.getDefaultRangeInMeters();

        rangeInMeters = min(rangeInMeters, getMaximumRangeInMeters());

        this.providerVehicleId = providerVehicleId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.fuelType = fuelType;
        this.gearbox = gearbox;
        this.provider = provider;
        this.rangeInMeters = rangeInMeters;
    }

    @Override
    public String toString() {
        return "VehicleDescription{" +
                "providerVehicleId=" + providerVehicleId +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", fuelType=" + fuelType +
                ", gearbox=" + gearbox +
                ", providerId=" + provider.getId() +
                ", providerName=" + provider.getName() +
                '}';
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getProviderVehicleId() {
        return providerVehicleId;
    }

    FuelType getFuelType() {
        return fuelType;
    }

    Gearbox getGearbox() {
        return gearbox;
    }

    Provider getProvider() {
        return provider;
    }

    public double getRangeInMeters() {
        return rangeInMeters;
    }

    @JsonSerialize
    public int getProviderId() {
        return provider.getId();
    }

    @JsonSerialize
    public String getProviderName() {
        return provider.getName();
    }

    /**
     * Returns maximum speed on given street. Trivial getter for most vehicles.
     */
    @JsonIgnore
    public abstract double getMaxSpeedInMetersPerSecond(StreetEdge streetEdge);

    @JsonIgnore
    public abstract TraverseMode getTraverseMode();

    @JsonSerialize
    public abstract VehicleType getVehicleType();

    protected abstract double getDefaultRangeInMeters();

    protected Double getMaximumRangeInMeters() {
        return Double.MAX_VALUE;
    }
}