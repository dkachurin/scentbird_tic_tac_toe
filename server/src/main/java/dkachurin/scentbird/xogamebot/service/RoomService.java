package dkachurin.scentbird.xogamebot.service;

import dkachurin.scentbird.xogamebot.configs.ApplicationProperties;
import dkachurin.scentbird.xogamebot.notification.GameNotificationService;
import dkachurin.scentbird.xogamebot.dao.RoomDAO;
import dkachurin.scentbird.xogamebot.model.MarkType;
import dkachurin.scentbird.xogamebot.model.Room;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import dkachurin.scentbird.xogamebot.model.RoomToPlayer;
import dkachurin.scentbird.xogamebot.model.exception.RoomBusinessLogicException;
import dkachurin.scentbird.xogamebot.model.exception.RoomErrorType;
import dkachurin.scentbird.xogamebot.model.response.PlayerInGameResponse;
import dkachurin.scentbird.xogamebot.model.response.RoomResponse;
import dkachurin.scentbird.xogamebot.model.response.RoomsListResponse;
import dkachurin.scentbird.xogamebot.utils.CellsUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    private final RoomDAO roomDAO;
    private final GameService gameService;
    private final GameNotificationService gameNotificationService;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public RoomService(
            final RoomDAO roomDAO,
            final GameService gameService,
            final GameNotificationService gameNotificationService,
            final ApplicationProperties applicationProperties
    ) {
        this.roomDAO = roomDAO;
        this.gameService = gameService;
        this.gameNotificationService = gameNotificationService;
        this.applicationProperties = applicationProperties;
    }

    private RoomResponse findRoom(final UUID roomId) {
        logger.info("findRoom: id {}", roomId);

        final Room room = roomDAO.findRoom(roomId);
        final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(roomId));
        logger.debug("findRoom: roomPlayers {}", roomPlayers);
        final List<PlayerInGameResponse> playerIds = roomPlayers.stream()
                .map(it -> new PlayerInGameResponse(it.playerId(), it.markType()))
                .toList();

        final RoomResponse response = RoomResponse.from(room, playerIds);
        logger.info("findRoom: response {}", response);
        return response;
    }

    @Transactional
    public RoomResponse createRoom(final String title) {
        logger.info("createRoom: title {}", title);
        
        { //validations
            final int activeGames = roomDAO.findRoomsCount(RoomStatus.activeStatuses());
            final int limit = applicationProperties.getActiveGamesLimit();
            if (activeGames >= limit) {
                logger.debug("createRoom: active games count limit. activeGames {}, limit {}", activeGames, limit);
                throw new RoomBusinessLogicException(RoomErrorType.TO_MANY_ACTIVE_GAMES);
            }
        }

        //logic
        final UUID roomId = roomDAO.createRoom(title);
        logger.debug("createRoom: created room {}", roomId);

        //response
        final RoomResponse response = findRoom(roomId);
        logger.info("createRoom: response {}", response);
        return response;
    }

    @Transactional
    public RoomResponse joinRoom(final UUID roomId, final UUID playerId, final MarkType markType) {
        logger.info("joinRoom: roomId {}, playerId {}, markType {}", roomId, playerId, markType);

        final Room room = roomDAO.findRoom(roomId);
        { //validations
            if (room == null) {
                logger.debug("joinRoom: room null");
                throw new RoomBusinessLogicException(RoomErrorType.ROOM_NOT_FOUND);
            }
            if (room.status() != RoomStatus.WAITING) {
                logger.debug("joinRoom: room status not waiting");
                throw new RoomBusinessLogicException(RoomErrorType.ONLY_ROOMS_IN_WAITING_STATUS_ALLOWED_TO_JOIN);
            }
            final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(roomId));
            logger.debug("joinRoom: roomPlayers {}", roomPlayers);
            if (roomPlayers.size() >= 2) {
                logger.debug("joinRoom: room players size meet limit. roomPlayers {}, limit {}", roomPlayers.size(), 2);
                throw new RoomBusinessLogicException(RoomErrorType.ALL_PLAYERS_ALREADY_JOINED);
            }
        }

        //logic
        final boolean gameStarted;
        { //add player to room
            roomDAO.addPlayerToRoom(roomId, playerId, markType);
            logger.debug("joinRoom: addPlayerToRoom. roomId {}, playerId {}, markType {}", roomId, playerId, markType);
            final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(roomId));
            if (roomPlayers.size() == 2) {
                roomDAO.updateRoomStatus(roomId, RoomStatus.IN_PROGRESS);
                final UUID gameId = UUID.randomUUID();
                gameService.createGame(gameId, roomId);
                logger.debug("joinRoom: start game because all players in game. gameId {}, roomId {}", gameId, roomId);
                gameStarted = true;
            } else {
                logger.debug("joinRoom: player added. Waiting for more players to start game");
                gameStarted = false;
            }
        }

        //notifications
        if (gameStarted) {
            logger.info("joinRoom: send notification. roomId {}", roomId);
            gameNotificationService.gameStateUpdated(
                    roomId,
                    room.status(),
                    List.of(),
                    CellsUtils.emptyCells()
            );
        }

        //response
        final RoomResponse response = findRoom(roomId);
        logger.info("joinRoom: response {}", response);
        return response;
    }

    public RoomsListResponse findRoomsList(final List<RoomStatus> statuses) {
        logger.info("findRoomsList: statuses {}", statuses);

        //validate params
        if (statuses.isEmpty()) {
            logger.debug("findRoomsList: empty statuses");
            return RoomsListResponse.empty();
        }

        //find data
        final List<Room> roomList = roomDAO.findRoomList(statuses);
        final Map<UUID, List<PlayerInGameResponse>> roomIdToPlayerIdsMap;
        { //collect information about room players
            final List<UUID> roomIds = roomList.stream().map(Room::id).toList();
            final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(roomIds);
            logger.debug("findRoomsList: roomPlayers {}", roomPlayers);

            roomIdToPlayerIdsMap = roomPlayers.stream()
                    .collect(Collectors.groupingBy(
                            RoomToPlayer::roomId,
                            Collectors.mapping(
                                    it -> new PlayerInGameResponse(it.playerId(), it.markType()),
                                    Collectors.toList()
                            )
                    ));
        }
        logger.debug("findRoomsList: roomIdToPlayerIdsMap {}", roomIdToPlayerIdsMap);

        //response
        final RoomsListResponse response = RoomsListResponse.from(roomList, roomIdToPlayerIdsMap);
        logger.info("findRoomsList: response {}", response);
        return response;
    }

}
