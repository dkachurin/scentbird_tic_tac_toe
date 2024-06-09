package dkachurin.scentbird.xogamebot.notification;

import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import dkachurin.scentbird.xogamebot.model.payload.GameStatusUpdatedPayload;
import dkachurin.scentbird.xogamebot.service.GameService;
import dkachurin.scentbird.xogamebot.utils.CellsUtils;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameNotificationService.class);

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
        logger.debug(
                "gameStateUpdated: sending notification. roomId {}, status {}, winingCells {}, cellsState {}", 
                roomId, status, winingCells, cellsState
        );

        final String destination = "/topic/game-state-updated-" + roomId;
        final GameStatusUpdatedPayload payload = new GameStatusUpdatedPayload(
                cellsState.state(),
                winingCells,
                status.name(),
                CellsUtils.cellsToString(cellsState));
        simpMessagingTemplate.convertAndSend(destination, payload);
        logger.debug("gameStateUpdated: sent notification. destination {}, payload {}", destination, payload);
    }

}
