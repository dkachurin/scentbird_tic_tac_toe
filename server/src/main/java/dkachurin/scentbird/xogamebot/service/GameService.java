package dkachurin.scentbird.xogamebot.service;

import dkachurin.scentbird.xogamebot.dao.GameDAO;
import dkachurin.scentbird.xogamebot.dao.RoomDAO;
import dkachurin.scentbird.xogamebot.model.*;
import dkachurin.scentbird.xogamebot.model.exception.GameBusinessLogicException;
import dkachurin.scentbird.xogamebot.model.exception.GameErrorType;
import dkachurin.scentbird.xogamebot.model.response.GameResponse;
import dkachurin.scentbird.xogamebot.notification.GameNotificationService;
import dkachurin.scentbird.xogamebot.utils.CellsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);

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
        logger.info("findGameByRoomId: roomId {}", roomId);
        final Game game = gameDAO.findGameByRoomId(roomId);
        if (game == null) {
            logger.debug("findGameByRoomId: game with id {} not found", roomId);
            return null;
        }

        final Room room = roomDAO.findRoom(game.roomId());
        final CellsWinningState winState = CellsUtils.findWinState(game.cells());
        final String winnerName;
        if (winState.winnerMarkType() != MarkType.NONE) {
            logger.debug("findGameByRoomId: win state != MarkType.NONE");
            
            final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(game.roomId()));
            logger.debug("findGameByRoomId: room players {}", roomPlayers);
            
            winnerName = roomPlayers.stream()
                    .filter(winnerPlayer -> winnerPlayer.markType() == winState.winnerMarkType())
                    .findFirst()
                    .map(winnerPlayer -> winnerPlayer.playerId().toString())
                    .orElse("Unknown Player Name");
            logger.debug("findGameByRoomId: winner name {}", winnerName);
        } else {
            logger.debug("findGameByRoomId: win state == MarkType.NONE");
            winnerName = null;
        }

        final GameResponse gameResponse = new GameResponse(
                game.id(),
                game.roomId(),
                game.cells(),
                room.status(),
                winnerName,
                winState.winnerMarkType(),
                winState.winningCells()
        );
        logger.info("findGameByRoomId: gameResponse {}", gameResponse);
        return gameResponse;
    }

    @Transactional
    public Game createGame(
            @NonNull final UUID gameId,
            @NonNull final UUID roomId
    ) {
        logger.info("createGame: gameId {}, roomId {}", gameId, roomId);
        //validate
        final Room room = roomDAO.findRoom(roomId);
        if (room == null) {
            logger.debug("createGame: room with id {} not found", roomId);
            throw new GameBusinessLogicException(GameErrorType.ROOM_FOR_GAME_NOT_EXISTS);
        }
        final Game game = gameDAO.findGame(gameId);
        if (game != null) {
            logger.debug("createGame: game with id {} already exists", gameId);
            throw new GameBusinessLogicException(GameErrorType.GAME_ID_ALREADY_EXISTS);
        }

        //process operation
        logger.debug("createGame: created game with id {} and roomId {}", gameId, roomId);
        gameDAO.createGame(gameId, roomId);

        //return result
        final Game result = gameDAO.findGame(gameId);
        logger.info("createGame: result {}", result);
        return result;
    }

    @Transactional
    public void processPlayerAction(
            @NonNull UUID gameId,
            @NonNull String secret,
            @NonNull UUID playerId,
            @NonNull String playingArea
    ) {
        logger.info(
                "processPlayerAction: gameId {}, secret XXX_MASKED_XXX, playerId {}, playingArea {}", 
                gameId, playerId, playingArea
        );
        final Game game = gameDAO.findGame(gameId);
        final Room room = roomDAO.findRoom(game.roomId());
        final CellsState previousState = game.cells();
        final Optional<RoomToPlayer> playerInGame = roomDAO.findRoomPlayers(List.of(game.roomId())).stream()
                .filter(it -> it.playerId().equals(playerId))
                .findAny();

        { //validations
            if (!secret.equals(room.secret())) {
                logger.debug("processPlayerAction: invalid secret");
                throw new GameBusinessLogicException(GameErrorType.INCORRECT_AUTH);
            }

            if (CellsUtils.findWinState(previousState).winnerMarkType() != MarkType.NONE) {
                logger.debug("processPlayerAction: win state != MarkType.NONE");
                throw new GameBusinessLogicException(GameErrorType.GAME_ALREADY_FINISHED);
            }

            //validate player action
            if (playerInGame.isEmpty()) {
                logger.debug("processPlayerAction: playerInGame is empty");
                throw new GameBusinessLogicException(GameErrorType.PLAYER_IS_NOT_IN_GAME);
            }
            //check if cell is not filled yet
            final boolean actionAllowed = CellsUtils.isValid(previousState, playingArea);
            if (!actionAllowed) {
                logger.debug("processPlayerAction: invalid action for playingArea {}", playingArea);
                throw new GameBusinessLogicException(GameErrorType.ACTION_NOT_ALLOWED);
            }
            final MarkType awaitingTurn = CellsUtils.getAwaitingTurn(previousState);
            if (playerInGame.get().markType() != awaitingTurn) {
                logger.debug(
                        "processPlayerAction: playerInGame markType != awaitingTurn. awaitingTurn {}, playerInGame {}", 
                        awaitingTurn, playerInGame
                );
                throw new GameBusinessLogicException(GameErrorType.ACTION_NOT_ALLOWED);
            }
        }

        //execute player action
        final CellsState targetState = CellsUtils.merge(
                previousState,
                playingArea,
                playerInGame.get().markType()
        );
        logger.debug("processPlayerAction: updateGame. gameId {}, targetState {}", gameId, targetState);
        gameDAO.updateGame(gameId, targetState);
        if (CellsUtils.isGameFinished(targetState)) {
            logger.debug("processPlayerAction: game finished. roomId {}", game.roomId());
            roomDAO.updateRoomStatus(game.roomId(), RoomStatus.DONE);
        }

        //notify about player action
        logger.info("processPlayerAction: notification sent to roomId {}", game.roomId());
        gameNotificationService.gameStateUpdated(
                room.id(),
                room.status(),
                CellsUtils.findWinState(targetState).winningCells(),
                targetState
        );
    }

}
