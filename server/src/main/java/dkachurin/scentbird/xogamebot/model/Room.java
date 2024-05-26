package dkachurin.scentbird.xogamebot.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Room(
        UUID id,
        String secret,
        String roomName,
        LocalDateTime createdAt,
        RoomStatus status
) {
}
