package unsw.trains.models.loads;

public class Cargo extends Load {
    public Cargo(String cargoId, String startStationId, String destStationId, int weight) {
        super(cargoId, startStationId, destStationId, weight);
    }

    @Override
    public String getType() {
        return "Cargo";
    }
}
