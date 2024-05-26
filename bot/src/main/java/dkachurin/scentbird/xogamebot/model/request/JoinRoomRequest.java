package dkachurin.scentbird.xogamebot.model.request;

import dkachurin.scentbird.xogamebot.model.MarkType;
import java.util.UUID;

public record JoinRoomRequest(UUID roomId, UUID playerId, MarkType markType) {}