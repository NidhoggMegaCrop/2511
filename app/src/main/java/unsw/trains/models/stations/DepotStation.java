package unsw.trains.models.stations;

import unsw.trains.models.loads.Load;
import unsw.utils.Position;

/**
 * Depot Station - cannot store any loads, max 8 trains.
 */
public class DepotStation extends Station {
    public DepotStation(String stationId, Position position) {
        super(stationId, position, 8);
    }

    @Override
    public boolean canStoreLoad(Load load) {
        return false;
    }

    @Override
    public String getType() {
        return "DepotStation";
    }
}
