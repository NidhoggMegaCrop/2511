package unsw.trains.models.loads;

public abstract class Load {
    private final String loadId;
    private final String startStationId;
    private final String destStationId;
    private final int weight;

    public Load(String loadId, String startStationId, String destStationId, int weight) {
        this.loadId = loadId;
        this.startStationId = startStationId;
        this.destStationId = destStationId;
        this.weight = weight;
    }

    /**
     * Get the type of this load.
     */
    public abstract String getType();

    /**
     * Check if this load has reached its destination.
     */
    public boolean hasReachedDestination(String currentStationId) {
        return destStationId.equals(currentStationId);
    }

    public String getLoadId() {
        return loadId;
    }

    public String getStartStationId() {
        return startStationId;
    }

    public String getDestStationId() {
        return destStationId;
    }

    public int getWeight() {
        return weight;
    }
}
