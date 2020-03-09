package org.opentripplanner.ext.tnc.api.model;

import org.opentripplanner.ext.tnc.routing.model.TransportationNetworkCompany;

/**
 * TNC summary included with itinerary {@link Leg}.
 */
public class TransportationNetworkCompanySummary {
    /**
     * The specific company that the TNC travel is for.
     */
    public TransportationNetworkCompany company;
    /**
     * The payment currency that the cost estimates are provided in.
     */
    public String currency;
    /**
     * The predicted travel duration in seconds of this leg as obtained from the API of the TNC provider.
     */
    public int travelDuration;
    /**
     * The maximum estimated cost in the specified currency as obtained from the API of the TNC provider.
     */
    public double maxCost;
    /**
     * The minimum estimated cost in the specified currency as obtained from the API of the TNC provider.
     */
    public double minCost;
    /**
     * The ID of the relevant travel product as obtained from the API of the TNC provider.
     */
    public String productId;
    /**
     * The human-readable display name of the particular TNC product as obtained from the API of the TNC provider.
     */
    public String displayName;
    /**
     * The estimated time in seconds for a TNC vehicle to reach the starting point of the ride as obtained from the API
     * of the TNC provider.
     */
    public int estimatedArrival;

}
