
import javax.swing.JPanel;
import javax.swing.Timer;

public class simulationLoop {

    private final world world;
    private final JPanel panel;
    private Timer timer;

    public simulationLoop(world world, JPanel panel) {
        this.world = world;
        this.panel = panel;
    }

    public void start() {
        if (timer != null && timer.isRunning()) {
            return;
        }

        timer = new Timer(5000, event -> {
            world.tick();
            panel.repaint();
        });
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }
}
