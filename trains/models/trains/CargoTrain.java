package unsw.trains.models.trains;

import unsw.trains.models.loads.Load;
import unsw.trains.models.loads.Cargo;
import unsw.trains.models.routes.Route;
import unsw.trains.models.stations.Station;

public class CargoTrain extends Train {
    private static final double BASE_SPEED = 3.0;
    private static final int MAX_CAPACITY = 5000;

    public CargoTrain(String trainId, Route route, Station startStation) {
        super(trainId, BASE_SPEED, MAX_CAPACITY, route, startStation);
    }

    @Override
    public String getType() {
        return "CargoTrain";
    }

    @Override
    public boolean canCarryLoad(Load load) {
        return load instanceof Cargo;
    }

    @Override
    protected int calculateCargoWeight() {
        return getLoads().stream().filter(load -> load instanceof Cargo).mapToInt(Load::getWeight).sum();
    }
}
