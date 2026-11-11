
import javax.swing.JFrame;

public class App {

    public static void main(String[] args) {

        world world = new world(1200, 600);

        JFrame frame = new JFrame("Indemic - Plague Simulation");

        simulationPanel panel = new simulationPanel(world);

        frame.add(panel);

        frame.setSize(1200, 1000);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        simulationLoop loop = new simulationLoop(world, panel);
        loop.start();
    }
}
