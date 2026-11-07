
import java.util.List;
import java.util.Random;

public class RandomCureStrategy implements CureStrategy {

    private final Random random = new Random();

    @Override
    public CureTarget selectTarget(List<CureTarget> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(
            random.nextInt(candidates.size())
        );
    }

    @Override
    public String name() {
        return "Random";
    }
}
