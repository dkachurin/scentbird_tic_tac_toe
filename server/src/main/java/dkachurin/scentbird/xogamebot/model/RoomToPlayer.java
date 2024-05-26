package dkachurin.scentbird.xogamebot.model;

import java.util.UUID;

public record RoomToPlayer(
        UUID playerId,
        UUID roomId,
        MarkType markType
) {
}
