package org.opentripplanner.transit.raptor.speed_test.api.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.transit.raptor.speed_test.testcase.Place;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Calendar;

/**
 * A Place is where a journey starts or ends, or a transit stop along the way.
 */
public class PlaceAPI {

    /**
     * For transit stops, the name of the stop.  For points of interest, the name of the POI.
     */
    public String name = null;

    /**
     * The ID of the stop. This is often something that users don't care about.
     */
    public FeedScopedId stopId = null;

    /**
     * The "code" of the stop. Depending on the transit agency, this is often
     * something that users care about.
     */
    public String stopCode = null;

    /**
     * The code or name identifying the quay/platform the vehicle will arrive at or depart from
     */
    public String platformCode = null;

    /**
     * The longitude of the place.
     */
    public Double lon = null;

    /**
     * The latitude of the place.
     */
    public Double lat = null;

    /**
     * The time the rider will arrive at the place.
     */
    public Calendar arrival = null;

    /**
     * The time the rider will depart the place.
     */
    public Calendar departure = null;

    @XmlAttribute
    @JsonSerialize
    public String orig;

    @XmlAttribute
    @JsonSerialize
    public String zoneId;

    /**
     * For transit trips, the stop index (numbered from zero from the start of the trip
     */
    @XmlAttribute
    @JsonSerialize
    public Integer stopIndex;

    /**
     * For transit trips, the sequence number of the stop. Per GTFS, these numbers are increasing.
     */
    @XmlAttribute
    @JsonSerialize
    public Integer stopSequence;

    /**
     * Type of vertex. (Normal, Bike sharing station, Bike P+R, Transit stop)
     * Mostly used for better localization of bike sharing and P+R station names
     */
    @XmlAttribute
    @JsonSerialize
    public VertexType vertexType;

    /**
     * In case the vertex is of type Bike sharing station.
     */
    public String bikeShareId;

    /**
     * In case the vertex is of a parking type.
     */
    public String bikeParkId;

    public String carParkId;

    public PlaceAPI(Place place) {
        this.name = place.getDescription();
        this.stopId = place.getStopId();
        this.lat = place.getLat();
        this.lon = place.getLon();
    }

    /**
     * Returns the geometry in GeoJSON format
     *
     * @return
     */
    @XmlElement
    String getGeometry() {
        return Constants.GEO_JSON_POINT + lon + "," + lat + Constants.GEO_JSON_TAIL;
    }

    public PlaceAPI() {
    }

    public PlaceAPI(Double lon, Double lat, String name) {
        this.lon = lon;
        this.lat = lat;
        this.name = name;
        this.vertexType = VertexType.NORMAL;
    }

    public PlaceAPI(Double lon, Double lat, String name, Calendar arrival, Calendar departure) {
        this(lon, lat, name);
        this.arrival = arrival;
        this.departure = departure;
    }
}
