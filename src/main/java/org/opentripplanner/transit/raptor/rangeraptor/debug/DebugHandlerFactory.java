package org.opentripplanner.transit.raptor.rangeraptor.debug;

import org.opentripplanner.transit.raptor.api.path.Path;
import org.opentripplanner.transit.raptor.api.request.DebugRequest;
import org.opentripplanner.transit.raptor.api.transit.TripScheduleInfo;
import org.opentripplanner.transit.raptor.api.view.ArrivalView;
import org.opentripplanner.transit.raptor.rangeraptor.WorkerLifeCycle;
import org.opentripplanner.transit.raptor.rangeraptor.view.DebugHandler;
import org.opentripplanner.transit.raptor.util.paretoset.ParetoSetEventListener;


/**
 * Use this factory to create debug handlers. If a routing request has not enabled debugging
 * {@code null} is returned. Use the {@link #isDebugStopArrival(int)} like methods before
 * retrieving a handler.
 *
 * @param <T> The TripSchedule type defined by the user of the range raptor API.
 */
public class DebugHandlerFactory<T extends TripScheduleInfo> {
    private DebugHandler<ArrivalView<T>> stopHandler;
    private DebugHandler<Path<T>> pathHandler;

    public DebugHandlerFactory(DebugRequest<T> request, WorkerLifeCycle lifeCycle) {
        this.stopHandler = isDebug(request.stopArrivalListener())
                ? new DebugHandlerStopArrivalAdapter<>(request, lifeCycle)
                : null;

        this.pathHandler = isDebug(request.pathFilteringListener())
                ? new DebugHandlerPathAdapter<>(request, lifeCycle)
                : null;
    }

    /* Stop Arrival */

    public boolean isDebugStopArrival() {
        return stopHandler != null;
    }

    public boolean isDebugStopArrival(int stop) {
        return stopHandler != null && stopHandler.isDebug(stop);
    }

    public DebugHandler<ArrivalView<T>> debugStopArrival() {
        return stopHandler;
    }

    public ParetoSetEventListener<ArrivalView<T>> paretoSetStopArrivalListener(int stop) {
        return isDebugStopArrival(stop) ? new ParetoSetDebugHandlerAdapter<>(stopHandler) : null;
    }


    /* path */

    @SuppressWarnings("WeakerAccess")
    public boolean isDebugPath() {
        return pathHandler != null;
    }

    public ParetoSetDebugHandlerAdapter<Path<T>> paretoSetDebugPathListener() {
        return isDebugPath() ? new ParetoSetDebugHandlerAdapter<>(pathHandler) : null;
    }

    /* private methods */

    private boolean isDebug(Object handler) {
        return handler != null;
    }
}
