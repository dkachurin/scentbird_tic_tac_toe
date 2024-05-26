package dkachurin.scentbird.xogamebot.model;

import java.util.UUID;
import org.springframework.lang.NonNull;

//моделька с параметрами хода игрока
public record PlayerActionRequest(
        @NonNull UUID gameId,
        @NonNull String secret,
        @NonNull UUID playerId,
        @NonNull String playingArea
) {
}
