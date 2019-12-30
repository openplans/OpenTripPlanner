package org.opentripplanner.routing.fares;

import junit.framework.TestCase;
import org.opentripplanner.routing.algorithm.raptor.transit.TransitLayer;
import org.opentripplanner.routing.algorithm.raptor.transit.TripSchedule;
import org.opentripplanner.routing.core.Fare;
import org.opentripplanner.routing.core.Fare.FareType;
import org.opentripplanner.routing.core.WrappedCurrency;
import org.opentripplanner.routing.services.FareService;
import org.opentripplanner.transit.raptor.api.path.Path;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 * @author laurent
 */
public class MultipleFareServiceTest extends TestCase {

    private class SimpleFareService implements FareService {

        private Fare fare;

        private SimpleFareService(Fare fare) {
            this.fare = fare;
        }

        @Override
        public Fare getCost(Path<TripSchedule> path, TransitLayer transitLayer) {
            return fare;
        }
    }

    public void testAddingMultipleFareService() {

        Fare fare1 = new Fare();
        fare1.addFare(FareType.regular, new WrappedCurrency("EUR"), 100);
        FareService fs1 = new SimpleFareService(fare1);

        Fare fare2 = new Fare();
        fare2.addFare(FareType.regular, new WrappedCurrency("EUR"), 140);
        fare2.addFare(FareType.student, new WrappedCurrency("EUR"), 120);
        FareService fs2 = new SimpleFareService(fare2);

        /*
         * Note: this fare is not very representative, as you should probably always compute a
         * "regular" fare in case you want to add bike and transit fares.
         */
        Fare fare3 = new Fare();
        fare3.addFare(FareType.student, new WrappedCurrency("EUR"), 80);
        FareService fs3 = new SimpleFareService(fare3);

        AddingMultipleFareService mfs = new AddingMultipleFareService(new ArrayList<FareService>());
        Fare fare = mfs.getCost(null, null);
        assertNull(fare);

        mfs = new AddingMultipleFareService(Arrays.asList(fs1));
        fare = mfs.getCost(null, null);
        assertEquals(100, fare.getFare(FareType.regular).getCents());
        assertEquals(null, fare.getFare(FareType.student));

        mfs = new AddingMultipleFareService(Arrays.asList(fs2));
        fare = mfs.getCost(null, null);
        assertEquals(140, fare.getFare(FareType.regular).getCents());
        assertEquals(120, fare.getFare(FareType.student).getCents());

        mfs = new AddingMultipleFareService(Arrays.asList(fs1, fs2));
        fare = mfs.getCost(null, null);
        assertEquals(240, fare.getFare(FareType.regular).getCents());
        assertEquals(220, fare.getFare(FareType.student).getCents());

        mfs = new AddingMultipleFareService(Arrays.asList(fs2, fs1));
        fare = mfs.getCost(null, null);
        assertEquals(240, fare.getFare(FareType.regular).getCents());
        assertEquals(220, fare.getFare(FareType.student).getCents());

        mfs = new AddingMultipleFareService(Arrays.asList(fs1, fs3));
        fare = mfs.getCost(null, null);
        assertEquals(100, fare.getFare(FareType.regular).getCents());
        assertEquals(180, fare.getFare(FareType.student).getCents());

        mfs = new AddingMultipleFareService(Arrays.asList(fs3, fs1));
        fare = mfs.getCost(null, null);
        assertEquals(100, fare.getFare(FareType.regular).getCents());
        assertEquals(180, fare.getFare(FareType.student).getCents());

        mfs = new AddingMultipleFareService(Arrays.asList(fs1, fs2, fs3));
        fare = mfs.getCost(null, null);
        assertEquals(240, fare.getFare(FareType.regular).getCents());
        assertEquals(300, fare.getFare(FareType.student).getCents());
    }
}
