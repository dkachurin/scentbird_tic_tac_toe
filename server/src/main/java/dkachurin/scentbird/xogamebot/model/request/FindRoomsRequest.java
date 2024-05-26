package dkachurin.scentbird.xogamebot.model.request;

import dkachurin.scentbird.xogamebot.model.RoomStatus;
import java.util.List;

public record FindRoomsRequest(List<RoomStatus> statuses) {}
