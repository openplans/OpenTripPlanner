package org.opentripplanner.transit.raptor.api.path;

import java.util.Objects;
import org.opentripplanner.transit.raptor.api.transit.RaptorTripSchedule;

/**
 * Represent a transit leg in a path.
 *
 * @param <T> The TripSchedule type defined by the user of the raptor API.
 */
public final class TransitPathLeg<T extends RaptorTripSchedule> extends IntermediatePathLeg<T> {
    private static final int NOT_SET = -1;

    private final PathLeg<T> next;
    private final T trip;
    private int fromStopPosition = NOT_SET;
    private int toStopPosition = NOT_SET;

    public TransitPathLeg(int fromStop, int fromTime, int toStop, int toTime, int cost, T trip, PathLeg<T> next) {
        super(fromStop, fromTime, toStop, toTime, cost);
        this.next = next;
        this.trip = trip;
    }

    /** Create a builder to change board or alight stop place. */
    public TransitPathLegBuilder<T> mutate() {
        return new TransitPathLegBuilder<>(this);
    }

    /**
     * The trip schedule info object passed into Raptor routing algorithm. 
     */
    public T trip() {
        return trip;
    }

    public int getFromStopPosition() {
        if(fromStopPosition == NOT_SET) {
            fromStopPosition = trip.findDepartureStopPosition(fromTime(), fromStop());
        }
        return fromStopPosition;
    }

    public int getToStopPosition() {
        if(toStopPosition == NOT_SET) {
            toStopPosition = trip.findArrivalStopPosition(toTime(), toStop());
        }
        return toStopPosition;
    }

    @Override
    public final boolean isTransitLeg() {
        return true;
    }

    @Override
    public final PathLeg<T> nextLeg() {
        return next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!super.equals(o)) { return false; }
        TransitPathLeg<?> that = (TransitPathLeg<?>) o;
        return trip.equals(that.trip) && next.equals(that.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), next, trip);
    }

    @Override
    public String toString() {
        return trip.pattern().debugInfo() + " " + asString(toStop());
    }
}
