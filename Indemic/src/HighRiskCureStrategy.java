
import java.util.Comparator;
import java.util.List;

public class HighRiskCureStrategy implements CureStrategy {

    @Override
    public CureTarget selectTarget(List<CureTarget> candidates) {
        return candidates.stream()
            .max(Comparator.comparingInt(
                (CureTarget c) -> c.nearbyInfected
            ).thenComparingInt(
                c -> -c.entity.getInfectionCountdown()
            ))
            .orElse(null);
    }

    @Override
    public String name() {
        return "High risk (infected neighbours)";
    }
}
