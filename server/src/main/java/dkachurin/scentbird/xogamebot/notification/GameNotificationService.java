package dkachurin.scentbird.xogamebot.notification;

import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import dkachurin.scentbird.xogamebot.model.payload.GameStatusUpdatedPayload;
import dkachurin.scentbird.xogamebot.utils.CellsUtils;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameNotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public GameNotificationService(final SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void gameStateUpdated(
            final UUID roomId,
            final RoomStatus status,
            final List<String> winingCells,
            final CellsState cellsState
    ) {
        simpMessagingTemplate.convertAndSend(
                "/topic/game-state-updated-" + roomId,
                new GameStatusUpdatedPayload(
                        cellsState.state(),
                        winingCells,
                        status.name(),
                        CellsUtils.cellsToString(cellsState))
        );
    }

}
