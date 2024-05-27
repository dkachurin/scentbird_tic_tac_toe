package dkachurin.scentbird.xogamebot.model.response;

import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.MarkType;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import java.util.List;
import java.util.UUID;

public record GameResponse(
        UUID id,
        UUID roomId,
        CellsState cells,
        RoomStatus status,
        String winner,
        MarkType winnerMarkType,
        List<String> winningCells
) {
}
