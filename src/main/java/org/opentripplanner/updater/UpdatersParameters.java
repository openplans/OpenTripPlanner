package org.opentripplanner.updater;

import org.opentripplanner.ext.bikerentalservicedirectory.api.BikeRentalServiceDirectoryFetcherParameters;
import org.opentripplanner.ext.siri.updater.SiriETGooglePubsubUpdaterParameters;
import org.opentripplanner.ext.siri.updater.SiriETUpdaterParameters;
import org.opentripplanner.ext.siri.updater.SiriSXUpdaterParameters;
import org.opentripplanner.ext.siri.updater.SiriVMUpdaterParameters;
import org.opentripplanner.updater.alerts.GtfsRealtimeAlertsUpdaterParameters;
import org.opentripplanner.updater.vehicle_parking.VehicleParkingUpdaterParameters;
import org.opentripplanner.updater.bike_rental.BikeRentalUpdaterParameters;
import org.opentripplanner.updater.stoptime.MqttGtfsRealtimeUpdaterParameters;
import org.opentripplanner.updater.stoptime.PollingStoptimeUpdaterParameters;
import org.opentripplanner.updater.stoptime.WebsocketGtfsRealtimeUpdaterParameters;
import org.opentripplanner.updater.street_notes.WFSNotePollingGraphUpdaterParameters;

import java.util.List;

public interface UpdatersParameters {

  BikeRentalServiceDirectoryFetcherParameters getBikeRentalServiceDirectoryFetcherParameters();

  List<BikeRentalUpdaterParameters> getBikeRentalParameters();

  List<GtfsRealtimeAlertsUpdaterParameters> getGtfsRealtimeAlertsUpdaterParameters();

  List<PollingStoptimeUpdaterParameters> getPollingStoptimeUpdaterParameters();

  List<SiriETUpdaterParameters> getSiriETUpdaterParameters();

  List<SiriETGooglePubsubUpdaterParameters> getSiriETGooglePubsubUpdaterParameters();

  List<SiriSXUpdaterParameters> getSiriSXUpdaterParameters();

  List<SiriVMUpdaterParameters> getSiriVMUpdaterParameters();

  List<WebsocketGtfsRealtimeUpdaterParameters> getWebsocketGtfsRealtimeUpdaterParameters();

  List<MqttGtfsRealtimeUpdaterParameters> getMqttGtfsRealtimeUpdaterParameters();

  List<VehicleParkingUpdaterParameters> getVehicleParkingUpdaterParameters();

  List<WFSNotePollingGraphUpdaterParameters> getWinkkiPollingGraphUpdaterParameters();
}
