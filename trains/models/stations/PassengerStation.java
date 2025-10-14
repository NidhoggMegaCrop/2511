package unsw.trains.models.stations;

import unsw.trains.models.loads.Load;
import unsw.trains.models.loads.Passenger;
import unsw.utils.Position;

/**
 * Passenger Station - can store passengers only, max 2 trains.
 */

public class PassengerStation extends Station {
    public PassengerStation(String stationId, Position position) {
        super(stationId, position, 2);
    }

    @Override
    public boolean canStoreLoad(Load load) {
        return load instanceof Passenger;
    }

    @Override
    public String getType() {
        return "PassengerStation";
    }
}
