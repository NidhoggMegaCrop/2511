package unsw.trains.models.loads;

public class Mechanic extends Passenger {
    public Mechanic(String passengerId, String startStationId, String destStationId) {
        super(passengerId, startStationId, destStationId);
    }

    @Override
    public String getType() {
        return "Mechanic";
    }
}
