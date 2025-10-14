package unsw.trains.models.stations;

import unsw.trains.models.loads.Load;
import unsw.trains.models.loads.Cargo;
import unsw.utils.Position;

/**
 * Cargo Station - can store cargo only, max 4 trains.
 */
public class CargoStation extends Station {
    public CargoStation(String stationId, Position position) {
        super(stationId, position, 4);
    }

    @Override
    public boolean canStoreLoad(Load load) {
        return load instanceof Cargo;
    }

    @Override
    public String getType() {
        return "CargoStation";
    }
}
