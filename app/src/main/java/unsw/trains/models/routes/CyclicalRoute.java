package unsw.trains.models.routes;

import java.util.List;

public class CyclicalRoute extends Route {
    public CyclicalRoute(List<String> stationIds, String startStationId) {
        super(stationIds, startStationId);
    }

    @Override
    public String getNextStationId() {
        int currentIdx = getCurrentIndex();
        currentIdx = (currentIdx + 1) % getStationCount();
        setCurrentIndex(currentIdx);
        return getStationIdAt(currentIdx);
    }

    @Override
    public boolean isValid() {
        return getStationCount() >= 3;
    }
}
