package org.opentripplanner.api.mapping;

import org.opentripplanner.api.model.ApiLeg;
import org.opentripplanner.ext.tnc.routing.mapping.TncApiMapper;
import org.opentripplanner.model.plan.Leg;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.util.OTPFeature;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LegMapper {
    private final WalkStepMapper walkStepMapper;
    private final AlertMapper alertMapper;

    public LegMapper(Locale locale) {
        this.walkStepMapper = new WalkStepMapper(locale);
        this.alertMapper = new AlertMapper(locale);
    }

    public List<ApiLeg> mapLegs(List<Leg> domain) {
        if(domain == null) { return null; }

        List<ApiLeg> apiLegs = new ArrayList<>();

        final int size = domain.size();
        final int lastIdx = size-1;

        for (int i=0; i < size; ++i) {
            Calendar arrivalTimeFromPlace = (i == 0) ? null : domain.get(i-1).endTime;
            Calendar departureTimeToPlace = (i == lastIdx) ? null : domain.get(i+1).startTime;

            apiLegs.add(mapLeg(domain.get(i), arrivalTimeFromPlace, departureTimeToPlace));
        }
        return apiLegs;
    }

    public ApiLeg mapLeg(Leg domain, Calendar arrivalTimeFromPlace, Calendar departureTimeToPlace) {
        if(domain == null) { return null; }
        ApiLeg api = new ApiLeg();
        api.startTime = domain.startTime;
        api.endTime = domain.endTime;

        // Set the arrival and departure times, even if this is redundant information
        api.from = PlaceMapper.mapPlace(domain.from, arrivalTimeFromPlace, api.startTime);
        api.to = PlaceMapper.mapPlace(domain.to, api.endTime, departureTimeToPlace);

        api.departureDelay = domain.departureDelay;
        api.arrivalDelay = domain.arrivalDelay;
        api.realTime = domain.realTime;
        api.isNonExactFrequency = domain.isNonExactFrequency;
        api.headway = domain.headway;
        api.distance = domain.distanceMeters;
        api.pathway = domain.pathway;

        // TODO OTP2 - This is fragile - what happen if we rename the domain modes?
        api.mode = domain.mode.name();
        api.route = domain.route;
        api.agencyName = domain.agencyName;
        api.agencyUrl = domain.agencyUrl;
        api.agencyBrandingUrl = domain.agencyBrandingUrl;
        api.agencyTimeZoneOffset = domain.agencyTimeZoneOffset;
        api.routeColor = domain.routeColor;
        api.routeType = domain.routeType;
        api.routeId = domain.routeId;
        api.routeTextColor = domain.routeTextColor;
        api.interlineWithPreviousLeg = domain.interlineWithPreviousLeg;
        api.tripShortName = domain.tripShortName;
        api.tripBlockId = domain.tripBlockId;
        api.headsign = domain.headsign;
        api.agencyId = domain.agencyId;
        api.tripId = domain.tripId;
        api.serviceDate = domain.serviceDate;
        api.routeBrandingUrl = domain.routeBrandingUrl;
        api.intermediateStops = PlaceMapper.mapStopArrivals(domain.intermediateStops);
        api.legGeometry = domain.legGeometry;
        api.walkSteps = walkStepMapper.mapWalkSteps(domain.walkSteps);
        api.alerts = alertMapper.mapAlerts(domain.alerts);
        api.routeShortName = domain.routeShortName;
        api.routeLongName = domain.routeLongName;
        api.boardRule = domain.boardRule;
        api.alightRule = domain.alightRule;
        api.rentedBike = domain.rentedBike;
        api.hailedCar = domain.hailed && domain.mode == TraverseMode.CAR;

        if(OTPFeature.TncRouting.isOn()) {
            api.tncData = TncApiMapper.mapData(domain.rideOffer);
        }

        return api;
    }
}
