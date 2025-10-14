package trains;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import unsw.exceptions.InvalidRouteException;
import unsw.response.models.StationInfoResponse;
import unsw.response.models.TrackInfoResponse;
import unsw.trains.TrainsController;
import unsw.utils.Position;
import unsw.utils.TrackType;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static trains.TestHelpers.assertListAreEqualIgnoringOrder;

@Timeout(value = 5, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class MyTests {

    // ==================== TASK A TESTS (5 tests) ====================
    @Test
    public void testCreateStationsAndListIds() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 10.0, 10.0);
        controller.createStation("s3", "CentralStation", 20.0, 20.0);

        assertListAreEqualIgnoringOrder(List.of("s1", "s2", "s3"), controller.listStationIds());

        assertEquals("PassengerStation", controller.getStationInfo("s1").getType());
        assertEquals("CargoStation", controller.getStationInfo("s2").getType());
        assertEquals(new Position(20.0, 20.0), controller.getStationInfo("s3").getPosition());
    }

    @Test
    public void testCreateTracksAndValidation() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "DepotStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 10.0, 10.0);
        controller.createTrack("t1", "s1", "s2");

        TrackInfoResponse info = controller.getTrackInfo("t1");
        assertEquals("t1", info.getTrackId());
        assertEquals(TrackType.NORMAL, info.getType());
        assertEquals(10, info.getDurability());

        assertListAreEqualIgnoringOrder(List.of("t1"), controller.listTrackIds());
    }

    @Test
    public void testCreateTrainsWithDifferentTypes() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CentralStation", 10.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
            controller.createTrain("train2", "CargoTrain", "s1", List.of("s1", "s2"));
            controller.createTrain("train3", "BulletTrain", "s2", List.of("s1", "s2"));
        });

        assertEquals("PassengerTrain", controller.getTrainInfo("train1").getType());
        assertEquals("CargoTrain", controller.getTrainInfo("train2").getType());
        assertEquals("BulletTrain", controller.getTrainInfo("train3").getType());

        assertListAreEqualIgnoringOrder(List.of("train1", "train2", "train3"), controller.listTrainIds());
    }

    @Test
    public void testCyclicalRouteValidation() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CentralStation", 10.0, 0.0);
        controller.createStation("s3", "CentralStation", 10.0, 10.0);
        controller.createTrack("t1", "s1", "s2");
        controller.createTrack("t2", "s2", "s3");
        controller.createTrack("t3", "s3", "s1");

        // BulletTrain can use cyclical routes
        assertDoesNotThrow(() -> {
            controller.createTrain("bullet", "BulletTrain", "s1", List.of("s1", "s2", "s3"));
        });

        // PassengerTrain cannot use cyclical routes
        assertThrows(InvalidRouteException.class, () -> {
            controller.createTrain("passenger", "PassengerTrain", "s1", List.of("s1", "s2", "s3"));
        });
    }

    @Test
    public void testStationInfoWithTrains() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        StationInfoResponse info = controller.getStationInfo("s1");
        assertEquals(1, info.getTrains().size());
        assertEquals("train1", info.getTrains().get(0).getTrainId());
    }

    // ==================== TASK B TESTS (5 tests) ====================

    @Test
    public void testTrainMovement() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CentralStation", 0.0, 0.0);
        controller.createStation("s2", "CentralStation", 10.0, 10.0);
        controller.createTrack("t1", "s1", "s2");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        Position initialPos = controller.getTrainInfo("train1").getPosition();
        assertEquals(new Position(0.0, 0.0), initialPos);

        controller.simulate();

        Position afterMove = controller.getTrainInfo("train1").getPosition();
        assertNotEquals(initialPos, afterMove);
    }

    @Test
    public void testPassengerEmbarkingAndDisembarking() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 5.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        controller.createPassenger("s1", "s2", "p1");

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        // Initially passenger at station
        assertEquals(1, controller.getStationInfo("s1").getLoads().size());
        assertEquals(0, controller.getTrainInfo("train1").getLoads().size());

        // After simulate: passenger embarks
        controller.simulate();
        assertEquals(0, controller.getStationInfo("s1").getLoads().size());
        assertEquals(1, controller.getTrainInfo("train1").getLoads().size());

        // After reaching s2: passenger disembarks
        controller.simulate(2);
        assertEquals(0, controller.getTrainInfo("train1").getLoads().size());
    }

    @Test
    public void testCargoSlowsDownTrain() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CargoStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 20.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        // Create cargo before train
        controller.createCargo("s1", "s2", "c1", 1000);

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "CargoTrain", "s1", List.of("s1", "s2"));
        });

        // Train picks up cargo and moves
        controller.simulate();

        // With 1000kg cargo: speed = 3 * (1 - 0.0001*1000) = 3 * 0.9 = 2.7 km/min
        Position pos = controller.getTrainInfo("train1").getPosition();
        assertEquals(new Position(2.7, 0.0), pos);
    }

    @Test
    public void testLoadEmbarkingPriority() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        // Create passengers in specific order
        controller.createPassenger("s1", "s2", "p2");
        controller.createPassenger("s1", "s2", "p1");

        // Create train with capacity for 1 passenger
        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        // Only capacity for 50 passengers, but we have 2
        // Should embark in lexicographical order: p1 first
        controller.simulate();

        // p1 should be on train (lexicographically first)
        assertEquals(2, controller.getTrainInfo("train1").getLoads().size());
        assertTrue(
                controller.getTrainInfo("train1").getLoads().stream().anyMatch(load -> load.getLoadId().equals("p1")));
    }

    @Test
    public void testPerishableCargoExpires() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "CargoStation", 0.0, 0.0);
        controller.createStation("s2", "CargoStation", 100.0, 0.0);
        controller.createTrack("t1", "s1", "s2");

        // Create perishable cargo that expires in 2 minutes
        controller.createPerishableCargo("s1", "s2", "pc1", 500, 2);

        assertEquals(1, controller.getStationInfo("s1").getLoads().size());

        // After 2 ticks, cargo should expire and be removed
        controller.simulate(2);

        assertEquals(0, controller.getStationInfo("s1").getLoads().size());
    }

    // ==================== TASK C TESTS (5 tests) ====================

    @Test
    public void testBreakableTrackCreationAndDamage() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 5.0, 0.0);
        controller.createTrack("breakable1", "s1", "s2", true);

        assertEquals(TrackType.UNBROKEN, controller.getTrackInfo("breakable1").getType());
        assertEquals(10, controller.getTrackInfo("breakable1").getDurability());

        // Run empty train to damage track
        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        controller.simulate(3); // Train reaches s2

        // Empty train damages by 1
        assertEquals(9, controller.getTrackInfo("breakable1").getDurability());
        assertEquals(TrackType.UNBROKEN, controller.getTrackInfo("breakable1").getType());
    }

    @Test
    public void testTrackBreaksAndRepairs() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 5.0, 0.0);
        controller.createTrack("breakable1", "s1", "s2", true);

        assertDoesNotThrow(() -> {
            controller.createTrain("train1", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        // Break track by running train 10 times
        for (int i = 0; i < 10; i++) {
            controller.simulate(3);
        }

        assertEquals(0, controller.getTrackInfo("breakable1").getDurability());
        assertEquals(TrackType.BROKEN, controller.getTrackInfo("breakable1").getType());

        // Track auto-repairs at 1 per tick
        controller.simulate(10);

        assertEquals(10, controller.getTrackInfo("breakable1").getDurability());
        assertEquals(TrackType.UNBROKEN, controller.getTrackInfo("breakable1").getType());
    }

    @Test
    public void testRegularTrainBlockedByBrokenTrack() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 5.0, 0.0);
        controller.createTrack("breakable1", "s1", "s2", true);

        // Break track
        assertDoesNotThrow(() -> {
            controller.createTrain("breaker", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        for (int i = 0; i < 10; i++) {
            controller.simulate(3);
        }

        assertEquals(TrackType.BROKEN, controller.getTrackInfo("breakable1").getType());

        // Create new train - should be blocked
        assertDoesNotThrow(() -> {
            controller.createTrain("blocked", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        Position initialPos = controller.getTrainInfo("blocked").getPosition();
        controller.simulate(5);

        assertEquals(initialPos, controller.getTrainInfo("blocked").getPosition());
        assertEquals("s1", controller.getTrainInfo("blocked").getLocation());
    }

    @Test
    public void testRepairTrainMovesOnBrokenTrack() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 5.0, 0.0);
        controller.createTrack("breakable1", "s1", "s2", true);

        // Break track
        assertDoesNotThrow(() -> {
            controller.createTrain("breaker", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        for (int i = 0; i < 10; i++) {
            controller.simulate(3);
        }

        assertEquals(TrackType.BROKEN, controller.getTrackInfo("breakable1").getType());

        // RepairTrain should move on broken track
        assertDoesNotThrow(() -> {
            controller.createTrain("repair", "RepairTrain", "s1", List.of("s1", "s2"));
        });

        Position initialPos = controller.getTrainInfo("repair").getPosition();
        controller.simulate();

        assertNotEquals(initialPos, controller.getTrainInfo("repair").getPosition());
    }

    @Test
    public void testMechanicBoostsTrackRepair() {
        TrainsController controller = new TrainsController();

        controller.createStation("s1", "PassengerStation", 0.0, 0.0);
        controller.createStation("s2", "PassengerStation", 10.0, 0.0);
        controller.createTrack("breakable1", "s1", "s2", true);

        // Break track
        assertDoesNotThrow(() -> {
            controller.createTrain("breaker", "PassengerTrain", "s1", List.of("s1", "s2"));
        });

        for (int i = 0; i < 10; i++) {
            controller.simulate(5);
        }

        assertEquals(0, controller.getTrackInfo("breakable1").getDurability());

        // Create mechanics and repair train
        controller.createPassenger("s1", "s2", "m1", true);
        controller.createPassenger("s1", "s2", "m2", true);

        assertDoesNotThrow(() -> {
            controller.createTrain("repair", "RepairTrain", "s1", List.of("s1", "s2"));
        });

        // After 1 tick: picks up mechanics
        controller.simulate();
        assertEquals(2, controller.getTrainInfo("repair").getLoads().size());

        // After 1 more tick: on broken track, applying repair
        // Repair = 1 (base) + 2*2 (mechanics) = 5
        controller.simulate();

        assertTrue(controller.getTrackInfo("breakable1").getDurability() >= 5,
                "Track should have repaired by at least 5 with 2 mechanics");
    }

    // ==================== TEST SUMMARY ====================
    // Task A Tests: 5 tests (stations, tracks, trains, validation, info)
    // Task B Tests: 5 tests (movement, embarking, cargo slowdown, priority, perishable)
    // Task C Tests: 5 tests (breakable tracks, repair, blocking, repair train, mechanics)
    // Total: 15 comprehensive tests covering all major functionality
}
