import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

public class simulationPanel extends JPanel {
    private world world;
    private Upgrade infectivityUpgrade;
    private Upgrade transmissionUpgrade;
    private boolean exposureMode = false;

    public simulationPanel(world world) {
        this.world = world;
        this.infectivityUpgrade = new InfectivityUpgrade();
        this.transmissionUpgrade = new TransmissionUpgrade();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                    requestFocusInWindow();
                handleClick(event.getX(), event.getY());
            }
        });
        setFocusable(true);

addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_E) {
            exposureMode = !exposureMode;
            repaint();
        }
    }
});
    }

    int totalEntities;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;

        // Draw grid for each section
        for (section section : world.getSections()) {
            drawSection(g2d, section);
        }

        // Draw statistics
        g2d.setColor(Color.BLACK);
        g2d.drawString("Total Entities: " + totalEntities, 10, getHeight() - 20);
        g2d.drawString("Simulation Grid View - Each cell shows entity count", 10, getHeight() - 5);
        drawHud(g2d);
        totalEntities = 0;
    }

    private void handleClick(int x, int y) {
        if (exposureMode && world.hasStarted() && y < 800) {
        world.exposeCell(x, y);
        repaint();
        return;
        }
        if (!world.hasStarted() && x < 1200 && y < 800) {
            int col = x / 600;
            int row = y / 400;
            world.selectStartingSection(row, col);
        } else if (y >= 850 && y <= 900) {
            if (x >= 10 && x <= 210) {
                infectivityUpgrade.apply(world);
            } else if (x >= 220 && x <= 420) {
                transmissionUpgrade.apply(world);
            }
        }
        repaint();
    }

    private void drawHud(Graphics2D g) {
        g.fillRect(0, 820, getWidth(), 130);
        g.setColor(new Color(250, 250, 250));
        g.fillRect(0, 820, getWidth(), 130);
        g.setColor(Color.BLACK);
        g.drawString("DNA points: " + world.getDnaPoints(), 10, 840);
        g.drawString("Infectivity: " + (int) (world.getInfectionChance() * 100) + "%", 150, 840);
        g.drawString("Section spread threshold: " + world.getInfectionThreshold(), 285, 840);
        if (exposureMode) {
        g.drawString("Exposure Mode: ON - Click a healthy cell to infect it", 850, 840);
        } else {
       g.drawString("Exposure Mode: OFF - Press E to activate", 850, 840);
       }

        drawButton(g, 10, 850, 200, 50, "Upgrade Infectivity");
        drawButton(g, 220, 850, 200, 50, "Upgrade Transmission");
        g.drawString(world.getStatusMessage(), 450, 880);
        g.setColor(new Color(70, 150, 70));
        g.fillRect(450, 900, 15, 15);
        g.setColor(Color.BLACK);
        g.drawString("Enhanced healthy", 475, 912);
        g.setColor(new Color(120, 180, 255));
        g.fillRect(620, 900, 15, 15);
        g.setColor(Color.BLACK);
        g.drawString("Defender", 645, 912);
        g.setColor(Color.BLACK);
        g.fillRect(760, 900, 15, 15);
        g.setColor(Color.BLACK);
        g.drawString("Dead", 785, 912);

        if (!world.hasStarted()) {
            g.setColor(new Color(255, 255, 255, 210));
            g.fillRect(0, 0, getWidth(), 50);
            g.setColor(Color.BLACK);
            g.drawString("Click a section to choose where the infection begins", 15, 30);
        }
    }

    private void drawButton(Graphics2D g, int x, int y, int width, int height, String label) {
        g.setColor(new Color(220, 230, 240));
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        g.drawString(label, x + 15, y + 30);
    }

    private void drawSection(Graphics2D g, section section) {
        int sectionX = (int) section.getX();
        int sectionY = (int) section.getY();
        int sectionWidth = (int) section.getWidth();
        int sectionHeight = (int) section.getHeight();

        int gridRows = section.getGridRows();
        int gridCols = section.getGridCols();

        int cellWidth = sectionWidth / gridCols;
        int cellHeight = sectionHeight / gridRows;

        // Draw grid cells
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                GridCell<Entity> gridCell = section.getGridCell(row, col);
                int x = sectionX + col * cellWidth;
                int y = sectionY + row * cellHeight;

                // Draw cell background based on infection status
                int healthyCount = gridCell.getHealthyCount();
                int infectedCount = gridCell.getInfectedCount();
                int enhancedHealthyCount = gridCell.getEnhancedHealthyCount();
                int defenderCount = gridCell.getDefenderCount();
                int deadCount = gridCell.getDeadCount();
                int totalCount = gridCell.getEntityCount();

                if (totalCount == 0) {
                    g.setColor(new Color(240, 240, 240)); // Light gray for empty
                } else if (deadCount > 0) {
                    g.setColor(Color.BLACK);
                 } else if (defenderCount > 0) {
                    g.setColor(new Color(120, 180, 255)); // Blue for defenders
                } else if (enhancedHealthyCount > 0) {
                    g.setColor(new Color(70, 150, 70)); // Dark green for enhanced healthy
                } else if (infectedCount > healthyCount) {
                    g.setColor(new Color(255, 200, 200)); // Light red for infected majority
                } else if (healthyCount > 0) {
                    g.setColor(new Color(200, 255, 200)); // Light green for healthy majority
                }
                g.fillRect(x, y, cellWidth, cellHeight);
                // Draw cell border (thin)
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1));
                g.drawRect(x, y, cellWidth, cellHeight);

                // Track entity count (no text display since each cell has exactly 1 entity)
                if (totalCount > 0) {
                    totalEntities += totalCount;
                }
            }
        }

        // Draw thick section border
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4)); // Thick border for section
        g.drawRect(sectionX, sectionY, sectionWidth, sectionHeight);

        // Draw section statistics
        int statsX = sectionX + 5;
        int statsY = sectionY  + 15;

        g.setColor(Color.BLACK);
        g.drawString("Healthy: " + section.getTotalHealthyCount(), statsX, statsY);
        g.drawString("Infected: " + section.getTotalInfectedCount(), statsX, statsY + 11);
        g.drawString("Enhanced healthy: " + section.getTotalEnhancedHealthyCount(), statsX, statsY + 22);
        g.drawString("Defenders: " + section.getTotalDefenderCount(), statsX, statsY + 33);
        g.drawString("Dead: " + section.getTotalDeadCount(), statsX, statsY + 44);
    }
}
