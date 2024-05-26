package dkachurin.scentbird.xogamebot.model.response;

import dkachurin.scentbird.xogamebot.model.Room;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RoomsListResponse(List<RoomResponse> rooms) {
    public static RoomsListResponse empty() {
        return new RoomsListResponse(List.of());
    }

    public static RoomsListResponse from(
            final List<Room> rooms,
            final Map<UUID, List<PlayerInGameResponse>> roomIdToPlayers
    ) {
        final List<RoomResponse> roomResponses = rooms.stream()
                .map(room -> RoomResponse.from(room, roomIdToPlayers.getOrDefault(room.id(), List.of())))
                .toList();

        return new RoomsListResponse(roomResponses);
    }
}
