package unsw.trains.models.stations;

import unsw.trains.models.trains.Train;
import unsw.trains.models.loads.Load;
import unsw.utils.Position;
import java.util.ArrayList;
import java.util.List;

public abstract class Station {
    private final String stationId;
    private final Position position;
    private final int maxTrains;
    private final List<Train> dockedTrains;
    private final List<Load> loads;

    public Station(String stationId, Position position, int maxTrains) {
        this.stationId = stationId;
        this.position = position;
        this.maxTrains = maxTrains;
        this.dockedTrains = new ArrayList<>();
        this.loads = new ArrayList<>();
    }

    /**
     * Check if this station can accept another train.
     */
    public boolean canAcceptTrain() {
        return dockedTrains.size() < maxTrains;
    }

    /**
     * Add a train to this station.
     */
    public void addTrain(Train train) {
        if (canAcceptTrain()) {
            dockedTrains.add(train);
        }
    }

    /**
     * Remove a train from this station.
     */
    public void removeTrain(Train train) {
        dockedTrains.remove(train);
    }

    /**
     * Add a load to this station.
     */
    public void addLoad(Load load) {
        if (canStoreLoad(load)) {
            loads.add(load);
        }
    }

    /**
     * Remove a load from this station.
     */
    public void removeLoad(Load load) {
        loads.remove(load);
    }

    /**
     * Check if a station can store a particular type of load.
     */
    public abstract boolean canStoreLoad(Load load);

    /**
     * Get the type name of this station.
     */
    public abstract String getType();

    public String getStationId() {
        return stationId;
    }

    public Position getPosition() {
        return position;
    }

    public int getMaxTrains() {
        return maxTrains;
    }

    public List<Train> getDockedTrains() {
        return new ArrayList<>(dockedTrains);
    }

    public List<Load> getLoads() {
        return new ArrayList<>(loads);
    }
}
