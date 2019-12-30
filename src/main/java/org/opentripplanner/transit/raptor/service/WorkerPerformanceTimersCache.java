package org.opentripplanner.transit.raptor.service;

import org.opentripplanner.transit.raptor.api.request.RangeRaptorRequest;
import org.opentripplanner.transit.raptor.rangeraptor.debug.WorkerPerformanceTimers;

import java.util.HashMap;
import java.util.Map;

public class WorkerPerformanceTimersCache {
    private final Map<String, WorkerPerformanceTimers> timers = new HashMap<>();
    private final boolean multithreaded;

    public WorkerPerformanceTimersCache(boolean multithreaded) {
        this.multithreaded = multithreaded;
    }

    public WorkerPerformanceTimers get(RangeRaptorRequest<?> request) {
        return timers.computeIfAbsent(RequestAlias.alias(request, multithreaded), WorkerPerformanceTimers::new);
    }
}
