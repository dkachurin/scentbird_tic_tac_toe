--liquibase formatted sql

--changeset dkachurin:TASK-001

CREATE TABLE room
(
    id              UUID      PRIMARY KEY,
    secret          VARCHAR   NOT NULL,
    title           VARCHAR   NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    status          VARCHAR   NOT NULL
);

CREATE TABLE room_to_player
(
    room_id         UUID        NOT NULL,
    player_id       UUID        NOT NULL,
    mark_type       VARCHAR     NOT NULL
);

CREATE TABLE game
(
    id          UUID        NOT NULL,
    room_id     UUID        NOT NULL,
    cells       VARCHAR     NOT NULL
);
