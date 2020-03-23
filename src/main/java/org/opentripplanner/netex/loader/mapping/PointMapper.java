package org.opentripplanner.netex.loader.mapping;

import org.opentripplanner.model.Coordinate;
import org.rutebanken.netex.model.LocationStructure;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;

/**
 * Help mapping a location and verifying a correct input (prevent NPE).
 */
class PointMapper {
    /**
     * This is a utility mapper with static methods, the constructor is private to prevent
     * creating new instances of this class.
     */
    private PointMapper() {}

    /**
     * This utility method check if the given {@code point} or one of its sub elements is
     * {@code null} before passing the location to the given {@code locationHandler}.
     *
     * @return true if the handler is successfully invoked with a location, {@code false} if
     *         any of the required data elements are {@code null}.
     */
    static Coordinate mapCoordinate(SimplePoint_VersionStructure point) {
        if(point == null || point.getLocation() == null) { return null; }
        LocationStructure loc = point.getLocation();

        // This should not happen
        if (loc.getLongitude() == null || loc.getLatitude() == null) {
            throw new IllegalArgumentException("Coordinate is not valid: " + loc);
        }
        // Location is safe to process
        return new Coordinate(loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue());
    }
}