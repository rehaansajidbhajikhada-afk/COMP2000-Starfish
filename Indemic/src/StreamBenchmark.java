
import java.util.ArrayList;
import java.util.List;

public class StreamBenchmark {

    public static void main(String[] args) {

        world gameWorld = new world(1200, 600);
        gameWorld.selectStartingSection(0, 0);

        List<cellState> original =
            PopulationAnalytics.createPopulationSnapshot(gameWorld);

        List<cellState> largeSnapshot = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {
            largeSnapshot.addAll(original);
        }

        for (int i = 0; i < 20; i++) {
            PopulationAnalytics.countInfectedSequential(largeSnapshot);
            PopulationAnalytics.countInfectedParallel(largeSnapshot);
        }

        long startSequential = System.nanoTime();
        long sequentialCount =
            PopulationAnalytics.countInfectedSequential(largeSnapshot);
        long sequentialTime = System.nanoTime() - startSequential;

        long startParallel = System.nanoTime();
        long parallelCount =
            PopulationAnalytics.countInfectedParallel(largeSnapshot);
        long parallelTime = System.nanoTime() - startParallel;

        System.out.println("Original population: " + original.size());
        System.out.println("Synthetic population: " + largeSnapshot.size());

        System.out.println("Sequential infected: " + sequentialCount);
        System.out.println("Parallel infected: " + parallelCount);

        System.out.printf(
            "Sequential time: %.3f ms%n",
            sequentialTime / 1_000_000.0
        );

        System.out.printf(
            "Parallel time: %.3f ms%n",
            parallelTime / 1_000_000.0
        );

        System.out.println(
            "Results match: " + (sequentialCount == parallelCount)
        );
    }
}
