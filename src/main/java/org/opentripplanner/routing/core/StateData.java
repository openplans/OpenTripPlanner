package org.opentripplanner.routing.core;

import java.util.Set;

/**
 * StateData contains the components of search state that are unlikely to be changed as often as
 * time or weight. This avoids frequent duplication, which should have a positive impact on both
 * time and space use during searches.
 */
public class StateData implements Cloneable {

    // the time at which the search started
    protected long startTime;

    protected boolean usingRentedBike;

    // TODO TNC - Is it possible to solve this in another way? (Next 3 fields)
    protected boolean usingHailedCar;

    protected boolean hasHailedCar = false;

    protected boolean carParked;

    protected boolean bikeParked;

    protected RoutingRequest opt;

    protected TraverseMode mode;

    /**
     * The mode that was used to traverse the backEdge
     */
    protected TraverseMode backMode;

    protected boolean backWalkingBike;

    public Set<String> bikeRentalNetworks;

    /* This boolean is set to true upon transition from a normal street to a no-through-traffic street. */
    protected boolean enteredNoThroughTrafficArea;

    public StateData(RoutingRequest options) {
        TraverseModeSet modes = options.modes;
        if (modes.getCar())
            mode = TraverseMode.CAR;
        else if (modes.getWalk())
            mode = TraverseMode.WALK;
        else if (modes.getBicycle())
            mode = TraverseMode.BICYCLE;
        else
            mode = null;
    }

    protected StateData clone() {
        try {
            return (StateData) super.clone();
        } catch (CloneNotSupportedException e1) {
            throw new IllegalStateException("This is not happening");
        }
    }

    public boolean hasHailedCar() { return hasHailedCar; }
}
