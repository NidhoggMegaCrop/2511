package unsw.trains.models.trains;

import unsw.trains.models.loads.Load;
import unsw.trains.models.loads.Cargo;
import unsw.trains.models.routes.Route;
import unsw.trains.models.stations.Station;

public class BulletTrain extends Train {
    private static final double BASE_SPEED = 5.0;
    private static final int MAX_CAPACITY = 5000;

    public BulletTrain(String trainId, Route route, Station startStation) {
        super(trainId, BASE_SPEED, MAX_CAPACITY, route, startStation);
    }

    @Override
    public String getType() {
        return "BulletTrain";
    }

    @Override
    public boolean canCarryLoad(Load load) {
        return true;
    }

    @Override
    protected int calculateCargoWeight() {
        return getLoads().stream().filter(load -> load instanceof Cargo).mapToInt(Load::getWeight).sum();
    }
}
