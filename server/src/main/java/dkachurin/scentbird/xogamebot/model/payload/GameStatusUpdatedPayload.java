package dkachurin.scentbird.xogamebot.model.payload;

import dkachurin.scentbird.xogamebot.model.MarkType;
import java.util.Map;

public record GameStatusUpdatedPayload(
        Map<String, MarkType> playingArea,
        String gameStatus,
        String playingAreaString
) {}
