/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.trippattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.opentripplanner.routing.edgetype.TableTripPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An TripUpdate is an ordered list of Updates which all refer to the same trip on the same day.
 * This class also provides methods for building, filtering, and sanity-checking such lists. 
 * @author abyrd
 */
public class TripUpdate extends AbstractUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(TripUpdate.class);

    public static final int MATCH_FAILED = -1;

    /** The trip to add, for ADDED updates. */
    @Getter
    private final Trip trip;

    private final List<Update> updates;

    @Getter
    private final Status status;

    protected TripUpdate(AgencyAndId tripId, long timestamp, ServiceDate serviceDate, Status status, List<Update> updates, Trip trip) {
        super(tripId, timestamp, serviceDate);
        this.status = status;
        this.updates = updates;
        this.trip = trip;
    }

    public List<Update> getUpdates() {
        return Collections.unmodifiableList(updates);
    }

    public boolean isCancellation() {
        return getStatus() == Status.CANCELED;
    }

    public boolean hasDelay() {
        return !updates.isEmpty() && updates.get(0).hasDelay();
    }

    public enum Status {
        /** This trip should be added to the graph, valid on the given serviceDate. */
        ADDED,
        /** This trip isn't running on the given serviceDate. */
        CANCELED,
        /** This trip should be removed from the graph. Only valid for ADDED trips. */
        REMOVED,
        /** This trip should be modified for the given serviceDate. */
        MODIFIED
    }
    
    public static TripUpdate forCanceledTrip(AgencyAndId tripId, long timestamp, ServiceDate serviceDate) {
        return new TripUpdate(tripId, timestamp, serviceDate, Status.CANCELED, Collections.<Update> emptyList(), null);
    }
    
    public static TripUpdate forRemovedTrip(AgencyAndId tripId, long timestamp, ServiceDate serviceDate) {
        return new TripUpdate(tripId, timestamp, serviceDate, Status.REMOVED, Collections.<Update> emptyList(), null);
    }
    
    public static TripUpdate forAddedTrip(Trip trip, long timestamp, ServiceDate serviceDate,  List<Update> stopTimes) {
        if(trip == null || trip.getId() == null || trip.getRoute() == null)
            throw new IllegalArgumentException("A trip with a valid tripId and route must be supplied.");
        if(stopTimes == null || stopTimes.size() < 2)
            throw new IllegalArgumentException("At least two stop times need to be supplied.");

        return new TripUpdate(trip.getId(), timestamp, serviceDate, Status.ADDED, stopTimes, trip);
    }
    
    public static TripUpdate forUpdatedTrip(AgencyAndId tripId, long timestamp, ServiceDate serviceDate, List<Update> updates) {
        if(updates == null || updates.isEmpty())
            throw new IllegalArgumentException("At least one update needs to be supplied.");

        return new TripUpdate(tripId, timestamp, serviceDate, Status.MODIFIED, updates, null);
    }

    /**
     * This method takes a list of updates that may have mixed TripIds, dates, etc. and splits it 
     * into a list of TripUpdatess, with each TripUpdate referencing a single trip on a single day.
     * TODO: implement date support for updates
     */
    public static List<TripUpdate> splitByTrip(List<Update> mixedUpdates) {
        List<TripUpdate> ret = new ArrayList<TripUpdate>();
        // Update comparator sorts on (tripId, timestamp, stopSequence, depart)
        Collections.sort(mixedUpdates);
        List<Update> blockUpdates = new LinkedList<Update>();
        blockUpdates.add(mixedUpdates.remove(0));

        for (Update u : mixedUpdates) { // create a new block when the trip or timestamp changes
            Update l = blockUpdates.get(0);
            if (!l.tripId.equals(u.tripId) || l.timestamp != u.timestamp || l.serviceDate != u.serviceDate) {
                TripUpdate tripUpdate = TripUpdate.forUpdatedTrip(l.tripId, l.timestamp, l.serviceDate, blockUpdates);
                ret.add(tripUpdate);
                blockUpdates = new LinkedList<Update>();
            }
            blockUpdates.add(u);
        }

        Update l = blockUpdates.get(0);
        TripUpdate tripUpdate = TripUpdate.forUpdatedTrip(l.tripId, l.timestamp, l.serviceDate, blockUpdates);
        ret.add(tripUpdate);

        return ret;
    }
    
    /** 
     * Check that this TripUpdate is internally coherent, meaning that:
     * 1. all Updates' trip_ids are the same, and match the UpdateBlock's trip_id
     * 2. stop sequence numbers are sequential and increasing
     * 3. all dwell times and run times are positive
     */
    public boolean isCoherent() {

        //LOG.debug("{}", this.toString());
        for (Update u : updates)
            if (u == null || ! u.tripId.equals(this.tripId))
               return false;

        // check that sequence numbers are sequential and increasing
        boolean increasing = true;
        boolean sequential = true;
        boolean timesCoherent = true;
        Update prev_u = null;
        for (Update u : updates) {
            if (prev_u != null) {
                if (u.stopSeq <= prev_u.stopSeq)
                    increasing = false;
                if (u.stopSeq - prev_u.stopSeq != 1)
                    sequential = false;
                if (prev_u.status != Update.Status.CANCEL && u.status != Update.Status.CANCEL
                                && u.arrive < prev_u.depart)
                    timesCoherent = false;
            }
            prev_u = u;
        }
        return increasing && timesCoherent; // || !sequential)
    }
    
    public boolean filter(boolean passed, boolean negativeDwells, boolean duplicateStops) {
        boolean modified = false;
        Update u, prev_u = null;
        for (Iterator<Update> iter = updates.iterator(); iter.hasNext(); prev_u = u) {
            u = iter.next();
            if (passed && u.status == Update.Status.PASSED) {
                iter.remove();
                modified = true;
                continue;
            }
            if (duplicateStops && prev_u != null && prev_u.stopId.equals(u.stopId)) {
                // updates with the same sequence number within a block are sorted by departure 
                // time. keeping the first update (earliest departure) is the more conservative 
                // option for depart-after trip planning
                // this should not happen since we are splitting into blocks on tripid and timestamp.
                LOG.warn("filtered duplicate stop {} from update for trip {}", u.stopId, u.tripId);
                iter.remove();
                modified = true;
                continue;
            }
            // last update in trip may have 0 departure
            if (negativeDwells && u.depart < u.arrive && u.depart != 0) {
                // in KV8 negative dwell times are very common, so logging them is debug-level
                LOG.debug("filtered negative dwell time at stop {} in update for trip {}",
                        u.stopId, u.tripId);
                u.arrive = u.depart;
                modified = true;
            }
        }
        return modified;
    }

    /**        
     * Updates may cover subsets of the scheduled stop times. In Dutch KV8 data, these update blocks 
     * are not aligned with respect to the full trip. They are however contiguous, and delay 
     * predictions decay linearly to match scheduled times at the end of the block of updates.
     * 
     * (Actually, I see that they don't in the middle of the night, and maybe we should just throw 
     * them out if they don't meet these criteria.)
     * 
     * This means that we can use scheduled times for the rest of the trip: updates are not 
     * cumulative. Note that GTFS sequence numbers are increasing but not necessarily sequential.
     * Though most NL data providers use increasing, sequential values, Arriva Line 315 does not.
     * 
     * OTP does not store stop sequence numbers, since they could potentially be different for each
     * trip in a pattern. Maybe we should, and just reuse the array when they are the same, and set
     * it to null when they are increasing and sequential.
     * 
     * Update blocks cannot be matched to stop blocks on the basis of the first update/stop because 
     * routes may contain loops with the same stop appearing twice. Because of all this we need to 
     * do some matching. This method also verifies that the stopIds match those in the trip, 
     * as redundant error checking.
     * 
     * @param pattern
     * @return
     */
    public int findUpdateStopIndex(TableTripPattern pattern) {
        if (updates == null || updates.size() < 1) {
            LOG.warn("Zero-length or null update block. Cannot match.");
            return MATCH_FAILED;
        }
        int result = matchBlockSimple(pattern);
        if (result == MATCH_FAILED) {
            LOG.debug("Simple block matching failed, trying fuzzy matching.");
            result = matchBlockFuzzy(pattern);
        }
        if (result != MATCH_FAILED) {
            LOG.debug("Found matching stop block at index {}.", result);
            return result;
        }
        LOG.warn("Update block matching failed completely.");
        LOG.warn("Have a look at the pattern and block:");
        List<Stop> patternStops = pattern.getStops();
        int nStops = patternStops.size();
        int nHops = nStops - 1;
        for (int i = 0; i < nStops; i++) {
            Stop s = patternStops.get(i);
            Update u = null; 
            if (i < updates.size())
                u = updates.get(i);
            int ti = pattern.getTripIndex(this.tripId);
            // stop-hop conversion
            int schedArr = (i < 1) ? 0 : pattern.getArrivalTime(i-1, ti);
            int schedDep = (i >= nHops) ? 0 : pattern.getDepartureTime(i, ti);
            System.out.printf("Stop %02d %s A%d D%d >>> %s\n", i, s.getId().getId(), 
                    schedArr, schedDep, (u == null) ? "--" : u.toString());
        }
        return MATCH_FAILED;
    }
    
    private int matchBlockSimple(TableTripPattern pattern) {
        List<Stop> patternStops = pattern.getStops();
        // we are matching the whole block so have an upper bound on the starting point 
        int high = patternStops.size() - updates.size();
        PATTERN: for (int pi = 0; pi <= high; pi++) { // index in pattern
            LOG.trace("---{}", pi);
            for (int ui = 0; ui < updates.size(); ui++) { // index in update
                Stop ps = patternStops.get(pi + ui);
                Update u = updates.get(ui);
                LOG.trace("{} == {}", ps.getId(), u.stopId);
                if ( ! ps.getId().equals(u.stopId)) {
                    continue PATTERN; // full-block match failed, try incrementing offset
                }
            }
            return pi;
        }
        return MATCH_FAILED;
    }
    
    // TODO: TripTimes patching is now not tolerant of mismatched (extra) updates. 
    // Fuzzy matching will have to be destructive or store the match indices in the update.
    // I like the second option.
    private int matchBlockFuzzy(TableTripPattern pattern) {
        List<Stop> patternStops = pattern.getStops();
        int nStops = patternStops.size(); // here we allow matching a subset of the block's updates
        int[] scores = new int[nStops]; // how bad is the match at each offset into the pattern
        for (int pi = 0; pi < nStops; pi++) { // index in pattern
            LOG.trace("---{}", pi);
            int score = 0;
            int si = pi;
            for (int ui = 0; ui < updates.size(); ui++) { // iterate over index within update
                if (si >= nStops) { 
                    break; // skip all remaining updates at the end of the list, do not raise score
                }
                Stop ps = patternStops.get(si);
                Update u = updates.get(ui);
                LOG.trace("{} == {}", ps.getId(), u.stopId);
                if ( ! ps.getId().getId().equals(u.stopId)) {
                    continue; // skip one update, do not raise score, do not increment stop
                }
                si += 1;
                score += 1; // raise the score because we did not need to skip this update
            }
            scores[pi] = score;
        }
        int bestScore = Integer.MIN_VALUE; // higher scores are better
        int bestStop = -1;
        // judging could be folded into above loop, and search stopped when nStops - pi < bestScore
        // but it's not slow enough to bother now, and we can print out a scorecard:
        LOG.debug("fuzzy matching scores: {}", scores);
        for (int i=0; i < nStops; i++) {
            if (scores[i] >= bestScore) { // test equality so we return the latest match
                bestScore = scores[i];
                bestStop = i;
            }
        }
        if (bestScore == 0) { // match failed, none of the offsets matched any updates 
            return MATCH_FAILED;
        }
        /* full-block match succeeded */
        return bestStop;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("tripId: ");
        sb.append(this.tripId);
        for (Update u : updates) {
            sb.append('\n');
            sb.append(u.toString());
        }
        return sb.toString();
    }
}
