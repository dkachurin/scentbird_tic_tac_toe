package dkachurin.scentbird.xogamebot.service;

import dkachurin.scentbird.xogamebot.model.MarkType;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import dkachurin.scentbird.xogamebot.model.exception.RoomBusinessLogicException;
import dkachurin.scentbird.xogamebot.model.exception.RoomErrorType;
import dkachurin.scentbird.xogamebot.model.response.RoomResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class RoomServiceIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Test
    public void testCreateRoom() {
        final String title = "Test Room";
        final RoomResponse response = roomService.createRoom(title);

        assertThat(response).isNotNull();
        assertThat(response.roomName()).isEqualTo(title);
        assertThat(response.status()).isEqualTo(RoomStatus.WAITING);
    }

    @Test
    public void testJoinRoom() {
        final String title = "Test Room";
        final RoomResponse createdRoomResponse = roomService.createRoom(title);
        final UUID roomId = createdRoomResponse.id();

        final UUID playerId1 = UUID.randomUUID();
        final UUID playerId2 = UUID.randomUUID();

        final RoomResponse response1 = roomService.joinRoom(roomId, playerId1, MarkType.CROSS);
        assertThat(response1.players()).hasSize(1);

        RoomResponse response2 = roomService.joinRoom(roomId, playerId2, MarkType.ZERO);
        assertThat(response2.players()).hasSize(2);
        assertThat(response2.status()).isEqualTo(RoomStatus.IN_PROGRESS);
    }

    @Test
    public void testJoinRoomWhenRoomNotFound() {
        final UUID roomId = UUID.randomUUID();
        final UUID playerId = UUID.randomUUID();

        assertThatThrownBy(() -> roomService.joinRoom(roomId, playerId, MarkType.CROSS))
                .isInstanceOf(RoomBusinessLogicException.class)
                .hasMessageContaining(RoomErrorType.ROOM_NOT_FOUND.name());
    }

    @Test
    public void testJoinRoomWhenRoomNotWaiting() {
        final String title = "Test Room";
        final RoomResponse createdRoomResponse = roomService.createRoom(title);
        final UUID roomId = createdRoomResponse.id();
        final UUID playerId1 = UUID.randomUUID();
        final UUID playerId2 = UUID.randomUUID();

        roomService.joinRoom(roomId, playerId1, MarkType.CROSS);
        roomService.joinRoom(roomId, playerId2, MarkType.ZERO);

        final UUID newPlayerId = UUID.randomUUID();
        assertThatThrownBy(() -> roomService.joinRoom(roomId, newPlayerId, MarkType.CROSS))
                .isInstanceOf(RoomBusinessLogicException.class)
                .hasMessageContaining(RoomErrorType.ONLY_ROOMS_IN_WAITING_STATUS_ALLOWED_TO_JOIN.name());
    }
}