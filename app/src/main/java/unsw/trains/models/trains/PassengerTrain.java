package unsw.trains.models.trains;

import unsw.trains.models.loads.Load;
import unsw.trains.models.loads.Passenger;
import unsw.trains.models.routes.Route;
import unsw.trains.models.stations.Station;

public class PassengerTrain extends Train {
    private static final double SPEED = 2.0;
    private static final int MAX_CAPACITY = 3500;

    public PassengerTrain(String trainId, Route route, Station startStation) {
        super(trainId, SPEED, MAX_CAPACITY, route, startStation);
    }

    @Override
    public String getType() {
        return "PassengerTrain";
    }

    @Override
    public boolean canCarryLoad(Load load) {
        return load instanceof Passenger;
    }

    @Override
    public double getCurrentSpeed() {
        return getBaseSpeed();
    }
}
