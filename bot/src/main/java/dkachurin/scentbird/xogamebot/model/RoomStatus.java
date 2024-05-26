package dkachurin.scentbird.xogamebot.model;

import java.util.List;

public enum RoomStatus {
    WAITING,
    IN_PROGRESS,
    DONE;

    public static List<RoomStatus> activeStatuses() {
        return List.of(RoomStatus.WAITING, RoomStatus.IN_PROGRESS);
    }
}
