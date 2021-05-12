package org.opentripplanner.updater.alerts;

import com.google.transit.realtime.GtfsRealtime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.opentripplanner.routing.alertpatch.AlertPatch;
import org.opentripplanner.routing.services.AlertPatchService;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class AlertsUpdateHandlerTest {

    private AlertsUpdateHandler handler;

    @Spy
    private FakeAlertPatchService service;

    @Before
    public void setUp() {
        handler = new AlertsUpdateHandler();
        handler.setFeedId("1");
        handler.setAlertPatchService(service);
    }

    @Test
    public void testAlertWithTimePeriod() {
        GtfsRealtime.Alert alert = GtfsRealtime.Alert.newBuilder()
                .addActivePeriod(GtfsRealtime.TimeRange.newBuilder().setStart(10).setEnd(20).build())
                .addInformedEntity(GtfsRealtime.EntitySelector.newBuilder().setAgencyId("1"))
                .build();
        AlertPatch patch = processOneAlert(alert);
        assertEquals(new Date(10 * 1000), patch.getAlert().effectiveStartDate);
        assertEquals(new Date(20 * 1000), patch.getAlert().effectiveEndDate);
    }

    @Test
    public void testAlertStart() {
        GtfsRealtime.Alert alert = GtfsRealtime.Alert.newBuilder()
                .addActivePeriod(GtfsRealtime.TimeRange.newBuilder().setStart(10).build())
                .addInformedEntity(GtfsRealtime.EntitySelector.newBuilder().setAgencyId("1"))
                .build();
        AlertPatch patch = processOneAlert(alert);
        assertEquals(new Date(10 * 1000), patch.getAlert().effectiveStartDate);
        assertNull(patch.getAlert().effectiveEndDate);
    }

    @Test
    public void testAlertEnd() {
        GtfsRealtime.Alert alert = GtfsRealtime.Alert.newBuilder()
                .addActivePeriod(GtfsRealtime.TimeRange.newBuilder().setEnd(20).build())
                .addInformedEntity(GtfsRealtime.EntitySelector.newBuilder().setAgencyId("1"))
                .build();
        AlertPatch patch = processOneAlert(alert);
        assertNull(patch.getAlert().effectiveStartDate);
        assertEquals(new Date(20 * 1000), patch.getAlert().effectiveEndDate);
    }

    @Test
    public void testRouteFromTripDescriptor() {
        GtfsRealtime.Alert alert = GtfsRealtime.Alert.newBuilder()
                .addInformedEntity(GtfsRealtime.EntitySelector.newBuilder().setTrip(
                        GtfsRealtime.TripDescriptor.newBuilder().setRouteId("routeA")
                )).build();
        AlertPatch patch = processOneAlert(alert);
        assertEquals("routeA", patch.getRoute().getId());
    }

    private AlertPatch processOneAlert(GtfsRealtime.Alert alert) {
        GtfsRealtime.FeedMessage message = GtfsRealtime.FeedMessage.newBuilder()
                .setHeader(GtfsRealtime.FeedHeader.newBuilder().setGtfsRealtimeVersion("1.0"))
                .addEntity(GtfsRealtime.FeedEntity.newBuilder().setAlert(alert).setId("1")).build();
        handler.update(message);
        Collection<AlertPatch> patches = service.getAllAlertPatches();
        assertEquals(1, patches.size());
        return patches.iterator().next();
    }

    static abstract class FakeAlertPatchService implements AlertPatchService {

        private Collection<AlertPatch> patches = new HashSet<>();

        @Override
        public Collection<AlertPatch> getAllAlertPatches() {
            return patches;
        }

        @Override
        public void apply(AlertPatch alertPatch) {
            patches.add(alertPatch);
        }
    }
}
