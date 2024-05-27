package dkachurin.scentbird.xogamebot.model;

import java.util.List;

public record CellsWinningState(
        MarkType winnerMarkType,
        List<String> winningCells
) {
}
