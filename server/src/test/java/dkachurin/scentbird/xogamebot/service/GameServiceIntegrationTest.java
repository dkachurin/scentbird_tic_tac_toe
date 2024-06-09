package dkachurin.scentbird.xogamebot.service;

import dkachurin.scentbird.xogamebot.dao.GameDAO;
import dkachurin.scentbird.xogamebot.dao.RoomDAO;
import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.Game;
import dkachurin.scentbird.xogamebot.model.MarkType;
import dkachurin.scentbird.xogamebot.model.exception.GameBusinessLogicException;
import dkachurin.scentbird.xogamebot.model.response.GameResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GameServiceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private RoomDAO roomDAO;

    @Autowired
    private GameDAO gameDAO;

    @Test
    @Transactional
    void testFindGameByRoomId_GameExists() {
        //prepare test data
        final UUID roomId = roomDAO.createRoom("Test Room");
        final UUID gameId = UUID.randomUUID();

        //action
        gameDAO.createGame(gameId, roomId);

        //assert
        GameResponse response = gameService.findGameByRoomId(roomId);
        assertNotNull(response);
        assertEquals(gameId, response.id());
        assertEquals(roomId, response.roomId());
    }

    @Test
    @Transactional
    void testFindGameByRoomId_GameDoesNotExist() {
        //prepare test data
        final UUID roomId = roomDAO.createRoom("Test Room");

        //action
        GameResponse response = gameService.findGameByRoomId(roomId);

        //assert
        assertNull(response);
    }

    @Test
    @Transactional
    void testCreateGame_Success() {
        //prepare test data
        final UUID roomId = roomDAO.createRoom("Test Room");
        final UUID gameId = UUID.randomUUID();

        //action
        Game createdGame = gameService.createGame(gameId, roomId);

        //assert
        assertNotNull(createdGame);
        assertEquals(gameId, createdGame.id());
        assertEquals(roomId, createdGame.roomId());
    }

    @Test
    @Transactional
    void testCreateGame_GameAlreadyExists() {
        //prepare test data
        final UUID roomId = roomDAO.createRoom("Test Room");
        final UUID gameId = UUID.randomUUID();
        gameDAO.createGame(gameId, roomId);

        //action & assert
        assertThrows(
                GameBusinessLogicException.class,
                () -> gameService.createGame(gameId, roomId)
        );
    }

    @Test
    @Transactional
    void testCreateGame_RoomDoesNotExist() {
        //prepare test data
        final UUID nonExistentRoomId = UUID.randomUUID();

        //action & assert
        assertThrows(
                GameBusinessLogicException.class,
                () -> gameService.createGame(UUID.randomUUID(), nonExistentRoomId)
        );
    }

    @Test
    @Transactional
    void testProcessPlayerAction_Success() {
        //prepare test data
        final UUID roomId = roomDAO.createRoom("Test Room");
        final UUID gameId = UUID.randomUUID();
        roomDAO.addPlayerToRoom(roomId, UUID.randomUUID(), MarkType.CROSS);
        roomDAO.addPlayerToRoom(roomId, UUID.randomUUID(), MarkType.ZERO);
        gameDAO.createGame(gameId, roomId);

        //action
        final String secret = roomDAO.findRoom(roomId).secret();
        final UUID playerId = roomDAO.findRoomPlayers(List.of(roomId)).getFirst().playerId();
        gameService.processPlayerAction(gameId, secret, playerId, "A1");

        //assert
        Game updatedGame = gameDAO.findGame(gameId);
        CellsState cellsState = updatedGame.cells();
        assertNotNull(cellsState);
        assertEquals(MarkType.CROSS, cellsState.state().get("A1"));
    }

}