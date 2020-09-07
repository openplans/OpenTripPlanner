package org.opentripplanner.api.resource;

import org.locationtech.jts.geom.Envelope;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.routing.bike_rental.BikeRentalStationService;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.standalone.Router;
import org.opentripplanner.util.ResourceBundleSingleton;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.opentripplanner.api.resource.ServerInfo.Q;

@Path("/routers/{routerId}/bike_rental")
@XmlRootElement
public class BikeRental {

    @Context
    OTPServer otpServer;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML + Q, MediaType.TEXT_XML + Q})
    public BikeRentalStationList getBikeRentalStations(
            @QueryParam("lowerLeft") String lowerLeft,
            @QueryParam("upperRight") String upperRight,
            @PathParam("routerId") String routerId,
            @QueryParam("locale") String locale_param) {

        Router router = otpServer.getRouter(routerId);
        if (router == null) return null;
        BikeRentalStationService bikeRentalService = router.graph.getService(BikeRentalStationService.class);
        Locale locale;
        locale = ResourceBundleSingleton.INSTANCE.getLocale(locale_param);
        if (bikeRentalService == null) return new BikeRentalStationList();
        Envelope envelope;
        if (lowerLeft != null) {
            envelope = getEnvelope(lowerLeft, upperRight);
        } else {
            envelope = new Envelope(-180,180,-90,90); 
        }
        Collection<BikeRentalStation> stations = bikeRentalService.getBikeRentalStations();
        List<BikeRentalStation> out = new ArrayList<>();
        for (BikeRentalStation station : stations) {
            if (envelope.contains(station.longitude, station.latitude)) {
                BikeRentalStation station_localized = station.clone();
                station_localized.locale = locale;
                out.add(station_localized);
            }
        }
        BikeRentalStationList brsl = new BikeRentalStationList();
        brsl.stations = out;
        return brsl;
    }

    /** Envelopes are in latitude, longitude format */
    public static Envelope getEnvelope(String lowerLeft, String upperRight) {
        String[] lowerLeftParts = lowerLeft.split(",");
        String[] upperRightParts = upperRight.split(",");

        Envelope envelope = new Envelope(Double.parseDouble(lowerLeftParts[1]),
                Double.parseDouble(upperRightParts[1]), Double.parseDouble(lowerLeftParts[0]),
                Double.parseDouble(upperRightParts[0]));
        return envelope;
    }

}
