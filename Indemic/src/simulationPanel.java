
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import javax.swing.JPanel;

public class simulationPanel extends JPanel {

    private world world;
    private Upgrade infectivityUpgrade;
    private Upgrade transmissionUpgrade;
    private boolean exposureMode = false;
    private Map<String, Character> rescueKeys = new HashMap<>();
    private Random keyRandom = new Random();
    private final String AVAILABLE_KEYS = "ABCDFGHIJKLMNOPQRSTUVWXYZ";

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
                    return;
                }
                char pressedKey
                        = Character.toUpperCase(event.getKeyChar());

                handleRescueKey(pressedKey);

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
        drawHud(g2d);
        totalEntities = 0;
    }

    private void handleClick(int x, int y) {

        // Exposure mode - only inside simulation grid
        if (exposureMode && world.hasStarted() && y < 600) {
            try {
                world.exposeCell(x, y);
            } catch (InvalidPositionException e) {
                System.out.println(
                        "Exposure failed: " + e.getMessage()
                );
            }

            repaint();
            return;
        }

        // Select starting section
        if (!world.hasStarted() && x < 1200 && y < 600) {

            int col = x / 600;
            int row = y / 300;

            world.selectStartingSection(row, col);
        } // HUD upgrade buttons
        else if (y >= 645 && y <= 690) {

            if (x >= 10 && x <= 210) {
                infectivityUpgrade.apply(world);

            } else if (x >= 220 && x <= 420) {
                transmissionUpgrade.apply(world);
            }
        }

        repaint();
    }

    private void drawHud(Graphics2D g) {

        // HUD background directly below the 600px simulation grid
        g.setColor(new Color(250, 250, 250));
        g.fillRect(0, 600, getWidth(), 180);

        g.setColor(Color.BLACK);

        // Main statistics
        g.drawString("Total Entities: " + totalEntities, 10, 625);
        g.drawString("DNA Points: " + world.getDnaPoints(), 150, 625);
        g.drawString(
                "Infectivity: " + (int) (world.getInfectionChance() * 100) + "%",
                270, 625
        );
        g.drawString(
                "Section Spread Threshold: " + world.getInfectionThreshold(),
                400, 625
        );

        // Exposure mode
        if (exposureMode) {
            g.drawString(
                    "Exposure Mode: ON - Click a healthy cell to infect it",
                    650, 625
            );
        } else {
            g.drawString(
                    "Exposure Mode: OFF - Press E to activate",
                    650, 625
            );
        }

        // Upgrade buttons
        drawButton(g, 10, 645, 200, 45, "Upgrade Infectivity");
        drawButton(g, 220, 645, 200, 45, "Upgrade Transmission");

        // Current game message
        g.setColor(Color.BLACK);
        g.drawString("Status: " + world.getStatusMessage(), 450, 672);

        // Legend
        g.setColor(new Color(70, 150, 70));
        g.fillRect(10, 710, 15, 15);

        g.setColor(Color.BLACK);
        g.drawString("Enhanced Healthy", 35, 722);

        g.setColor(new Color(120, 180, 255));
        g.fillRect(180, 710, 15, 15);

        g.setColor(Color.BLACK);
        g.drawString("Defender", 205, 722);

        g.setColor(Color.BLACK);
        g.fillRect(300, 710, 15, 15);

        g.setColor(Color.BLACK);
        g.drawString("Dead", 325, 722);

        g.setColor(new Color(255, 200, 200));
        g.fillRect(380, 710, 15, 15);

        g.setColor(Color.BLACK);
        g.drawString("Infected", 405, 722);

        // Initial instruction
        if (!world.hasStarted()) {
            g.setColor(new Color(255, 255, 255, 210));
            g.fillRect(0, 0, getWidth(), 50);

            g.setColor(Color.BLACK);
            g.drawString(
                    "Click a section to choose where the infection begins",
                    15, 30
            );
        }
    }

    private void drawButton(Graphics2D g, int x, int y, int width, int height, String label) {
        g.setColor(new Color(220, 230, 240));
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        g.drawString(label, x + 15, y + 30);
    }

    private char getRescueKey(section targetSection, int row, int col) {

        String cellId = targetSection.getX() + "-"
                + targetSection.getY() + "-"
                + row + "-" + col;

        // Keep the same key if this cell already has one
        if (rescueKeys.containsKey(cellId)) {
            return rescueKeys.get(cellId);
        }

        HashSet<Character> usedKeys
                = new HashSet<>(rescueKeys.values());

        String available = "";

        for (int i = 0; i < AVAILABLE_KEYS.length(); i++) {
            char key = AVAILABLE_KEYS.charAt(i);

            if (!usedKeys.contains(key)) {
                available += key;
            }
        }

        if (available.length() == 0) {
            return '?';
        }

        char selectedKey
                = available.charAt(keyRandom.nextInt(available.length()));

        rescueKeys.put(cellId, selectedKey);

        return selectedKey;
    }

    private void handleRescueKey(char pressedKey) {

        for (section currentSection : world.getSections()) {

            for (int row = 0; row < currentSection.getGridRows(); row++) {
                for (int col = 0; col < currentSection.getGridCols(); col++) {

                    // Scoreboard is not a rescue cell
                    if (row == 0 && col == 0) {
                        continue;
                    }

                    String cellId = currentSection.getX() + "-"
                            + currentSection.getY() + "-"
                            + row + "-" + col;

                    if (rescueKeys.containsKey(cellId)
                            && rescueKeys.get(cellId) == pressedKey) {

                        world.rescueInfectedEntity(
                                currentSection, row, col);

                        rescueKeys.remove(cellId);

                        repaint();
                        return;
                    }
                }
            }
        }
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

                int healthyCount = gridCell.getHealthyCount();
                int infectedCount = gridCell.getInfectedCount();
                int enhancedHealthyCount = gridCell.getEnhancedHealthyCount();
                int defenderCount = gridCell.getDefenderCount();
                int deadCount = gridCell.getDeadCount();
                int totalCount = gridCell.getEntityCount();

                boolean scoreboardCell = (row == 0 && col == 0);
                String cellId = section.getX() + "-"
                        + section.getY() + "-"
                        + row + "-" + col;

                if (infectedCount == 0) {
                    rescueKeys.remove(cellId);
                }
                // Choose cell colour
                if (scoreboardCell) {
                    g.setColor(new Color(190, 150, 100)); // Brown scoreboard
                } else if (totalCount == 0) {
                    g.setColor(new Color(240, 240, 240)); // Empty
                } else if (deadCount == totalCount) {
                    g.setColor(Color.BLACK); // Black only if everyone is dead
                } else if (defenderCount > 0) {
                    g.setColor(new Color(120, 180, 255)); // Defender
                } else if (enhancedHealthyCount > 0) {
                    g.setColor(new Color(70, 150, 70)); // Enhanced healthy
                } else if (infectedCount > healthyCount) {
                    g.setColor(new Color(255, 200, 200)); // Infected majority
                } else if (healthyCount > 0) {
                    g.setColor(new Color(200, 255, 200)); // Healthy
                } else {
                    g.setColor(new Color(240, 240, 240));
                }

                // Actually paint the cell
                g.fillRect(x, y, cellWidth, cellHeight);

                // Cell border
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1));
                g.drawRect(x, y, cellWidth, cellHeight);
                // Show rescue key and countdown for infected gameplay cells
                if (!scoreboardCell && infectedCount > 0) {

                    char rescueKey = getRescueKey(section, row, col);

                    // Find an infected entity in this cell
                    Entity infectedEntity = null;

                    for (Entity entity : gridCell.getEntities()) {
                        if (entity.getState() == cellState.INFECTED) {
                            infectedEntity = entity;
                            break;
                        }
                    }

                    if (infectedEntity != null) {

                        g.setColor(Color.BLACK);

                        // Big rescue key
                        g.setFont(g.getFont().deriveFont(22f));
                        g.drawString("[" + rescueKey + "]",
                                x + cellWidth / 2 - 15,
                                y + cellHeight / 2);

                        // Countdown
                        g.setFont(g.getFont().deriveFont(12f));
                        g.drawString("Time: " + infectedEntity.getInfectionCountdown(),
                                x + 5,
                                y + cellHeight - 8);
                    }
                }
                if (totalCount > 0) {
                    totalEntities += totalCount;
                }
            }
        }

        // Thick section border
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(4));
        g.drawRect(sectionX, sectionY, sectionWidth, sectionHeight);

        // Scoreboard text inside brown top-left cell
        int statsX = sectionX + 5;
        int statsY = sectionY + 15;

        g.setColor(Color.BLACK);
        g.drawString("Healthy: " + section.getTotalHealthyCount(), statsX, statsY);
        g.drawString("Infected: " + section.getTotalInfectedCount(), statsX, statsY + 11);
        g.drawString("Enhanced healthy: " + section.getTotalEnhancedHealthyCount(), statsX, statsY + 22);
        g.drawString("Defenders: " + section.getTotalDefenderCount(), statsX, statsY + 33);
        g.drawString("Dead: " + section.getTotalDeadCount(), statsX, statsY + 44);
    }
}
