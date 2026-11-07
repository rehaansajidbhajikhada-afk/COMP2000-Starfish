
import java.util.Comparator;
import java.util.List;

public class UrgentCureStrategy implements CureStrategy {

    @Override
    public CureTarget selectTarget(List<CureTarget> candidates) {
        return candidates.stream()
            .min(Comparator.comparingInt(
                c -> c.entity.getInfectionCountdown()
            ))
            .orElse(null);
    }

    @Override
    public String name() {
        return "Urgent (lowest timer)";
    }
}
