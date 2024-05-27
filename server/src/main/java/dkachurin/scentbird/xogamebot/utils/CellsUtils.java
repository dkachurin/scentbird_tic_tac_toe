package dkachurin.scentbird.xogamebot.utils;

import dkachurin.scentbird.xogamebot.model.CellsState;
import dkachurin.scentbird.xogamebot.model.CellsWinningState;
import dkachurin.scentbird.xogamebot.model.MarkType;
import java.util.HashMap;
import java.util.List;
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

    public static CellsWinningState findWinState(final CellsState state) {
        final Map<String, MarkType> stateMap = state.state();
        //vertical
        for (final String cell : new String[]{"A", "B", "C"}) {
            final String index1 = cell + "1";
            final String index2 = cell + "2";
            final String index3 = cell + "3";
            if (stateMap.get(index1) == stateMap.get(index2) && stateMap.get(index2) == stateMap.get(index3)) {
                return new CellsWinningState(stateMap.get(index1), List.of(index1, index2, index3));
            }
        }

        //horizontal
        for (final String cell : new String[]{"1", "2", "3"}) {
            final String indexA = "A" + cell;
            final String indexB = "B" + cell;
            final String indexC = "C" + cell;
            if (stateMap.get(indexA) == stateMap.get(indexB) && stateMap.get(indexB) == stateMap.get(indexC)) {
                return new CellsWinningState(stateMap.get(indexA), List.of(indexA, indexB, indexC));
            }
        }

        { //cross 1
            final MarkType a1 = stateMap.get("A1");
            final MarkType b2 = stateMap.get("B2");
            final MarkType c3 = stateMap.get("C3");
            if (a1 == b2 && b2 == c3) {
                return new CellsWinningState(a1, List.of("A1", "B2", "C3"));
            }
        }

        { //cross 2
            final MarkType a3 = stateMap.get("A3");
            final MarkType b2 = stateMap.get("B2");
            final MarkType c1 = stateMap.get("C1");
            if (a3 == b2 && b2 == c1) {
                return new CellsWinningState(a3, List.of("A3", "B2", "C1"));
            }
        }

        return new CellsWinningState(MarkType.NONE, List.of());
    }

    public static boolean isGameFinished(final CellsState state) {
        final boolean allCellsAreFilled = state.state().values()
                .stream()
                .noneMatch(cellMarkType -> cellMarkType == MarkType.NONE);

        if (allCellsAreFilled) {
            return true;
        }

        final MarkType winner = findWinState(state).winnerMarkType();
        return winner != MarkType.NONE;
    }
}
