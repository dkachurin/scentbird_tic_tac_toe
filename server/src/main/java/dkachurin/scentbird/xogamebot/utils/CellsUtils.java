package dkachurin.scentbird.xogamebot.utils;

import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.MarkType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class CellsUtils {
    //cells = A1=NONE,A2=NONE,...,C3=NONE

    private static final String cellsDelimiter = ",";
    private static final String keyValueDelimiter = "=";

    public static CellsState parseCells(final String cells) {
        final Map<String, MarkType> resultMutableMap = new HashMap<>();

        for (final String cellState : cells.split(cellsDelimiter)) {
            final String[] keyToValue = cellState.split(keyValueDelimiter);
            resultMutableMap.put(keyToValue[0], MarkType.valueOf(keyToValue[1]));
        }

        return new CellsState(Map.copyOf(resultMutableMap));
    }

    public static String cellsToString(final CellsState cells) {
        return cells.state().entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(joining(","));
    }

    public static CellsState merge(
            final CellsState previousState,
            final String playingArea,
            final MarkType markType
    ) {
        final Map<String, MarkType> state = new HashMap<>(previousState.state());
        state.put(playingArea, markType);
        return new CellsState(Map.copyOf(state));
    }
    public static final Set<String> allowedPlayingAreas = Set.of(
            "A1", "A2", "A3",
            "B1", "B2", "B3",
            "C1", "C2", "C3"
    );
    public static CellsState emptyCells() {
        final Map<String, MarkType> map = allowedPlayingAreas.stream()
                .collect(Collectors.toMap(Function.identity(), it -> MarkType.NONE));

        return new CellsState(map);
    }
    public static boolean isValid(final CellsState previousState, final String playingArea) {
        final boolean allowedAction = allowedPlayingAreas.contains(playingArea.trim());
        if (!allowedAction) {
            return false;
        }
        final MarkType markType = previousState.state().get(playingArea);
        return markType == MarkType.NONE;
    }

    public static MarkType getAwaitingTurn(final CellsState state) {
        final long crossesCount = state.state().values().stream().filter(it -> it == MarkType.CROSS).count();
        final long zerosCount = state.state().values().stream().filter(it -> it == MarkType.ZERO).count();
        if (crossesCount == zerosCount) {
            return MarkType.CROSS;
        } else {
            return MarkType.ZERO;
        }
    }
}
