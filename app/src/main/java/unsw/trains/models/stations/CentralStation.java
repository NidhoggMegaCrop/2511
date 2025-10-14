package unsw.trains.models.stations;

import unsw.trains.models.loads.Load;
import unsw.utils.Position;

/**
 * Central Station - can store both passengers and cargo, max 8 trains.
 */

public class CentralStation extends Station {
    public CentralStation(String stationId, Position position) {
        super(stationId, position, 8);
    }

    @Override
    public boolean canStoreLoad(Load load) {
        return true;
    }

    @Override
    public String getType() {
        return "CentralStation";
    }
}
