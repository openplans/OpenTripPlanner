/* This file is based on code copied from project OneBusAway, see the LICENSE file for further information. */
package org.opentripplanner.model;

/**
 * A place where actual boarding/departing happens. It can be a bus stop on one side of a road or
 * a platform at a train station. Equivalent to GTFS stop location 0 or NeTEx quay.
 */
public final class Stop extends TransitEntity<FeedScopedId> {

    private static final long serialVersionUID = 1L;

    private FeedScopedId id;

    /**
     * Name of the stop if provided. Usually only code is used.
     */
    private String name;

    private double lat;

    private double lon;

    /**
     * Public facing stop code (short text or number).
     */
    private String code;

    /**
     * Additional information about the stop (if needed).
     */
    private String description;

    /**
     * Used for GTFS fare information.
     */
    private String zone;

    /**
     * URL to a web page containing information about this particular stop.
     */
    private String url;

    private Station parentStation;

    private TransferPriority transferPriority;

    private WheelChairBoarding wheelchairBoarding;

    public Stop() {}

    public Stop(FeedScopedId id) {
        this.id = id;
    }

    @Override public FeedScopedId getId() {
        return id;
    }

    @Override public void setId(FeedScopedId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Station getParentStation() {
        return parentStation;
    }

    public void setParentStation(Station parentStation) {
        this.parentStation = parentStation;
    }

    public TransferPriority getTransferPriority() {
        return transferPriority;
    }

    public void setTransferPriority(TransferPriority transferPriority) {
        this.transferPriority = transferPriority;
    }

    public WheelChairBoarding getWheelchairBoarding() {
        return wheelchairBoarding;
    }

    public void setWheelchairBoarding(WheelChairBoarding wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }

    @Override
    public String toString() {
        return "<Stop " + this.id + ">";
    }
}
