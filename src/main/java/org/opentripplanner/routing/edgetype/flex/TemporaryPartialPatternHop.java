package org.opentripplanner.routing.edgetype.flex;

import com.vividsolutions.jts.linearref.LengthIndexedLine;
import org.onebusaway.gtfs.model.Stop;
import org.opentripplanner.routing.core.RoutingContext;
import org.opentripplanner.routing.edgetype.PartialPatternHop;
import org.opentripplanner.routing.edgetype.PatternHop;
import org.opentripplanner.routing.edgetype.TemporaryEdge;
import org.opentripplanner.routing.vertextype.PatternArriveVertex;
import org.opentripplanner.routing.vertextype.PatternDepartVertex;
import org.opentripplanner.routing.vertextype.PatternStopVertex;

/**
 * Created by simon on 6/13/17.
 */
// this may replace TemporaryPatternHop. the problem I want to solve is multiple possible hops.
public class TemporaryPartialPatternHop extends PartialPatternHop implements TemporaryEdge {
    public TemporaryPartialPatternHop(PatternHop hop, PatternStopVertex from, PatternStopVertex to, Stop fromStop, Stop toStop, RoutingContext rctx, double startIndex, double endIndex) {
        super(hop, from, to, fromStop, toStop, startIndex, endIndex);
    }

    // todo can this be smarter
    // start hop is a hop from the existing origin TO a new flag destination
    public static TemporaryPartialPatternHop startHop(PatternHop hop, PatternArriveVertex to, Stop toStop, RoutingContext rctx) {
        LengthIndexedLine line = new LengthIndexedLine(hop.getGeometry());
        return new TemporaryPartialPatternHop(hop, (PatternStopVertex) hop.getFromVertex(), to, hop.getBeginStop(), toStop, rctx, line.getStartIndex(), line.project(to.getCoordinate()));
    }

    public static TemporaryPartialPatternHop endHop(PatternHop hop, PatternDepartVertex from, Stop fromStop, RoutingContext rctx) {
        LengthIndexedLine line = new LengthIndexedLine(hop.getGeometry());
        return new TemporaryPartialPatternHop(hop, from, (PatternStopVertex) hop.getToVertex(), fromStop, hop.getEndStop(), rctx, line.project(from.getCoordinate()), line.getEndIndex());
    }

    public TemporaryPartialPatternHop shortenEnd(PatternStopVertex to, Stop toStop, RoutingContext rctx) {
        LengthIndexedLine line = new LengthIndexedLine(getOriginalHop().getGeometry());
        double endIndex = line.project(to.getCoordinate());
        if (endIndex < getStartIndex())
            return null;
        return new TemporaryPartialPatternHop(getOriginalHop(), (PatternStopVertex) getFromVertex(), to, getBeginStop(), toStop, rctx, getStartIndex(), endIndex);
    }

    @Override
    public void dispose() {
        fromv.removeOutgoing(this);
        tov.removeIncoming(this);
    }

    // is this hop too not-different to care about? for now lets say should be 2% different *shrugs*
    public boolean isTrivial() {
        return false;
//        double length = getOriginalHopLength();
//        double thisLength = getEndIndex() - getStartIndex();
//        return (thisLength / length) > 0.99;
    }
}