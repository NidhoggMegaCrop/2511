package unsw.trains.models.loads;

public class Passenger extends Load {
    private static final int PASSENGER_WEIGHT = 70;

    public Passenger(String passengerId, String startStationId, String destStationId) {
        super(passengerId, startStationId, destStationId, PASSENGER_WEIGHT);
    }

    @Override
    public String getType() {
        return "Passenger";
    }
}
