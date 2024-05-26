package dkachurin.scentbird.xogamebot.model.response;

import dkachurin.scentbird.xogamebot.model.MarkType;
import java.util.UUID;

public record PlayerInGameResponse(
        UUID id,

        MarkType markType
) {}
