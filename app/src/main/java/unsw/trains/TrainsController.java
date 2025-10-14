package unsw.trains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import unsw.exceptions.InvalidRouteException;
import unsw.response.models.*;

import unsw.trains.models.stations.*;
import unsw.trains.models.tracks.Track;
import unsw.trains.models.trains.*;
import unsw.trains.models.loads.*;
import unsw.trains.models.routes.*;

import unsw.utils.Position;
import unsw.utils.TrackType;

public class TrainsController {
    private final Map<String, Station> stations;
    private final Map<String, Track> tracks;
    private final Map<String, Train> trains;

    /**
     * Helper class to store track and load weight for damage calculation.
     * This is needed because we calculate damage AFTER disembarking, but we need
     * the weight from BEFORE disembarking.
     */
    private static class TrackDamageInfo {
        private final Track track;
        private final int loadWeight;

        TrackDamageInfo(Track track, int loadWeight) {
            this.track = track;
            this.loadWeight = loadWeight;
        }
    }

    public TrainsController() {
        this.stations = new HashMap<>();
        this.tracks = new HashMap<>();
        this.trains = new HashMap<>();
    }

    public void createStation(String stationId, String type, double x, double y) {
        Position position = new Position(x, y);
        Station station;

        switch (type) {
        case "PassengerStation":
            station = new PassengerStation(stationId, position);
            break;
        case "CargoStation":
            station = new CargoStation(stationId, position);
            break;
        case "CentralStation":
            station = new CentralStation(stationId, position);
            break;
        case "DepotStation":
            station = new DepotStation(stationId, position);
            break;
        default:
            throw new IllegalArgumentException("Unknown station type: " + type);
        }

        stations.put(stationId, station);
    }

    public void createTrack(String trackId, String fromStationId, String toStationId) {
        Station fromStation = stations.get(fromStationId);
        Station toStation = stations.get(toStationId);
        Track track = new Track(trackId, fromStation, toStation);
        tracks.put(trackId, track);
    }

    public void createTrack(String trackId, String fromStationId, String toStationId, boolean isBreakable) {
        Station fromStation = stations.get(fromStationId);
        Station toStation = stations.get(toStationId);
        Track track = new Track(trackId, fromStation, toStation, isBreakable);
        tracks.put(trackId, track);
    }

    public void createTrain(String trainId, String type, String stationId, List<String> route)
            throws InvalidRouteException {
        Station startStation = stations.get(stationId);

        Route trainRoute = createAndValidateRoute(type, route, stationId);

        Train train;
        switch (type) {
        case "PassengerTrain":
            train = new PassengerTrain(trainId, trainRoute, startStation);
            break;
        case "CargoTrain":
            train = new CargoTrain(trainId, trainRoute, startStation);
            break;
        case "BulletTrain":
            train = new BulletTrain(trainId, trainRoute, startStation);
            break;
        case "RepairTrain":
            train = new RepairTrain(trainId, trainRoute, startStation);
            break;
        default:
            throw new IllegalArgumentException("Unknown train type: " + type);
        }

        trains.put(trainId, train);
        startStation.addTrain(train);
    }

    /**
     * Create and validate a route based on train type.
     */
    private Route createAndValidateRoute(String trainType, List<String> routeStations, String startStationId)
            throws InvalidRouteException {
        boolean isCyclical = isRouteCyclical(routeStations);

        Route route;
        if (isCyclical) {
            if (!trainType.equals("BulletTrain")) {
                throw new InvalidRouteException(trainType + " cannot use cyclical routes. Only BulletTrain can.");
            }
            route = new CyclicalRoute(routeStations, startStationId);
        } else {
            route = new LinearRoute(routeStations, startStationId);
        }

        if (!route.isValid()) {
            throw new InvalidRouteException("Invalid route structure");
        }

        validateRouteHasTracks(routeStations, isCyclical);

        return route;
    }

    /**
     * Check if a route is cyclical (has track between first and last stations).
     */
    private boolean isRouteCyclical(List<String> routeStations) {
        if (routeStations.size() < 3) {
            return false;
        }
        String firstStation = routeStations.get(0);
        String lastStation = routeStations.get(routeStations.size() - 1);
        return hasTrackBetween(firstStation, lastStation);
    }

    /**
    * Validate that all required tracks exist for a route.
    */
    private void validateRouteHasTracks(List<String> routeStations, boolean isCyclical) throws InvalidRouteException {
        for (int i = 0; i < routeStations.size() - 1; i++) {
            if (!hasTrackBetween(routeStations.get(i), routeStations.get(i + 1))) {
                throw new InvalidRouteException(
                        "No track between " + routeStations.get(i) + " and " + routeStations.get(i + 1));
            }
        }

        if (isCyclical) {
            String first = routeStations.get(0);
            String last = routeStations.get(routeStations.size() - 1);
            if (!hasTrackBetween(first, last)) {
                throw new InvalidRouteException("No track between first and last station for cyclical route");
            }
        }
    }

    /**
     * Check if a track exists between two stations.
     */
    private boolean hasTrackBetween(String stationId1, String stationId2) {
        return tracks.values().stream().anyMatch(track -> track.connectsStations(stationId1, stationId2));
    }

    public List<String> listStationIds() {
        return new ArrayList<>(stations.keySet());
    }

    public List<String> listTrackIds() {
        return new ArrayList<>(tracks.keySet());
    }

    public List<String> listTrainIds() {
        return new ArrayList<>(trains.keySet());
    }

    public TrainInfoResponse getTrainInfo(String trainId) {
        Train train = trains.get(trainId);
        if (train == null) {
            return null;
        }

        List<LoadInfoResponse> loadResponses = train.getLoads().stream()
                .map(load -> new LoadInfoResponse(load.getLoadId(), load.getType())).collect(Collectors.toList());

        return new TrainInfoResponse(train.getTrainId(), train.getCurrentLocation(), train.getType(),
                train.getPosition(), loadResponses);
    }

    public StationInfoResponse getStationInfo(String stationId) {
        Station station = stations.get(stationId);
        if (station == null) {
            return null;
        }

        List<LoadInfoResponse> loadResponses = station.getLoads().stream()
                .map(load -> new LoadInfoResponse(load.getLoadId(), load.getType())).collect(Collectors.toList());

        List<TrainInfoResponse> trainResponses = station.getDockedTrains().stream()
                .map(train -> getTrainInfo(train.getTrainId())).collect(Collectors.toList());

        return new StationInfoResponse(station.getStationId(), station.getType(), station.getPosition(), loadResponses,
                trainResponses);
    }

    public TrackInfoResponse getTrackInfo(String trackId) {
        Track track = tracks.get(trackId);
        if (track == null) {
            return null;
        }

        return new TrackInfoResponse(track.getTrackId(), track.getFromStation().getStationId(),
                track.getToStation().getStationId(), track.getType(), track.getDurability());
    }

    public void simulate() {
        // First: Embark loads at stations
        embarkLoadsAtStations();

        // Second: Move all trains and record track usage
        // IMPORTANT: Record load weight BEFORE disembarking for damage calculation
        List<Train> trainsList = new ArrayList<>(trains.values());
        trainsList.sort((t1, t2) -> t1.getTrainId().compareTo(t2.getTrainId()));

        Map<Train, TrackDamageInfo> trainTrackMovements = new HashMap<>();

        for (Train train : trainsList) {
            // Record weight before movement (for damage calculation later)
            int loadWeightBeforeMove = train.getLoads().stream().mapToInt(Load::getWeight).sum();

            Track usedTrack = moveTrain(train);

            if (usedTrack != null) {
                trainTrackMovements.put(train, new TrackDamageInfo(usedTrack, loadWeightBeforeMove));
            }
        }

        // Third: Disembark loads that reached their destination
        disembarkLoadsFromTrains();

        // Fourth: Damage tracks using weight from BEFORE disembarking
        damageTracksFromTrainMovement(trainTrackMovements);

        // Fifth: Repair broken tracks
        repairBrokenTracks();
    }

    /**
     * Move a single train toward its next station.
     * Returns the track used if the train completed traversal, null otherwise.
     */
    private Track moveTrain(Train train) {
        String currentLocation = train.getCurrentLocation();
        boolean isAtStation = stations.containsKey(currentLocation);

        if (!isAtStation) {
            return moveTrainOnTrack(train);
        } else {
            return departFromStation(train);
        }
    }

    /**
     * Handle train departing from a station.
     * Returns the track used if train reached the destination station.
     */
    private Track departFromStation(Train train) {
        Station currentStation = stations.get(train.getCurrentLocation());
        String nextStationId = train.getNextStationId();
        Station nextStation = stations.get(nextStationId);

        if (!nextStation.canAcceptTrain()) {
            return null;
        }

        // Check if the track to next station exists
        Track track = findTrackBetween(train.getCurrentLocation(), nextStationId);
        if (track == null) {
            return null;
        }

        // Check if track is broken and train cannot use it
        if (track.getType() == TrackType.BROKEN && !canMoveOnBrokenTrack(train)) {
            return null; // Train waits at station
        }

        boolean reachedStation = train.move(nextStation);

        if (reachedStation) {
            // Train reached destination - this is when we damage the track
            currentStation.removeTrain(train);
            nextStation.addTrain(train);
            train.setCurrentLocation(nextStationId);
            return track; // Return track for damage calculation
        } else {
            // Train is now on the track
            currentStation.removeTrain(train);
            train.setCurrentLocation(track.getTrackId());
            return null; // No damage yet, train still moving
        }
    }

    /**
     * Handle train continuing to move on a track.
     * Returns the track if train reached station (for damage calculation).
     */
    private Track moveTrainOnTrack(Train train) {
        String currentStationInRoute = train.getRoute().getCurrentStationId();
        Station destinationStation = stations.get(currentStationInRoute);

        if (!destinationStation.canAcceptTrain()) {
            return null;
        }

        // Get the track the train is currently on
        Track currentTrack = tracks.get(train.getCurrentLocation());

        boolean reachedStation = train.move(destinationStation);

        if (reachedStation) {
            // Train reached destination - damage the track it was on
            destinationStation.addTrain(train);
            train.setCurrentLocation(currentStationInRoute);
            return currentTrack; // Return the track for damage calculation
        }

        return null; // Still moving on track
    }

    /**
     * Check if a train can move on broken tracks.
     */
    private boolean canMoveOnBrokenTrack(Train train) {
        return train instanceof RepairTrain && ((RepairTrain) train).canMoveOnBrokenTracks();
    }

    /**
    * Find a track between two stations.
    */
    private Track findTrackBetween(String stationId1, String stationId2) {
        return tracks.values().stream().filter(track -> track.connectsStations(stationId1, stationId2)).findFirst()
                .orElse(null);
    }

    /**
     * Handle embarking of loads at all stations.
     */
    private void embarkLoadsAtStations() {
        for (Station station : stations.values()) {
            List<Train> trainsAtStation = station.getDockedTrains();
            trainsAtStation.sort((t1, t2) -> t1.getTrainId().compareTo(t2.getTrainId()));

            for (Train train : trainsAtStation) {
                embarkLoads(train, station);
            }
        }
    }

    /**
     * Handle disembarking of loads from all trains at their destination.
     */
    private void disembarkLoadsFromTrains() {
        for (Train train : trains.values()) {
            String currentLocation = train.getCurrentLocation();

            // Only disembark if train is at a station
            if (stations.containsKey(currentLocation)) {
                Station station = stations.get(currentLocation);
                disembarkLoads(train, station);
            }
        }
    }

    /**
    * Disembark all loads from a train that have reached their destination.
    */
    private void disembarkLoads(Train train, Station station) {
        List<Load> loadsToRemove = new ArrayList<>();

        for (Load load : train.getLoads()) {
            if (load.hasReachedDestination(station.getStationId())) {
                loadsToRemove.add(load);
            }
        }

        // Remove all disembarking loads
        for (Load load : loadsToRemove) {
            train.removeLoad(load);
            // Load is removed from system (not added back to station)
        }
    }

    /**
     * Embark loads from station onto train if appropriate.
     */
    private void embarkLoads(Train train, Station station) {
        // Get all loads at station, sorted lexicographically
        List<Load> loadsAtStation = new ArrayList<>(station.getLoads());
        loadsAtStation.sort((l1, l2) -> l1.getLoadId().compareTo(l2.getLoadId()));

        List<Load> loadsToEmbark = new ArrayList<>();

        // Try to embark each load
        for (Load load : loadsAtStation) {
            // Check if train can carry this type of load
            if (!train.canCarryLoad(load)) {
                continue;
            }

            // Check if train has capacity
            if (!train.hasCapacityFor(load)) {
                continue;
            }

            // Check if train will visit the load's destination
            if (!trainWillVisitStation(train, load.getDestStationId())) {
                continue;
            }

            // All checks passed - embark this load
            loadsToEmbark.add(load);
        }

        // Embark all selected loads
        for (Load load : loadsToEmbark) {
            station.removeLoad(load);
            train.addLoad(load);
        }
    }

    /**
    * Check if a train's route will visit a given station.
    */
    private boolean trainWillVisitStation(Train train, String destinationStationId) {
        List<String> routeStations = train.getRoute().getStationIds();
        return routeStations.contains(destinationStationId);
    }

    /**
     * Damage tracks based on train movements.
     * Uses weight from BEFORE disembarking.
     */
    private void damageTracksFromTrainMovement(Map<Train, TrackDamageInfo> trainTrackMovements) {
        for (Map.Entry<Train, TrackDamageInfo> entry : trainTrackMovements.entrySet()) {
            Train train = entry.getKey();
            TrackDamageInfo damageInfo = entry.getValue();

            // RepairTrains don't damage tracks
            if (train instanceof RepairTrain) {
                continue;
            }

            // Calculate damage based on load weight from BEFORE disembarking
            int totalWeight = damageInfo.loadWeight;

            int damage = 1 + (int) Math.ceil((double) totalWeight / 1000.0);
            damageInfo.track.damage(damage);
        }
    }

    /**
     * Repair broken tracks.
     * First apply mechanic boosts, then apply base repair to all broken tracks.
     */
    private void repairBrokenTracks() {
        // First, apply mechanic repair boosts for RepairTrains ON broken tracks
        for (Train train : trains.values()) {
            if (train instanceof RepairTrain) {
                RepairTrain repairTrain = (RepairTrain) train;
                String currentLocation = repairTrain.getCurrentLocation();

                // Check if repair train is on a track (not at station)
                Track track = tracks.get(currentLocation);
                if (track != null && track.getType() == TrackType.BROKEN) {
                    // Apply mechanic boost (2 per mechanic)
                    int mechanicCount = repairTrain.getMechanicCount();
                    if (mechanicCount > 0) {
                        int repairBoost = mechanicCount * 2;
                        track.repair(repairBoost);
                    }
                }
            }
        }

        // Then, apply base repair to all broken tracks (1 per tick)
        for (Track track : tracks.values()) {
            if (track.getType() == TrackType.BROKEN) {
                track.repair(1); // Base repair rate
            }
        }
    }

    /**
     * Simulate for the specified number of minutes. You should NOT modify
     * this function.
     */
    public void simulate(int numberOfMinutes) {
        for (int i = 0; i < numberOfMinutes; i++) {
            simulate();
        }
    }

    public void createPassenger(String startStationId, String destStationId, String passengerId) {
        Station startStation = stations.get(startStationId);
        Passenger passenger = new Passenger(passengerId, startStationId, destStationId);
        startStation.addLoad(passenger);
    }

    public void createPassenger(String startStationId, String destStationId, String passengerId, boolean isMechanic) {
        Station startStation = stations.get(startStationId);
        Passenger passenger;

        if (isMechanic) {
            passenger = new Mechanic(passengerId, startStationId, destStationId);
        } else {
            passenger = new Passenger(passengerId, startStationId, destStationId);
        }

        startStation.addLoad(passenger);
    }

    public void createCargo(String startStationId, String destStationId, String cargoId, int weight) {
        Station startStation = stations.get(startStationId);
        Cargo cargo = new Cargo(cargoId, startStationId, destStationId, weight);
        startStation.addLoad(cargo);
    }

    public void createPerishableCargo(String startStationId, String destStationId, String cargoId, int weight,
            int minsTillPerish) {
        Station startStation = stations.get(startStationId);
        PerishableCargo perishableCargo = new PerishableCargo(cargoId, startStationId, destStationId, weight,
                minsTillPerish);
        startStation.addLoad(perishableCargo);
    }
}
