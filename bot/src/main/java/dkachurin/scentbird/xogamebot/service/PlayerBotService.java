package dkachurin.scentbird.xogamebot.service;

import dkachurin.scentbird.xogamebot.client.GameClient;
import dkachurin.scentbird.xogamebot.client.RoomClient;
import dkachurin.scentbird.xogamebot.model.Game;
import dkachurin.scentbird.xogamebot.model.MarkType;
import dkachurin.scentbird.xogamebot.model.PlayerActionRequest;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import dkachurin.scentbird.xogamebot.model.request.CreateRoomRequest;
import dkachurin.scentbird.xogamebot.model.request.FindByIdRequest;
import dkachurin.scentbird.xogamebot.model.request.FindRoomsRequest;
import dkachurin.scentbird.xogamebot.model.request.JoinRoomRequest;
import dkachurin.scentbird.xogamebot.model.response.PlayerInGameResponse;
import dkachurin.scentbird.xogamebot.model.response.RoomResponse;
import dkachurin.scentbird.xogamebot.model.response.RoomsListResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class PlayerBotService {

    private final GameClient gameClient;
    private final RoomClient roomClient;

    private final ExecutorService robotsExecutorService = Executors.newFixedThreadPool(10);

    public PlayerBotService(
            final GameClient gameClient,
            final RoomClient roomClient
    ) {
        this.gameClient = gameClient;
        this.roomClient = roomClient;
    }

    public UUID startPlayerBot() {
        final UUID currentPlayerId = UUID.randomUUID();

        { //find available game or create new
            final FindRoomsRequest findRoomsRequestParams = new FindRoomsRequest(List.of(RoomStatus.WAITING));
            final RoomsListResponse availableRooms = roomClient.list(findRoomsRequestParams);
            if (availableRooms.rooms().isEmpty()) {
                roomClient.create(new CreateRoomRequest("Room " + System.currentTimeMillis()));
            }
        }

        final UUID roomId;
        final String roomSecret;
        { //join random game
            final FindRoomsRequest findRoomsRequestParams = new FindRoomsRequest(List.of(RoomStatus.WAITING));
            final RoomsListResponse availableRooms = roomClient.list(findRoomsRequestParams);
            final RoomResponse room = availableRooms.rooms().get(0);
            final List<MarkType> usedMarkTypes = room.players().stream()
                    .map(PlayerInGameResponse::markType)
                    .toList();
            final Optional<MarkType> markType = Stream.of(MarkType.ZERO, MarkType.CROSS)
                    .filter(it -> !usedMarkTypes.contains(it))
                    .findAny();
            if (markType.isEmpty()) {
                throw new RuntimeException("No available mark types");
            }
            roomId = room.id();
            roomSecret = room.secret();
            roomClient.join(new JoinRoomRequest(roomId, currentPlayerId, markType.get()));
        }

        startRobotGame(roomId, roomSecret, currentPlayerId);

        return roomId;
    }

    private void startRobotGame(final UUID roomId, final String secret, final UUID currentPlayerId) {
        robotsExecutorService.submit(() -> {
            final long startedTime = System.currentTimeMillis();
            //5 минут играет, а потом ливает из игры
            final long timePerGameLimitMillis = 5 * 60 * 1000;

            while (startedTime + timePerGameLimitMillis > System.currentTimeMillis()) {

                try {
                    int random = new Random().nextInt(7000);
                    //noinspection BusyWait
                    Thread.sleep(5_000 + random);
                    final Game game = gameClient.find(new FindByIdRequest(roomId.toString()));
                    if (game == null) {
                        continue;
                    }

                    final Map<String, MarkType> state = game.cells().state();
                    final Optional<String> playingArea = state.entrySet().stream()
                            .filter(it -> it.getValue() == MarkType.NONE)
                            .map(Map.Entry::getKey)
                            .findAny();

                    if (playingArea.isEmpty()) {
                        //finish game
                        return;
                    }

                    gameClient.action(new PlayerActionRequest(
                            game.id(),
                            secret,
                            currentPlayerId,
                            playingArea.get()
                    ));
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
