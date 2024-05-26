package dkachurin.scentbird.xogamebot.dao;

import dkachurin.scentbird.xogamebot.model.MarkType;
import dkachurin.scentbird.xogamebot.model.Room;
import dkachurin.scentbird.xogamebot.model.RoomStatus;
import dkachurin.scentbird.xogamebot.model.RoomToPlayer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoomDAO {

    private static final String roomSelectFields = "id, secret, title, created_at, status";
    private static final RowMapper<Room> roomRowMapper = (rs, num) -> new Room(
            UUID.fromString(rs.getString("id")),
            rs.getString("secret"),
            rs.getString("title"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            RoomStatus.valueOf(rs.getString("status"))
    );
    private static final RowMapper<RoomToPlayer> roomToPlayerRowMapper = (rs, num) -> new RoomToPlayer(
            UUID.fromString(rs.getString("player_id")),
            UUID.fromString(rs.getString("room_id")),
            MarkType.valueOf(rs.getString("mark_type"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public RoomDAO(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UUID createRoom(final String title) {
        final UUID id = UUID.randomUUID();
        final LocalDateTime now = LocalDateTime.now();

        final String insertSql = "" +
                "INSERT INTO room(id, secret, title, created_at, status)" +
                "     VALUES (:id, :secret, :title, :created_at, :status)";

        jdbcTemplate.update(
                insertSql,
                Map.of(
                        "id", id,
                        "secret", "secret_" + RandomStringUtils.randomAlphabetic(10),
                        "title", title,
                        "created_at", now,
                        "status", RoomStatus.WAITING.name()
                ));

        return id;
    }

    public List<Room> findRoomList(final List<RoomStatus> statuses) {
        final String sql = "SELECT " + roomSelectFields + " FROM room WHERE status IN (:statuses)";

        return jdbcTemplate.query(
                sql,
                Map.of("statuses", statuses.stream().map(RoomStatus::name).toList()),
                roomRowMapper
        );
    }

    public int findRoomsCount(final List<RoomStatus> statuses) {
        final String sql = "SELECT count(id) FROM room WHERE status IN (:statuses)";

        return jdbcTemplate.queryForObject(
                sql,
                Map.of("statuses", statuses.stream().map(RoomStatus::name).toList()),
                Integer.class
        );
    }

    public Room findRoom(final UUID id) {
        final String sql = "SELECT " + roomSelectFields + " FROM room WHERE id = :id";

        return jdbcTemplate.queryForObject(
                sql,
                Map.of("id", id),
                roomRowMapper
        );
    }

    public List<RoomToPlayer> findRoomPlayers(final List<UUID> roomIds) {
        final String sql = "SELECT room_id, player_id, mark_type FROM room_to_player WHERE room_id IN (:room_ids)";

        return jdbcTemplate.query(
                sql,
                Map.of("room_ids", roomIds),
                roomToPlayerRowMapper
        );
    }

    public void updateRoomStatus(final UUID roomId, final RoomStatus roomStatus) {
        final String sql = "UPDATE room SET status = :status WHERE id = :id";
        jdbcTemplate.update(
                sql,
                Map.of("id", roomId, "status", roomStatus.name())
        );
    }

    public void addPlayerToRoom(final UUID roomId, final UUID playerId, final MarkType markType) {
        final String insertSql = "" +
                "INSERT INTO room_to_player(room_id, player_id, mark_type)" +
                "     VALUES (:room_id, :player_id, :mark_type)";

        jdbcTemplate.update(
                insertSql,
                Map.of("room_id", roomId, "player_id", playerId, "mark_type", markType.name())
        );
    }
}
