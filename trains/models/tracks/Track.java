package unsw.trains.models.tracks;

import unsw.trains.models.stations.Station;
import unsw.utils.TrackType;

public class Track {
    private final String trackId;
    private final Station fromStation;
    private final Station toStation;
    private TrackType type;
    private int durability;

    /**
     * Constructor for a normal track.
     */
    public Track(String trackId, Station fromStation, Station toStation) {
        this(trackId, fromStation, toStation, TrackType.NORMAL, 10);
    }

    /**
     * Constructor for a breakable track.
     */
    public Track(String trackId, Station fromStation, Station toStation, boolean isBreakable) {
        this(trackId, fromStation, toStation, isBreakable ? TrackType.UNBROKEN : TrackType.NORMAL, 10);
    }

    /**
     * Base constructor.
     */
    private Track(String trackId, Station fromStation, Station toStation, TrackType type, int durability) {
        this.trackId = trackId;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.type = type;
        this.durability = durability;
    }

    /**
     * Check if this track connects the given two stations.
     */
    public boolean connectsStations(String stationId1, String stationId2) {
        return (fromStation.getStationId().equals(stationId1) && toStation.getStationId().equals(stationId2))
                || (fromStation.getStationId().equals(stationId2) && toStation.getStationId().equals(stationId1));
    }

    /**
     * Get the other station connected by this track.
     */
    public Station getOtherStation(Station currentStation) {
        if (currentStation.equals(fromStation)) {
            return toStation;
        } else if (currentStation.equals(toStation)) {
            return fromStation;
        }
        return null;
    }

    /**
     * Damage this track when a train uses it.
     */
    public void damage(int amount) {
        if (type == TrackType.UNBROKEN || type == TrackType.BROKEN) {
            durability = Math.max(0, durability - amount);
            if (durability == 0) {
                type = TrackType.BROKEN;
            }
        }
    }

    /**
     * Repair this track by a certain amount.
     */
    public void repair(int amount) {
        if (type == TrackType.BROKEN || type == TrackType.UNBROKEN) {
            durability = Math.min(10, durability + amount);
            if (durability == 10) {
                type = TrackType.UNBROKEN;
            }
        }
    }

    /**
     * Check if trains can use this track.
     */
    public boolean isUsable() {
        return type != TrackType.BROKEN;
    }

    public String getTrackId() {
        return trackId;
    }

    public Station getFromStation() {
        return fromStation;
    }

    public Station getToStation() {
        return toStation;
    }

    public TrackType getType() {
        return type;
    }

    public int getDurability() {
        return durability;
    }
}
