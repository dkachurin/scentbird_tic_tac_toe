package dkachurin.scentbird.xogamebot.service;

import dkachurin.scentbird.xogamebot.dao.GameDAO;
import dkachurin.scentbird.xogamebot.dao.RoomDAO;
import dkachurin.scentbird.xogamebot.model.*;
import dkachurin.scentbird.xogamebot.model.exception.GameBusinessLogicException;
import dkachurin.scentbird.xogamebot.model.exception.GameErrorType;
import dkachurin.scentbird.xogamebot.model.response.GameResponse;
import dkachurin.scentbird.xogamebot.notification.GameNotificationService;
import dkachurin.scentbird.xogamebot.utils.CellsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public GameResponse findGameByRoomId(
            @NonNull final UUID roomId
    ) {
        final Game game = gameDAO.findGameByRoomId(roomId);
        if (game == null) {
            return null;
        }

        final Room room = roomDAO.findRoom(game.roomId());
        final CellsWinningState winState = CellsUtils.findWinState(game.cells());
        final String winnerName;
        if (winState.winnerMarkType() != MarkType.NONE) {
            final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(game.roomId()));
            winnerName = roomPlayers.stream()
                    .filter(winnerPlayer -> winnerPlayer.markType() == winState.winnerMarkType())
                    .findFirst()
                    .map(winnerPlayer -> winnerPlayer.playerId().toString())
                    .orElse("Unknown Player Name");
        } else {
            winnerName = null;
        }

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

    @Transactional
    public Game createGame(
            @NonNull final UUID gameId,
            @NonNull final UUID roomId
    ) {
        //validate
        final Room room = roomDAO.findRoom(roomId);
        if (room == null) {
            throw new GameBusinessLogicException(GameErrorType.ROOM_FOR_GAME_NOT_EXISTS);
        }
        final Game game = gameDAO.findGame(gameId);
        if (game != null) {
            throw new GameBusinessLogicException(GameErrorType.GAME_ID_ALREADY_EXISTS);
        }

        //process operation
        gameDAO.createGame(gameId, roomId);

        //return result
        return gameDAO.findGame(gameId);
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
        final CellsState previousState = game.cells();
        final Optional<RoomToPlayer> playerInGame = roomDAO.findRoomPlayers(List.of(game.roomId())).stream()
                .filter(it -> it.playerId().equals(playerId))
                .findAny();

        { //validations
            if (!secret.equals(room.secret())) {
                throw new GameBusinessLogicException(GameErrorType.INCORRECT_AUTH);
            }

            if (CellsUtils.findWinState(previousState).winnerMarkType() != MarkType.NONE) {
                throw new GameBusinessLogicException(GameErrorType.GAME_ALREADY_FINISHED);
            }

            //validate player action
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
