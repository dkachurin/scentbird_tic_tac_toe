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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

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
        final Room room = roomDAO.findRoom(roomId);
        final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(roomId));
        final List<PlayerInGameResponse> playerIds = roomPlayers.stream()
                .map(it -> new PlayerInGameResponse(it.playerId(), it.markType()))
                .toList();
        return RoomResponse.from(room, playerIds);
    }

    @Transactional
    public RoomResponse createRoom(final String title) {
        { //validations
            final int activeGames = roomDAO.findRoomsCount(RoomStatus.activeStatuses());
            if (activeGames >= applicationProperties.getActiveGamesLimit()) {
                throw new RoomBusinessLogicException(RoomErrorType.TO_MANY_ACTIVE_GAMES);
            }
        }

        //logic
        final UUID roomId = roomDAO.createRoom(title);

        //response
        return findRoom(roomId);
    }

    @Transactional
    public RoomResponse joinRoom(final UUID roomId, final UUID playerId, final MarkType markType) {
        final Room room = roomDAO.findRoom(roomId);
        { //validations
            if (room == null) {
                throw new RoomBusinessLogicException(RoomErrorType.ROOM_NOT_FOUND);
            }
            if (room.status() != RoomStatus.WAITING) {
                throw new RoomBusinessLogicException(RoomErrorType.ONLY_ROOMS_IN_WAITING_STATUS_ALLOWED_TO_JOIN);
            }
            final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(roomId));
            if (roomPlayers.size() >= 2) {
                throw new RoomBusinessLogicException(RoomErrorType.ALL_PLAYERS_ALREADY_JOINED);
            }
        }

        //logic
        final boolean gameStarted;
        { //add player to room
            roomDAO.addPlayerToRoom(roomId, playerId, markType);
            final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(List.of(roomId));
            if (roomPlayers.size() == 2) {
                roomDAO.updateRoomStatus(roomId, RoomStatus.IN_PROGRESS);
                gameService.createGame(UUID.randomUUID(), roomId);
                gameStarted = true;
            } else {
                gameStarted = false;
            }
        }

        //notifications
        if (gameStarted) {
            gameNotificationService.gameStateUpdated(
                    roomId,
                    room.status(),
                    List.of(),
                    CellsUtils.emptyCells()
            );
        }

        //response
        return findRoom(roomId);
    }

    public RoomsListResponse findRoomsList(final List<RoomStatus> statuses) {
        //validate params
        if (statuses.isEmpty()) {
            return RoomsListResponse.empty();
        }

        //find data
        final List<Room> roomList = roomDAO.findRoomList(statuses);
        final Map<UUID, List<PlayerInGameResponse>> roomIdToPlayerIdsMap;
        { //collect information about room players
            final List<UUID> roomIds = roomList.stream().map(Room::id).toList();
            final List<RoomToPlayer> roomPlayers = roomDAO.findRoomPlayers(roomIds);

            roomIdToPlayerIdsMap = roomPlayers.stream()
                    .collect(Collectors.groupingBy(
                            RoomToPlayer::roomId,
                            Collectors.mapping(
                                    it -> new PlayerInGameResponse(it.playerId(), it.markType()),
                                    Collectors.toList()
                            )
                    ));
        }

        //response
        return RoomsListResponse.from(roomList, roomIdToPlayerIdsMap);
    }

}
