package unsw.trains.models.trains;

import unsw.trains.models.loads.Load;
import unsw.trains.models.routes.Route;
import unsw.trains.models.stations.Station;
import unsw.utils.Position;
import java.util.ArrayList;
import java.util.List;

public abstract class Train {
    private final String trainId;
    private final double baseSpeed;
    private final int maxCapacityKg;
    private final Route route;
    private Position position;
    private String currentLocation;
    private final List<Load> loads;

    public Train(String trainId, double baseSpeed, int maxCapacityKg, Route route, Station startStation) {
        this.trainId = trainId;
        this.baseSpeed = baseSpeed;
        this.maxCapacityKg = maxCapacityKg;
        this.route = route;
        this.position = startStation.getPosition();
        this.currentLocation = startStation.getStationId();
        this.loads = new ArrayList<>();
    }

    /**
     * Get the type name of this train.
     */
    public abstract String getType();

    /**
     * Check if this train can carry a particular type of load.
     */
    public abstract boolean canCarryLoad(Load load);

    /**
     * Calculate the current speed of the train, accounting for cargo slowdown.
     */
    public double getCurrentSpeed() {
        int totalCargoWeight = calculateCargoWeight();
        double slowdownFactor = 1.0 - (0.0001 * totalCargoWeight);
        return baseSpeed * slowdownFactor;
    }

    /**
     * Calculate total cargo weight on the train (not including passengers for slowdown).
     */
    protected int calculateCargoWeight() {
        return 0;
    }

    /**
     * Check if this train has capacity for a load.
     */
    public boolean hasCapacityFor(Load load) {
        int currentWeight = loads.stream().mapToInt(Load::getWeight).sum();
        return currentWeight + load.getWeight() <= maxCapacityKg;
    }

    /**
     * Add a load to this train.
     */
    public void addLoad(Load load) {
        if (canCarryLoad(load) && hasCapacityFor(load)) {
            loads.add(load);
        }
    }

    /**
     * Remove a load from this train.
     */
    public void removeLoad(Load load) {
        loads.remove(load);
    }

    /**
     * Move the train towards its next station.
     * Returns true if train reached the station, false if still on track.
     */
    public boolean move(Station nextStation) {
        Position nextPos = nextStation.getPosition();
        double speed = getCurrentSpeed();

        if (position.isInBound(nextPos, speed)) {
            position = nextPos;
            return true;
        } else {
            position = position.calculateNewPosition(nextPos, speed);
            return false;
        }
    }

    /**
     * Get the next station ID from the route.
     */
    public String getNextStationId() {
        return route.getNextStationId();
    }

    /**
     * Check if this train is currently at a station.
     */
    public boolean isAtStation() {
        // Location starts with "station-" for stations, "track-" for tracks
        return currentLocation != null && !currentLocation.startsWith("track-");
    }

    public String getTrainId() {
        return trainId;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public int getMaxCapacityKg() {
        return maxCapacityKg;
    }

    public Route getRoute() {
        return route;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String location) {
        this.currentLocation = location;
    }

    public List<Load> getLoads() {
        return new ArrayList<>(loads);
    }
}
