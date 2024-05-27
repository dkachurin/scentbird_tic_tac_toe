package dkachurin.scentbird.xogamebot.model.payload;

import dkachurin.scentbird.xogamebot.model.MarkType;
import java.util.List;
import java.util.Map;

public record GameStatusUpdatedPayload(
        Map<String, MarkType> playingArea,
        List<String> winingCells,
        String gameStatus,
        String playingAreaString
) {}
