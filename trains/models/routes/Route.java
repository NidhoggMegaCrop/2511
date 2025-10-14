package unsw.trains.models.routes;

import java.util.ArrayList;
import java.util.List;

public abstract class Route {
    private final List<String> stationIds;
    private int currentIndex;

    public Route(List<String> stationIds, String startStationId) {
        this.stationIds = new ArrayList<>(stationIds);
        this.currentIndex = stationIds.indexOf(startStationId);
    }

    /**
     * Get the next station ID in the route.
     */
    public abstract String getNextStationId();

    /**
     * Get the current station ID.
     */
    public String getCurrentStationId() {
        return stationIds.get(currentIndex);
    }

    /**
     * Check if this is a valid route.
     */
    public abstract boolean isValid();

    /**
     * Get the list of station IDs in this route.
     */
    public List<String> getStationIds() {
        return new ArrayList<>(stationIds);
    }

    protected int getCurrentIndex() {
        return currentIndex;
    }

    protected void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    protected int getStationCount() {
        return stationIds.size();
    }

    protected String getStationIdAt(int index) {
        return stationIds.get(index);
    }
}
