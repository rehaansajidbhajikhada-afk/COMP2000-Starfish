import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public class PopulationAnalytics {

    public static Map<cellState, Long> countByState(world gameWorld) {
        return gameWorld.getSections().stream()
            .flatMap(area ->
                IntStream.range(0, area.getGridRows())
                    .boxed()
                    .flatMap(row ->
                        IntStream.range(0, area.getGridCols())
                            .mapToObj(col -> area.getGridCell(row, col))
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

    public static List<cellState> createPopulationSnapshot(world gameWorld) {
        List<cellState> snapshot = new ArrayList<>();

        for (section area : gameWorld.getSections()) {
            for (int row = 0; row < area.getGridRows(); row++) {
                for (int col = 0; col < area.getGridCols(); col++) {

                    GridCell<Entity> cell = area.getGridCell(row, col);

                    if (cell == null) continue;

                    for (Entity entity : cell.getEntities()) {
                        snapshot.add(entity.getState());
                    }
                }
            }
        }

        return snapshot;
    }

    public static long countInfectedSequential(List<cellState> snapshot) {
        return snapshot.stream()
            .filter(state -> state == cellState.INFECTED)
            .count();
    }

    public static long countInfectedParallel(List<cellState> snapshot) {
        return snapshot.parallelStream()
            .filter(state -> state == cellState.INFECTED)
            .count();
    }
}
