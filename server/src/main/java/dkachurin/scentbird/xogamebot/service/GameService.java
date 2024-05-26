package dkachurin.scentbird.xogamebot.service;

import dkachurin.scentbird.xogamebot.dao.GameDAO;
import dkachurin.scentbird.xogamebot.dao.RoomDAO;
import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.Game;
import dkachurin.scentbird.xogamebot.model.MarkType;
import dkachurin.scentbird.xogamebot.model.Room;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import dkachurin.scentbird.xogamebot.model.RoomToPlayer;
import dkachurin.scentbird.xogamebot.model.exception.GameBusinessLogicException;
import dkachurin.scentbird.xogamebot.model.exception.GameErrorType;
import dkachurin.scentbird.xogamebot.notification.GameNotificationService;
import dkachurin.scentbird.xogamebot.utils.CellsUtils;
import java.util.List;
import java.util.Map;
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

    public Game findGameByRoomId(final UUID roomId) {
        return gameDAO.findGameByRoomId(roomId);
    }
    public Game findGame(final UUID gameId) {
        return gameDAO.findGame(gameId);
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
        if (findWinner(previousState) != MarkType.NONE) {
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
        if (isGameFinished(targetState)) {
            roomDAO.updateRoomStatus(game.roomId(), RoomStatus.DONE);
        }

        //notify about player action
        gameNotificationService.gameStateUpdated(room.id(), room.status(), targetState);
    }

    private MarkType findWinner(final CellsState state) {
        final Map<String, MarkType> stateMap = state.state();
        //vertical
        for (final String cell : new String[]{"A", "B", "C"}) {
            final MarkType c1 = stateMap.get(cell + "1");
            final MarkType c2 = stateMap.get(cell + "2");
            final MarkType c3 = stateMap.get(cell + "3");
            if (c1 == c2 && c2 == c3) {
                return c1;
            }
        }

        //horizontal
        for (final String cell : new String[]{"1", "2", "3"}) {
            final MarkType c1 = stateMap.get("A" + cell);
            final MarkType c2 = stateMap.get("B" + cell);
            final MarkType c3 = stateMap.get("C" + cell);
            if (c1 == c2 && c2 == c3) {
                return c1;
            }
        }

        { //cross 1
            final MarkType a1 = stateMap.get("A1");
            final MarkType b2 = stateMap.get("B2");
            final MarkType c3 = stateMap.get("C3");
            if (a1 == b2 && b2 == c3) {
                return a1;
            }
        }

        { //cross 2
            final MarkType a3 = stateMap.get("A1");
            final MarkType b2 = stateMap.get("B2");
            final MarkType c1 = stateMap.get("C3");
            if (a3 == b2 && b2 == c1) {
                return a3;
            }
        }

        return MarkType.NONE;
    }

    private boolean isGameFinished(final CellsState state) {
        return state.state().values()
                .stream()
                .noneMatch(cellMarkType -> cellMarkType == MarkType.NONE);
    }
}
