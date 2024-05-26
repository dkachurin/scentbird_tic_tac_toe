package dkachurin.scentbird.xogamebot.model.response;

import dkachurin.scentbird.xogamebot.model.Room;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        String secret,
        String roomName,
        List<PlayerInGameResponse> players,
        LocalDateTime createdAt,
        RoomStatus status
) {

    public static RoomResponse from(final Room createdRoom, final List<PlayerInGameResponse> players) {
        return new RoomResponse(
                createdRoom.id(),
                createdRoom.secret(),
                createdRoom.roomName(),
                players,
                createdRoom.createdAt(),
                createdRoom.status()
        );
    }
}
