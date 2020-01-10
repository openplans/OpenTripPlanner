package org.opentripplanner.transit.raptor.rangeraptor;

import org.opentripplanner.transit.raptor.api.transit.TripPatternInfo;
import org.opentripplanner.transit.raptor.api.transit.TripScheduleInfo;
import org.opentripplanner.transit.raptor.rangeraptor.transit.TripScheduleSearch;


/**
 * Provides alternative implementations of some transit-specific logic within the {@link RangeRaptorWorker}.
 *
 * @param <T> The TripSchedule type defined by the user of the range raptor API.
 */
public interface TransitRoutingStrategy<T extends TripScheduleInfo> {

    /**
     * Prepare the {@link TransitRoutingStrategy} to route using the given pattern and tripSearch.
     */
    void prepareForTransitWith(TripPatternInfo<T> pattern, TripScheduleSearch<T> tripSearch);

    /**
     * Perform the routing with the initialized pattern and tripSearch at the given stopPositionInPattern.
     * <p/>
     * This method is called for each stop position in a pattern after the first stop reached in the previous round.
     *
     * @param stopPositionInPattern the current stop position in the pattern set
     *                              in {@link #prepareForTransitWith(TripPatternInfo, TripScheduleSearch)}
     */
    void routeTransitAtStop(final int stopPositionInPattern);

}
