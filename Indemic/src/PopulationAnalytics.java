import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PopulationAnalytics {

    public static Map<cellState, Long> countByState(
            world gameWorld) {

        return gameWorld.getSections().stream()
            .flatMap(area ->
                java.util.stream.IntStream
                    .range(0, area.getGridRows())
                    .boxed()
                    .flatMap(row ->
                        java.util.stream.IntStream
                            .range(0, area.getGridCols())
                            .mapToObj(col ->
                                area.getGridCell(row, col)
                            )
                    )
            )
            .filter(cell -> cell != null)
            .flatMap(cell -> cell.getEntities().stream())
            .collect(Collectors.groupingBy(
                Entity::getState,
                () -> new EnumMap<>(cellState.class),
                Collectors.counting()
            ));
    }
}

