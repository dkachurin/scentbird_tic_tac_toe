package dkachurin.scentbird.xogamebot.service;

import dkachurin.scentbird.xogamebot.dao.GameDAO;
import dkachurin.scentbird.xogamebot.dao.RoomDAO;
import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.CellsWinningState;
import dkachurin.scentbird.xogamebot.model.Game;
import dkachurin.scentbird.xogamebot.model.MarkType;
import dkachurin.scentbird.xogamebot.model.Room;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import dkachurin.scentbird.xogamebot.model.RoomToPlayer;
import dkachurin.scentbird.xogamebot.model.exception.GameBusinessLogicException;
import dkachurin.scentbird.xogamebot.model.exception.GameErrorType;
import dkachurin.scentbird.xogamebot.model.response.GameResponse;
import dkachurin.scentbird.xogamebot.notification.GameNotificationService;
import dkachurin.scentbird.xogamebot.utils.CellsUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {
    private final GameNotificationService gameNotificationService;
    private final RoomDAO roomDAO;
    private final GameDAO gameDAO;

    @Autowired
    public GameService(
            final GameNotificationService gameNotificationService,
            final RoomDAO roomDAO,
            final GameDAO gameDAO
    ) {
        this.gameNotificationService = gameNotificationService;
        this.roomDAO = roomDAO;
        this.gameDAO = gameDAO;
    }

    public GameResponse findGameByRoomId(final UUID roomId) {
        final Game game = gameDAO.findGameByRoomId(roomId);
        if (game == null) {
            return null;
        }

        final Room room = roomDAO.findRoom(game.roomId());
        final CellsWinningState winState = CellsUtils.findWinState(game.cells());
        final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(game.roomId()));
        final Optional<RoomToPlayer> winnerPlayer = roomPlayers.stream()
                .filter(it -> it.markType() == winState.winnerMarkType())
                .findFirst();
        final String winnerName = winnerPlayer.isPresent()
                ? winnerPlayer.get().playerId().toString()
                : "Unknown";
        return new GameResponse(
                game.id(),
                game.roomId(),
                game.cells(),
                room.status(),
                winnerName,
                winState.winnerMarkType(),
                winState.winningCells()
        );
    }

    public void createGame(final UUID gameId, final UUID roomId) {
        //validate
        // - if game already created for example
        // - if everything is ready for game creation
        // - ?

        //process operation
        gameDAO.createGame(gameId, roomId);

        //return result
        // - ?
    }

    @Transactional
    public void processPlayerAction(
            @NonNull UUID gameId,
            @NonNull String secret,
            @NonNull UUID playerId,
            @NonNull String playingArea
    ) {
        final Game game = gameDAO.findGame(gameId);
        final Room room = roomDAO.findRoom(game.roomId());
        final boolean wrongSecret = !secret.equals(room.secret());
        if (wrongSecret) {
            throw new GameBusinessLogicException(GameErrorType.INCORRECT_AUTH);
        }

        final Optional<RoomToPlayer> playerInGame = roomDAO.findRoomPlayers(List.of(game.roomId())).stream()
                .filter(it -> it.playerId().equals(playerId))
                .findAny();
        final CellsState previousState = game.cells();
        if (CellsUtils.findWinState(previousState).winnerMarkType() != MarkType.NONE) {
            throw new GameBusinessLogicException(GameErrorType.GAME_ALREADY_FINISHED);
        }

        { //validate player action
            if (playerInGame.isEmpty()) {
                throw new GameBusinessLogicException(GameErrorType.PLAYER_IS_NOT_IN_GAME);
            }
            //check if cell is not filled yet
            final boolean actionAllowed = CellsUtils.isValid(previousState, playingArea);
            if (!actionAllowed) {
                throw new GameBusinessLogicException(GameErrorType.ACTION_NOT_ALLOWED);
            }
            final MarkType awaitingTurn = CellsUtils.getAwaitingTurn(previousState);
            if (playerInGame.get().markType() != awaitingTurn) {
                throw new GameBusinessLogicException(GameErrorType.ACTION_NOT_ALLOWED);
            }
        }

        //execute player action
        final CellsState targetState = CellsUtils.merge(
                previousState,
                playingArea,
                playerInGame.get().markType()
        );
        gameDAO.updateGame(gameId, targetState);
        if (CellsUtils.isGameFinished(targetState)) {
            roomDAO.updateRoomStatus(game.roomId(), RoomStatus.DONE);
        }

        //notify about player action
        gameNotificationService.gameStateUpdated(
                room.id(),
                room.status(),
                CellsUtils.findWinState(targetState).winningCells(),
                targetState
        );
    }

}
