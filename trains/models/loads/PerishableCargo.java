package unsw.trains.models.loads;

public class PerishableCargo extends Cargo {
    private int minsTillPerish;

    public PerishableCargo(String cargoId, String startStationId, String destStationId, int weight,
            int minsTillPerish) {
        super(cargoId, startStationId, destStationId, weight);
        this.minsTillPerish = minsTillPerish;
    }

    @Override
    public String getType() {
        return "PerishableCargo";
    }

    public boolean tick() {
        minsTillPerish--;
        return minsTillPerish <= 0;
    }

    public int getMinsTillPerish() {
        return minsTillPerish;
    }
}
