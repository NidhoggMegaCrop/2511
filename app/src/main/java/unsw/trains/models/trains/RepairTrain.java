package unsw.trains.models.trains;

import unsw.trains.models.routes.Route;
import unsw.trains.models.stations.Station;

public class RepairTrain extends PassengerTrain {
    public RepairTrain(String trainId, Route route, Station startStation) {
        super(trainId, route, startStation);
    }

    @Override
    public String getType() {
        return "RepairTrain";
    }

    /**
     * Repair trains can move on broken tracks.
     */
    public boolean canMoveOnBrokenTracks() {
        return true;
    }

    /**
     * Get the number of mechanics on this train (for repair boost).
     */
    public int getMechanicCount() {
        return (int) getLoads().stream().filter(load -> load.getType().equals("Mechanic")).count();
    }
}
