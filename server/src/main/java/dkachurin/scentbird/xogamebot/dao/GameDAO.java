package dkachurin.scentbird.xogamebot.dao;

import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.Game;
import dkachurin.scentbird.xogamebot.utils.CellsUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GameDAO {

    private static final String gameSelectFields = "id, room_id, cells";
    private static final RowMapper<Game> gameRowMapper = (rs, num) -> {
        CellsState cells = CellsUtils.parseCells(rs.getString("cells"));
        UUID roomId = UUID.fromString(rs.getString("room_id"));
        UUID id = UUID.fromString(rs.getString("id"));
        return new Game(
                id,
                roomId,
                cells
        );
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public GameDAO(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createGame(final UUID gameId, final UUID roomId) {
        final String sql = "INSERT INTO game(id, room_id, cells) VALUES (:id, :room_id, :cells)";
        final String emptyCells = CellsUtils.cellsToString(CellsUtils.emptyCells());
        jdbcTemplate.update(
                sql,
                Map.of("id", gameId, "room_id", roomId, "cells", emptyCells)
        );
    }

    public void updateGame(final UUID gameId, final CellsState cells) {
        final String sql = "UPDATE game SET cells = :cells WHERE id = :game_id";
        jdbcTemplate.update(
                sql,
                Map.of("game_id", gameId, "cells", CellsUtils.cellsToString(cells))
        );
    }

    public Game findGame(final UUID gameId) {
        final String sql = "SELECT " + gameSelectFields + " FROM game WHERE id = :game_id";

        return DataAccessUtils.singleResult(jdbcTemplate.query(
                sql,
                Map.of("game_id", gameId),
                gameRowMapper
        ));
    }

    public Game findGameByRoomId(final UUID roomId) {
        final String sql = "SELECT " + gameSelectFields + " FROM game WHERE room_id = :room_id";

        final List<Game> list = jdbcTemplate.query(
                sql,
                Map.of("room_id", roomId),
                gameRowMapper
        );

        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

}
