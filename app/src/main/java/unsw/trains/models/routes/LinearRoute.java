package unsw.trains.models.routes;

import java.util.List;

public class LinearRoute extends Route {
    private boolean movingForward;

    public LinearRoute(List<String> stationIds, String startStationId) {
        super(stationIds, startStationId);
        int currentIdx = getCurrentIndex();
        if (currentIdx == 0) {
            movingForward = true;
        } else if (currentIdx == getStationCount() - 1) {
            movingForward = false;
        } else {
            movingForward = true;
        }
    }

    @Override
    public String getNextStationId() {
        int currentIdx = getCurrentIndex();
        if (movingForward) {
            currentIdx++;
            setCurrentIndex(currentIdx);
            if (currentIdx >= getStationCount() - 1) {
                movingForward = false;
            }
        } else {
            currentIdx--;
            setCurrentIndex(currentIdx);
            if (currentIdx <= 0) {
                movingForward = true;
            }
        }
        return getStationIdAt(currentIdx);
    }

    @Override
    public boolean isValid() {
        return getStationCount() >= 2;
    }
}
