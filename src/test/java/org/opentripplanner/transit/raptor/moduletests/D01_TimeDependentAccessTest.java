package org.opentripplanner.transit.raptor.moduletests;

import static org.junit.Assert.assertEquals;
import static org.opentripplanner.transit.raptor._data.api.PathUtils.pathsToString;
import static org.opentripplanner.transit.raptor._data.transit.TestRoute.route;
import static org.opentripplanner.transit.raptor._data.transit.TestTransfer.SECONDS_IN_DAY;
import static org.opentripplanner.transit.raptor._data.transit.TestTransfer.walk;
import static org.opentripplanner.transit.raptor._data.transit.TestTripSchedule.schedule;
import static org.opentripplanner.util.time.TimeUtils.hm2time;

import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.transit.raptor.RaptorService;
import org.opentripplanner.transit.raptor._data.RaptorTestConstants;
import org.opentripplanner.transit.raptor._data.transit.TestTransitData;
import org.opentripplanner.transit.raptor._data.transit.TestTripSchedule;
import org.opentripplanner.transit.raptor.api.request.RaptorProfile;
import org.opentripplanner.transit.raptor.api.request.RaptorRequestBuilder;
import org.opentripplanner.transit.raptor.rangeraptor.configure.RaptorConfig;

/*
 * FEATURE UNDER TEST
 *
 * Raptor should take into account time restrictions on access. If the time restrictions require it,
 * there should be a wait before boarding the trip so that the access is traversed while "open".
 */
public class D01_TimeDependentAccessTest implements RaptorTestConstants {

    private final TestTransitData data = new TestTransitData();
    private final RaptorRequestBuilder<TestTripSchedule> requestBuilder =
            new RaptorRequestBuilder<>();
    private final RaptorService<TestTripSchedule> raptorService = new RaptorService<>(
            RaptorConfig.defaultConfigForTest()
    );

    @Before
    public void setup() {
        data.withRoute(
                route("R1", STOP_A, STOP_B, STOP_C, STOP_D, STOP_E)
                        .withTimetable(
                                schedule("00:10 00:15 00:20 00:25 00:30"),
                                schedule("00:15 00:20 00:25 00:30 00:35"),
                                schedule("00:20 00:25 00:30 00:35 00:40"),
                                schedule("00:25 00:30 00:35 00:40 00:45"),
                                schedule("24:10 24:15 24:20 24:25 24:30"),
                                schedule("24:15 24:20 24:25 24:30 24:35"),
                                schedule("24:20 24:25 24:30 24:35 24:40"),
                                schedule("24:25 24:30 24:35 24:40 24:45")
                        )
        );
        requestBuilder.searchParams()
                .addEgressPaths(walk(STOP_E, D1m));

        requestBuilder.searchParams()
                .earliestDepartureTime(T00_10)
                .searchWindow(Duration.ofMinutes(30))
                .timetableEnabled(true);
    }

    /*
     * There is no time restriction, all routes are found.
     */
    @Test
    public void openInWholeSearchIntervalTest() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .addAccessPaths(
                        walk(STOP_B, D2m)
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:15 0:30 ~ 5 ~ Walk 1m [00:13:00 00:31:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20 0:35 ~ 5 ~ Walk 1m [00:18:00 00:36:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25 0:40 ~ 5 ~ Walk 1m [00:23:00 00:41:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:30 0:45 ~ 5 ~ Walk 1m [00:28:00 00:46:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:15+1d 0:30+1d ~ 5 ~ Walk 1m [00:13:00+1d 00:31:00+1d 18m, cost: 2220]",
                pathsToString(response)
        );
    }

    @Test
    public void openInWholeSearchIntervalTestFullDay() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .searchWindow(Duration.ofDays(2))
                .addAccessPaths(
                        walk(STOP_B, D2m, T00_00, T01_00)
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:15 0:30 ~ 5 ~ Walk 1m [00:13:00 00:31:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20 0:35 ~ 5 ~ Walk 1m [00:18:00 00:36:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25 0:40 ~ 5 ~ Walk 1m [00:23:00 00:41:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:30 0:45 ~ 5 ~ Walk 1m [00:28:00 00:46:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:15+1d 0:30+1d ~ 5 ~ Walk 1m [00:13:00+1d 00:31:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25+1d 0:40+1d ~ 5 ~ Walk 1m [00:23:00+1d 00:41:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:30+1d 0:45+1d ~ 5 ~ Walk 1m [00:28:00+1d 00:46:00+1d 18m, cost: 2220]",
                pathsToString(response)
        );
    }

    @Test
    public void openInWholeSearchIntervalTestNextDay() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .earliestDepartureTime(SECONDS_IN_DAY + T00_10)
                .addAccessPaths(
                        walk(STOP_B, D2m, T00_00, T01_00)
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:15+1d 0:30+1d ~ 5 ~ Walk 1m [00:13:00+1d 00:31:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25+1d 0:40+1d ~ 5 ~ Walk 1m [00:23:00+1d 00:41:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:30+1d 0:45+1d ~ 5 ~ Walk 1m [00:28:00+1d 00:46:00+1d 18m, cost: 2220]",
                pathsToString(response)
        );
    }

    /*
     * The access is only open after 00:18, which means that we may arrive at the stop at 00:20 at
     * the earliest.
     */
    @Test
    public void openInSecondHalfIntervalTest() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .addAccessPaths(
                        walk(STOP_B, D2m, hm2time(0, 18), T01_00)
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:20 0:35 ~ 5 ~ Walk 1m [00:18:00 00:36:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25 0:40 ~ 5 ~ Walk 1m [00:23:00 00:41:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:30 0:45 ~ 5 ~ Walk 1m [00:28:00 00:46:00 18m, cost: 2220]\n"
                        // This bus may only be reached by waiting at the stop between 01:00 and 24:15:00,
                        // since the access only opens at 0:18, which is too late to catch the bus.
                        + "Walk 2m ~ 2 ~ BUS R1 0:15+1d 0:30+1d ~ 5 ~ Walk 1m [01:00:00 00:31:00+1d 23h31m, cost: 85800]",
                pathsToString(response)
        );
    }

    @Test
    public void openInSecondHalfIntervalTestFullDay() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .searchWindow(Duration.ofDays(2))
                .addAccessPaths(
                        walk(STOP_B, D2m, hm2time(0, 18), T01_00)
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:20 0:35 ~ 5 ~ Walk 1m [00:18:00 00:36:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25 0:40 ~ 5 ~ Walk 1m [00:23:00 00:41:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:30 0:45 ~ 5 ~ Walk 1m [00:28:00 00:46:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:15+1d 0:30+1d ~ 5 ~ Walk 1m [01:00:00 00:31:00+1d 23h31m, cost: 85800]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25+1d 0:40+1d ~ 5 ~ Walk 1m [00:23:00+1d 00:41:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:30+1d 0:45+1d ~ 5 ~ Walk 1m [00:28:00+1d 00:46:00+1d 18m, cost: 2220]",
                pathsToString(response)
        );
    }

    @Test
    public void openInSecondHalfIntervalTestNextDay() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .earliestDepartureTime(SECONDS_IN_DAY + T00_10)
                .addAccessPaths(
                        walk(STOP_B, D2m, hm2time(0, 18), T01_00)
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25+1d 0:40+1d ~ 5 ~ Walk 1m [00:23:00+1d 00:41:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:30+1d 0:45+1d ~ 5 ~ Walk 1m [00:28:00+1d 00:46:00+1d 18m, cost: 2220]",
                pathsToString(response)
        );
    }

    /*
     * The access is only open before 00:20, which means that we arrive at the stop by 00:22 at the latest.
     */
    @Test
    public void openInFirstHalfIntervalTest() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .addAccessPaths(
                        walk(STOP_B, D2m, T00_00, hm2time(0, 20))
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:15 0:30 ~ 5 ~ Walk 1m [00:13:00 00:31:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20 0:35 ~ 5 ~ Walk 1m [00:18:00 00:36:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25 0:40 ~ 5 ~ Walk 1m [00:20:00 00:41:00 21m, cost: 2400]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:15+1d 0:30+1d ~ 5 ~ Walk 1m [00:13:00+1d 00:31:00+1d 18m, cost: 2220]",
                pathsToString(response)
        );
    }

    @Test
    public void openInFirstHalfIntervalTestFullDay() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .searchWindow(Duration.ofDays(2))
                .addAccessPaths(
                        walk(STOP_B, D2m, T00_00, hm2time(0, 20))
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals( ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:15 0:30 ~ 5 ~ Walk 1m [00:13:00 00:31:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20 0:35 ~ 5 ~ Walk 1m [00:18:00 00:36:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25 0:40 ~ 5 ~ Walk 1m [00:20:00 00:41:00 21m, cost: 2400]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:15+1d 0:30+1d ~ 5 ~ Walk 1m [00:13:00+1d 00:31:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25+1d 0:40+1d ~ 5 ~ Walk 1m [00:20:00+1d 00:41:00+1d 21m, cost: 2400]",
                pathsToString(response)
        );
    }

    @Test
    public void openInFirstHalfIntervalTestNextDay() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .earliestDepartureTime(SECONDS_IN_DAY + T00_10)
                .addAccessPaths(
                        walk(STOP_B, D2m, T00_00, hm2time(0, 20))
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals( ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:15+1d 0:30+1d ~ 5 ~ Walk 1m [00:13:00+1d 00:31:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25+1d 0:40+1d ~ 5 ~ Walk 1m [00:20:00+1d 00:41:00+1d 21m, cost: 2400]",
                pathsToString(response)
        );
    }

    /*
     * The access is only open after 00:18 and before 00:20. This means that we arrive at the stop at
     * 00:20 at the earliest and 00:22 at the latest.
     */
    @Test
    public void partiallyOpenIntervalTest() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .addAccessPaths(
                        walk(STOP_B, D2m, hm2time(0, 18), hm2time(0, 20))
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:20 0:35 ~ 5 ~ Walk 1m [00:18:00 00:36:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25 0:40 ~ 5 ~ Walk 1m [00:20:00 00:41:00 21m, cost: 2400]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]",
                pathsToString(response)
        );
    }

    @Test
    public void partiallyOpenIntervalTestFullDay() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .searchWindow(Duration.ofDays(2))
                .addAccessPaths(
                        walk(STOP_B, D2m, hm2time(0, 18), hm2time(0, 20))
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:20 0:35 ~ 5 ~ Walk 1m [00:18:00 00:36:00 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25 0:40 ~ 5 ~ Walk 1m [00:20:00 00:41:00 21m, cost: 2400]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25+1d 0:40+1d ~ 5 ~ Walk 1m [00:20:00+1d 00:41:00+1d 21m, cost: 2400]",
                pathsToString(response)
        );
    }

    @Test
    public void partiallyOpenIntervalTestNextDay() {
        requestBuilder.profile(RaptorProfile.MULTI_CRITERIA)
                .searchParams()
                .earliestDepartureTime(SECONDS_IN_DAY + T00_10)
                .addAccessPaths(
                        walk(STOP_B, D2m, hm2time(0, 18), hm2time(0, 20))
                );

        var response = raptorService.route(requestBuilder.build(), data);

        assertEquals(
                ""
                        + "Walk 2m ~ 2 ~ BUS R1 0:20+1d 0:35+1d ~ 5 ~ Walk 1m [00:18:00+1d 00:36:00+1d 18m, cost: 2220]\n"
                        + "Walk 2m ~ 2 ~ BUS R1 0:25+1d 0:40+1d ~ 5 ~ Walk 1m [00:20:00+1d 00:41:00+1d 21m, cost: 2400]",
                pathsToString(response)
        );
    }
}
