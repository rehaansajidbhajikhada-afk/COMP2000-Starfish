
import java.util.List;

public interface CureStrategy {
    CureTarget selectTarget(List<CureTarget> candidates);
    String name();
}
