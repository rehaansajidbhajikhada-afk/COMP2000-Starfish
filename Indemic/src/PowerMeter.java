
import java.util.ArrayList;
import java.util.List;

public class PowerMeter {

    private int power = 0;

    private final List<PowerObserver> observers = new ArrayList<>();

    public void addObserver(PowerObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(PowerObserver observer) {
        observers.remove(observer);
    }

    public int getPower() {
        return power;
    }

    public boolean isReady() {
        return power == 100;
    }

    public void charge(int amount) {
        if (amount <= 0) {
            return;
        }

        int previousPower = power;
        power = Math.min(100, power + amount);

        if (power != previousPower) {
            notifyObservers();
        }
    }

    public boolean consume() {
        if (!isReady()) {
            return false;
        }

        power = 0;
        notifyObservers();
        return true;
    }

    private void notifyObservers() {
        for (PowerObserver observer : observers) {
            observer.onPowerChanged(power);
        }
    }
}

