package dkachurin.scentbird.xogamebot.model;

import java.util.UUID;

public record Game(
        UUID id,
        UUID roomId,
        CellsState cells
) {
}
