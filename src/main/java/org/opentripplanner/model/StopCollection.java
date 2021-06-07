package org.opentripplanner.model;

import java.util.Collection;

/**
 * A grouping of Stops referred to by the same name. No actual boarding or alighting happens at this
 * point, but rather at its underlying childStops.
 */
public interface StopCollection {

        FeedScopedId getId();

        String getName();

        /**
         * Implementations should go down the hierarchy and return all the underlying stops
         * recursively.
         */
        Collection<Stop> getChildStops();

        default double getLat() {
                return getCoordinate().latitude();
        }

        default double getLon() {
                return getCoordinate().longitude();
        }

        /**
         * Representative location for the StopLocation. Can either be the actual location of the stop, or
         * the centroid of an area or line.
         */
        WgsCoordinate getCoordinate();
}
