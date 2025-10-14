package trains;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import unsw.exceptions.InvalidRouteException;
import unsw.response.models.StationInfoResponse;
import unsw.response.models.TrackInfoResponse;
import unsw.response.models.TrainInfoResponse;
import unsw.trains.TrainsController;
import unsw.utils.Position;
import unsw.utils.TrackType;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static trains.TestHelpers.assertListAreEqualIgnoringOrder;

@Timeout(value = 5, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class MyTests {

    // Test creating different types of stations and verify their properties
    @Test
    public void testDifferentStationTypes() {
        TrainsController controller = new TrainsController();

        controller.createStation("cargo1", "CargoStation", 15.0, 25.0);
        controller.createStation("central1", "CentralStation", 30.0, 40.0);

        // Check cargo station
        StationInfoResponse cargoInfo = controller.getStationInfo("cargo1");
        assertEquals("CargoStation", cargoInfo.getType());
        assertEquals(new Position(15.0, 25.0), cargoInfo.getPosition());

        // Check central station
        StationInfoResponse centralInfo = controller.getStationInfo("central1");
        assertEquals("CentralStation", centralInfo.getType());
        assertEquals(new Position(30.0, 40.0), centralInfo.getPosition());
    }

    // Test that stations initially have no trains or loads
    @Test
    public void testNewStationEmpty() {
        TrainsController controller = new TrainsController();
        controller.createStation("s1", "PassengerStation", 10.0, 10.0);

        StationInfoResponse info = controller.getStationInfo("s1");
        assertEquals(0, info.getTrains().size());
        assertEquals(0, info.getLoads().size());
    }

    // Test track creation and info
    @Test
    public void testTrackCreationAndInfo() {
        TrainsController controller = new TrainsController();

        controller.createStation("station1", "DepotStation", 0.0, 0.0);
        controller.createStation("station2", "DepotStation", 20.0, 20.0);
        controller.createTrack("track1", "station1", "station2");

        TrackInfoResponse trackInfo = controller.getTrackInfo("track1");
        assertEquals("track1", trackInfo.getTrackId());
        assertEquals(TrackType.NORMAL, trackInfo.getType());
        assertEquals(10, trackInfo.getDurability());
    }

    // Test bullet train on cyclical route - should work
    @Test
    public void testBulletTrainCyclicalRoute() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CentralStation", 10.0, 0.0);
        controller.createStation("s3", "CentralStation", 10.0, 10.0);

        controller.createTrack("t1", "s1", "s2");
        controller.createTrack("t2", "s2", "s3");
        controller.createTrack("t3", "s3", "s1");

        assertDoesNotThrow(() -> {
            controller.createTrain("bullet1", "BulletTrain", "s1", List.of("s1", "s2", "s3"));
        });

        TrainInfoResponse info = controller.getTrainInfo("bullet1");
        assertEquals("BulletTrain", info.getType());
        assertEquals("s1", info.getLocation());
    }

    // Test passenger train rejects cyclical route
    @Test
    public void testPassengerTrainRejectsCyclical() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);
        controller.createStation("s3", "PassengerStation", 10.0, 10.0);

        controller.createTrack("t1", "s1", "s2");
        controller.createTrack("t2", "s2", "s3");
        controller.createTrack("t3", "s3", "s1");

        assertThrows(InvalidRouteException.class, () -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2", "s3"));
        });
    }

    // Test invalid route - missing track
    @Test
    public void testInvalidRouteMissingTrack() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CargoStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 10.0, 0.0);
        controller.createStation("s3", "CargoStation", 20.0, 0.0);

        controller.createTrack("t1", "s1", "s2");

        assertThrows(InvalidRouteException.class, () -> {
            controller.createTrain("cargo1", "CargoTrain", "s1", List.of("s1", "s2", "s3"));
        });
    }

    // Test train starting in middle of route
    @Test
    public void testTrainStartMiddleOfRoute() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CentralStation", 15.0, 0.0);
        controller.createStation("s3", "CentralStation", 30.0, 0.0);

        controller.createTrack("t1", "s1", "s2");
        controller.createTrack("t2", "s2", "s3");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "BulletTrain", "s2", List.of("s1", "s2", "s3"));
        });

        TrainInfoResponse info = controller.getTrainInfo("train1");
        assertEquals(new Position(15.0, 0.0), info.getPosition());
        assertEquals("s2", info.getLocation());
    }

    // Test that train appears in station's train list
    @Test
    public void testStationShowsDockedTrain() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 5.0, 5.0);
        controller.createStation("s2", "PassengerStation", 15.0, 5.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        StationInfoResponse stationInfo = controller.getStationInfo("s1");
        assertEquals(1, stationInfo.getTrains().size());
        assertEquals("train1", stationInfo.getTrains().get(0).getTrainId());
    }

    // Test listing methods return correct IDs
    @Test
    public void testListMethods() {
        TrainsController controller = new TrainsController();

        controller.createStation("station1", "PassengerStation", 0.0, 0.0);
        controller.createStation("station2", "PassengerStation", 10.0, 0.0);
        controller.createTrack("track1", "station1", "station2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "station1", List.of("station1", "station2"));
        });

        assertListAreEqualIgnoringOrder(List.of("station1", "station2"), controller.listStationIds());
        assertListAreEqualIgnoringOrder(List.of("track1"), controller.listTrackIds());
        assertListAreEqualIgnoringOrder(List.of("train1"), controller.listTrainIds());
    }

    // Test trains start with no loads
    @Test
    public void testTrainStartsEmpty() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CargoStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 20.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("cargo1", "CargoTrain", "s1", List.of("s1", "s2"));
        });

        TrainInfoResponse info = controller.getTrainInfo("cargo1");
        assertNotNull(info.getLoads());
        assertEquals(0, info.getLoads().size());
    }

    @Test
    public void testSimpleMovement() {
        TrainsController controller = new TrainsController();

        // Create two stations 10km apart
        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        // Create passenger train (speed = 2 km/min) at s1
        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        // Initial position: at s1
        TrainInfoResponse info = controller.getTrainInfo("train1");
        assertEquals(new Position(0.0, 0.0), info.getPosition());
        assertEquals("s1", info.getLocation());

        // After 1 tick: should move 2km toward s2
        controller.simulate();
        info = controller.getTrainInfo("train1");
        assertEquals(new Position(2.0, 0.0), info.getPosition());
        // Should be on track, not at station
        assertNotEquals("s1", info.getLocation());
        assertNotEquals("s2", info.getLocation());

        // After 4 more ticks: should reach s2 (total 10km)
        controller.simulate(4);
        info = controller.getTrainInfo("train1");
        assertEquals(new Position(10.0, 0.0), info.getPosition());
        assertEquals("s2", info.getLocation());
    }

    @Test
    public void testCreatePassengerAtStation() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);

        controller.createPassenger("s1", "s2", "p1");

        StationInfoResponse info = controller.getStationInfo("s1");
        assertEquals(1, info.getLoads().size());
        assertEquals("p1", info.getLoads().get(0).getLoadId());
        assertEquals("Passenger", info.getLoads().get(0).getType());
    }

    @Test
    public void testCreateCargoAtStation() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CargoStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 20.0, 0.0);

        controller.createCargo("s1", "s2", "cargo1", 500);

        StationInfoResponse info = controller.getStationInfo("s1");
        assertEquals(1, info.getLoads().size());
        assertEquals("cargo1", info.getLoads().get(0).getLoadId());
        assertEquals("Cargo", info.getLoads().get(0).getType());
    }

    @Test
    public void testPassengerEmbarksOnTrain() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        controller.createPassenger("s1", "s2", "p1");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        // Initially passenger at station, train has no loads
        assertEquals(1, controller.getStationInfo("s1").getLoads().size());
        assertEquals(0, controller.getTrainInfo("train1").getLoads().size());

        // After simulate, train should pick up passenger
        controller.simulate();

        // Passenger should now be on train
        assertEquals(0, controller.getStationInfo("s1").getLoads().size());
        assertEquals(1, controller.getTrainInfo("train1").getLoads().size());
        assertEquals("p1", controller.getTrainInfo("train1").getLoads().get(0).getLoadId());
    }

    @Test
    public void testPassengerDisembarksAtDestination() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 5.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        controller.createPassenger("s1", "s2", "p1");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        // First simulate: train picks up passenger and moves
        controller.simulate();
        assertEquals(1, controller.getTrainInfo("train1").getLoads().size());

        // Continue simulating until train reaches s2
        controller.simulate(2); // Train should reach s2 (total 5km, speed 2km/min)

        // Passenger should have disembarked (removed from system)
        TrainInfoResponse trainInfo = controller.getTrainInfo("train1");
        assertEquals(0, trainInfo.getLoads().size());
    }

    @Test
    public void testCargoSlowsDownTrain() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CargoStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 20.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        // Create cargo train without cargo (speed = 3 km/min)
        assertDoesNotThrow(() -> {
            controller.createTrain("cargo1", "CargoTrain", "s1", List.of("s1", "s2"));
        });

        // Move once without cargo
        controller.simulate();
        Position posWithoutCargo = controller.getTrainInfo("cargo1").getPosition();
        // Should move 3km
        assertEquals(new Position(3.0, 0.0), posWithoutCargo);

        // Now create a new train with heavy cargo
        assertDoesNotThrow(() -> {
            controller.createTrain("cargo2", "CargoTrain", "s1", List.of("s1", "s2"));
        });

        // Add heavy cargo (1000kg = 10% slowdown)
        controller.createCargo("s1", "s2", "heavy", 1000);

        // Simulate to pick up cargo and move
        controller.simulate();

        // Train with cargo should move slower
        // Base speed 3, with 1000kg cargo: 3 * (1 - 0.0001 * 1000) = 3 * 0.9 = 2.7 km/min
        Position posWithCargo = controller.getTrainInfo("cargo2").getPosition();
        assertEquals(new Position(2.7, 0.0), posWithCargo);
    }
}
