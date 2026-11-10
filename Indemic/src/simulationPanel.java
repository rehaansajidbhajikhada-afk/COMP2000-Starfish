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
    private final AudioManager audio = new AudioManager();
private String audioPhase = "";
private long gameOverSoundStart = 0;
private boolean laughPlayed = false;
private long emergencyDeadline = 0;
private int emergencyTimeLeft = 3;
private long gameOverAnimationStart = 0;
    private world world;
    private Upgrade infectivityUpgrade;
    private Upgrade transmissionUpgrade;
    private boolean exposureMode = false;
    private Map<String, Character> rescueKeys = new HashMap<>();
    private Random keyRandom = new Random();
    private final String AVAILABLE_KEYS = "ADFGHIJKLMNOPQRSTUVWXYZ4567890";
    private int displayedPower = 0;
    private long overloadDeadline = 0;
private int overloadTimeLeft = 5;
private boolean overloadWarning = false;
    private final java.util.Set<Integer> heldKeys =
    new java.util.HashSet<>();

private long oneKeyWarningUntil = 0;
private String boosterAlert = "";
    public simulationPanel(world world) {
        this.world = world;
        this.infectivityUpgrade = new InfectivityUpgrade();
        this.transmissionUpgrade = new TransmissionUpgrade();
        
// Observer 1: Update HUD power
world.getPowerMeter().addObserver(power -> {
    displayedPower = power;
    repaint();
});

// Observer 2: Display a ready notification
world.getPowerMeter().addObserver(power -> {
    if (power == 100) {
        boosterAlert = "BOOSTER READY! Press B";
    } else {
        boosterAlert = "";
    }
});
addFocusListener(new java.awt.event.FocusAdapter() {
    @Override
    public void focusLost(java.awt.event.FocusEvent event) {
        heldKeys.clear();
    }
});

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                requestFocusInWindow();
                handleClick(event.getX(), event.getY());
            }
        });
        setFocusable(true);

javax.swing.Timer emergencyTimer = new javax.swing.Timer(50, event -> {
    updateGameAudio();
    updateRescueKeyCapacity();

    if (world.isGameOver()) {
        emergencyDeadline = 0;
        repaint();
        return;
    }

    if (world.isEmergencyActive()) {

        if (emergencyDeadline == 0) {
            emergencyDeadline =
                System.nanoTime() + 3_000_000_000L;
        }

        long remaining =
            emergencyDeadline - System.nanoTime();

        emergencyTimeLeft = (int) Math.max(
            0,
            (remaining + 999_999_999L) / 1_000_000_000L
        );

        if (remaining <= 0) {
            world.failEmergency();
            emergencyDeadline = 0;
        }

        repaint();

    } else {
        emergencyDeadline = 0;
        emergencyTimeLeft = 3;
    }
});

emergencyTimer.start();

       
addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent event) {
        int code = event.getKeyCode();

// Ignore key auto-repeat while held
if (heldKeys.contains(code)) {
    return;
}

// Multiple simultaneously held keys are rejected
if (!heldKeys.isEmpty()) {
    oneKeyWarningUntil =
        System.nanoTime() + 1_000_000_000L;

    repaint();
    return;
}

heldKeys.add(code);
if (world.isGameOver()) {
    return;
}

if (world.isEmergencyActive()) {
    world.handleEmergencyKey(event.getKeyChar());
    repaint();
    return;
}
        // E = Exposure Mode
        if (event.getKeyCode() == KeyEvent.VK_E) {
            exposureMode = !exposureMode;
            repaint();
            return;
        }

        // 1 = Urgent Cure
        if (event.getKeyCode() == KeyEvent.VK_1) {
            world.setCureStrategy(new UrgentCureStrategy());
            repaint();
            return;
        }

        // 2 = High Risk Cure
        if (event.getKeyCode() == KeyEvent.VK_2) {
            world.setCureStrategy(new HighRiskCureStrategy());
            repaint();
            return;
        }

        // 3 = Random Cure
        if (event.getKeyCode() == KeyEvent.VK_3) {
            world.setCureStrategy(new RandomCureStrategy());
            repaint();
            return;
        }

        // C = Automatic Cure
        if (event.getKeyCode() == KeyEvent.VK_C) {
            world.autoRescue();
            repaint();
            return;
        }
        // B = Power Booster
if (event.getKeyCode() == KeyEvent.VK_B) {
    world.activateBooster();
    repaint();
    return;
}

        // Existing manual rescue keys
        char pressedKey =
                Character.toUpperCase(event.getKeyChar());

        handleRescueKey(pressedKey);
    }
    @Override
public void keyReleased(KeyEvent event) {
    heldKeys.remove(event.getKeyCode());
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
        if (world.isEmergencyActive()) {
    drawEmergencyOverlay(g2d);
} if (world.isGameOver()) {
    drawGameOverOverlay(g2d);
} 
if (overloadWarning
        && !world.isGameOver()
        && !world.isEmergencyActive()) {

    g2d.setColor(new Color(180, 0, 0, 220));

    int boxWidth = 540;
    int boxHeight = 110;
    int x = (getWidth() - boxWidth) / 2;
    int y = 200;

    g2d.fillRoundRect(
        x, y, boxWidth, boxHeight, 20, 20
    );

    g2d.setColor(Color.WHITE);
    g2d.setFont(new java.awt.Font(
        "SansSerif", java.awt.Font.BOLD, 26
    ));

    g2d.drawString(
        "RESCUE SYSTEM OVERLOADED!",
        x + 32, y + 42
    );

    g2d.setFont(new java.awt.Font(
        "SansSerif", java.awt.Font.BOLD, 22
    ));

    g2d.drawString(
        "FREE A KEY: " + overloadTimeLeft + "s",
        x + 150, y + 82
    );
}

if (!world.isGameOver()
        && System.nanoTime() < oneKeyWarningUntil) {

    g2d.setColor(new Color(200, 20, 20));
    g2d.setFont(new java.awt.Font(
        "SansSerif", java.awt.Font.BOLD, 28
    ));

    String warning = "PRESS ONE KEY AT A TIME!";

    int x = (
        getWidth() - g2d.getFontMetrics().stringWidth(warning)
    ) / 2;

    g2d.drawString(warning, x, 130);
}
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
g.setColor(Color.BLACK);

g.drawString(
    "Outbreak Stage: " + world.getOutbreakStage(),
    1080, 625
);

g.drawString(
    "Rescue Keys: " + rescueKeys.size()
        + "/" + AVAILABLE_KEYS.length(),
    1050, 755
);
g.drawString(
    "Selected Strategy: " + world.getCureStrategyName(),
    650, 755
);


        // HUD background directly below the 600px simulation grid
        g.setColor(new Color(250, 250, 250));
        g.fillRect(0, 600, getWidth(), 180);
        g.setFont(new java.awt.Font(
    "SansSerif", java.awt.Font.PLAIN, 13
));

        g.setColor(Color.BLACK);

        // Main statistics
        g.drawString("Total Entities: " + totalEntities, 10, 625);
        g.drawString("DNA Points: " + world.getDnaPoints(), 150, 625);
        g.setColor(Color.BLACK);

g.drawString(
    "Total Infected: " + world.getWorldInfectedCount(),
    650, 685
);

g.drawString(
    "Total Dead: " + world.getWorldDeadCount(),
    850, 685
);
java.util.Map<cellState, Long> stats =
    world.getPopulationStatistics();

g.setColor(Color.BLACK);

g.drawString(
    "Healthy: " + stats.getOrDefault(cellState.HEALTHY, 0L),
    950, 685
);

g.drawString(
    "Defenders: " + stats.getOrDefault(cellState.DEFENDER, 0L),
    1050, 685
); 
java.awt.Font originalFont = g.getFont();

g.setColor(Color.BLACK);
g.setFont(new java.awt.Font(
    "SansSerif", java.awt.Font.BOLD, 18
));

g.drawString(
    "SCORE: " + world.getScore(),
    1050, 660
);

// Restore font so other HUD labels stay normal
g.setFont(originalFont);

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
        
// Power Booster HUD
g.setColor(Color.BLACK);
g.setFont(g.getFont().deriveFont(13f));

g.drawString("Power: " + displayedPower + "%", 650, 705);

// Background bar
g.setColor(Color.LIGHT_GRAY);
g.fillRect(650, 712, 250, 14);

// Filled power bar
g.setColor(new Color(50, 180, 90));
g.fillRect(650, 712, displayedPower * 250 / 100, 14);

// Bar border
g.setColor(Color.BLACK);
g.drawRect(650, 712, 250, 14);

// Booster notification
g.drawString(boosterAlert, 650, 745);

        // Cure strategy controls
g.setColor(Color.BLACK);
g.setFont(g.getFont().deriveFont(13f));

g.drawString(
    "AUTO CURE: [1] Urgent  [2] High Risk  [3] Random  [C] Cure (2 DNA)",
    10, 755
);

g.drawString(
    "Selected: " + world.getCureStrategyName(),
    10, 775
);

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

private void drawEmergencyOverlay(Graphics2D g) {
    g.setColor(new Color(70, 0, 0, 220));
    g.fillRect(0, 0, getWidth(), getHeight());

    g.setColor(Color.WHITE);
    g.setFont(g.getFont().deriveFont(32f));
    g.drawString("CRITICAL EMERGENCY!", 380, 250);

    String sequence = world.getEmergencySequence();
    int progress = world.getEmergencyProgress();

    g.setFont(g.getFont().deriveFont(48f));

    for (int i = 0; i < sequence.length(); i++) {
        if (i < progress) {
            g.setColor(Color.GREEN);
        } else if (i == progress) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.WHITE);
        }

        g.drawString(
            String.valueOf(sequence.charAt(i)),
            410 + i * 90,
            360
        );
        
    }

    g.setColor(Color.WHITE);
    g.setFont(g.getFont().deriveFont(18f));
    g.drawString("Type the highlighted letters in order!", 400, 420);

g.setColor(emergencyTimeLeft <= 2
    ? Color.RED
    : Color.YELLOW);

g.setFont(g.getFont().deriveFont(
    java.awt.Font.BOLD, 36f
));

String countdown = "TIME LEFT: " + emergencyTimeLeft + "s";

int textX = (
    getWidth() - g.getFontMetrics().stringWidth(countdown)
) / 2;

g.drawString(countdown, textX, 490);

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

                boolean hasRescuableInfected = false;

for (Entity entity : gridCell.getEntities()) {
    if (entity.getState() == cellState.INFECTED
            && entity.isRescueAvailable()) {
        hasRescuableInfected = true;
        break;
    }
}

if (!hasRescuableInfected) {
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
                if (!scoreboardCell && hasRescuableInfected){
                    char rescueKey = getRescueKey(section, row, col);

                    // Find an infected entity in this cell
                    Entity infectedEntity = null;

                    for (Entity entity : gridCell.getEntities()) {
    if (entity.getState() == cellState.INFECTED
            && entity.isRescueAvailable()) {
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

private void drawGameOverOverlay(Graphics2D g) {

    if (gameOverAnimationStart == 0) {
        gameOverAnimationStart = System.nanoTime();
    }

    double seconds = (System.nanoTime()
        - gameOverAnimationStart) / 1_000_000_000.0;

    int width = getWidth();
    int height = getHeight();

    // Red game-over background
    g.setColor(new Color(100, 0, 0));
    g.fillRect(0, 0, width, height);

    // Pulsing red background
    int pulse = (int) (20 + 15 * Math.sin(seconds * 4));
    g.setColor(new Color(160, 0, 0, pulse));
    g.fillOval(
        width / 2 - 300,
        height / 2 - 300,
        600, 600
    );

    // Game-over heading
    g.setFont(new java.awt.Font(
        "SansSerif", java.awt.Font.BOLD, 65
    ));
    g.setColor(Color.WHITE);

    String title = "GAME OVER";
    int titleX = (width - g.getFontMetrics()
        .stringWidth(title)) / 2;

    g.drawString(title, titleX, 145);

    // Zombies walk toward centre for 2.5 seconds
    double progress = Math.min(1.0, seconds / 2.5);

    int center = width / 2;
    int groundY = height / 2 + 150;

    int[] startX = {-120, width + 120, -200};
    int[] endX = {center - 170, center + 170, center};

    for (int i = 0; i < 3; i++) {

        int zombieX = (int) (
            startX[i] + (endX[i] - startX[i]) * progress
        );

        boolean dancing = seconds >= 2.5;

        drawZombie(
            g,
            zombieX,
            groundY,
            seconds,
            dancing,
            i
        );
    }

    // Text after walking finishes
    if (seconds >= 2.5) {

        g.setColor(Color.YELLOW);
        g.setFont(new java.awt.Font(
            "SansSerif", java.awt.Font.BOLD, 38
        ));

        String laugh = "HA! HA! HA!";
        int laughX = (width - g.getFontMetrics()
            .stringWidth(laugh)) / 2;

        g.drawString(laugh, laughX, height - 105);
    }

    g.setColor(Color.WHITE);
    g.setFont(new java.awt.Font(
        "SansSerif", java.awt.Font.PLAIN, 19
    ));

    String subtitle = "The zombies have taken over!";
    int subtitleX = (width - g.getFontMetrics()
        .stringWidth(subtitle)) / 2;

    g.drawString(subtitle, subtitleX, height - 50);
}

private void drawZombie(
        Graphics2D g,
        int x,
        int y,
        double time,
        boolean dancing,
        int index) {

    Graphics2D z = (Graphics2D) g.create();

    // Walking or dancing movement
    double movement = Math.sin(time * 12 + index * 2);

    int bounce = dancing
        ? (int) (Math.abs(movement) * 22)
        : (int) (Math.abs(movement) * 7);

    z.translate(x, y - bounce);

    // Zombie colours
    Color skin = new Color(95, 165, 85);
    Color shirt = new Color(65, 75, 115);

    if (index == 1) {
        skin = new Color(140, 175, 90);
        shirt = new Color(115, 65, 75);
    } else if (index == 2) {
        skin = new Color(85, 145, 125);
        shirt = new Color(90, 70, 125);
    }

    z.setStroke(new BasicStroke(
        8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND
    ));

    // Legs
    z.setColor(new Color(45, 45, 65));

    int legSwing = (int) (movement * 20);

    z.drawLine(-16, 20, -20 - legSwing, 75);
    z.drawLine(16, 20, 20 + legSwing, 75);

    // Body
    z.setColor(shirt);
    z.fillRoundRect(-34, -85, 68, 110, 14, 14);

    // Dancing arms wave more dramatically
    int armSwing = dancing
        ? (int) (movement * 35)
        : 10;

    z.setColor(skin);

    z.drawLine(-30, -65, -65, -30 - armSwing);
    z.drawLine(30, -65, 65, -30 + armSwing);

    // Head
    z.setColor(skin);
    z.fillOval(-37, -155, 74, 75);

    // Eyes
    z.setColor(Color.WHITE);
    z.fillOval(-24, -130, 18, 19);
    z.fillOval(8, -130, 18, 19);

    // Red pupils
    z.setColor(Color.RED);
    z.fillOval(-18, -124, 9, 10);
    z.fillOval(14, -124, 9, 10);

    // Zombie mouth
    z.setColor(new Color(45, 20, 20));
    z.fillRoundRect(-19, -105, 38, 15, 8, 8);

    // Teeth
    z.setColor(Color.WHITE);

    for (int tooth = -12; tooth <= 12; tooth += 12) {
        z.fillRect(tooth, -104, 5, 7);
    }

    z.dispose();
}

private void updateGameAudio() {

    String nextPhase;

    if (world.isGameOver()) {
        nextPhase = "GAME_OVER";

    } else if (world.isEmergencyActive()) {
        nextPhase = "EMERGENCY";

    } else if (!world.hasStarted()) {
        nextPhase = "WAITING";

    } else {
        nextPhase = "NORMAL";
    }

    // Only change audio when game state changes
    if (!nextPhase.equals(audioPhase)) {

        audioPhase = nextPhase;

        switch (nextPhase) {

            case "WAITING":
                // No music before player starts the game
                audio.stopAll();
                break;

            case "NORMAL":
                // Starts after selecting a section,
                // or resumes after surviving an emergency
                audio.playNormal();
                gameOverSoundStart = 0;
                laughPlayed = false;
                break;

            case "EMERGENCY":
                // Stop normal music and play emergency audio
                audio.playEmergency();
                break;

            case "GAME_OVER":
                // Stop emergency music immediately
                audio.playGameOver();

                // Start delay before zombie laughter
                gameOverSoundStart = System.nanoTime();
                laughPlayed = false;
                break;
        }
    }

    // Play laughter once, after zombies finish walking
    if (world.isGameOver()
            && !laughPlayed
            && gameOverSoundStart != 0) {

        double elapsedSeconds =
            (System.nanoTime() - gameOverSoundStart)
            / 1_000_000_000.0;

        if (elapsedSeconds >= 2.5) {
            audio.playLaugh();
            laughPlayed = true;
        }
    }
}

private void updateRescueKeyCapacity() {

    if (!world.hasStarted() || world.isGameOver()
            || world.isEmergencyActive()) {
        return;
    }

    java.util.Set<String> activeCells =
        new java.util.HashSet<>();

    for (section area : world.getSections()) {
        for (int row = 0; row < area.getGridRows(); row++) {
            for (int col = 0; col < area.getGridCols(); col++) {

                // Scoreboard is not a rescue cell
                if (row == 0 && col == 0) {
                    continue;
                }

                GridCell<Entity> cell = area.getGridCell(row, col);
                if (cell == null) continue;

                boolean eligible = false;

                for (Entity entity : cell.getEntities()) {
                    if (entity.getState() == cellState.INFECTED
                            && entity.isRescueAvailable()) {
                        eligible = true;
                        break;
                    }
                }

                if (eligible) {
                    String cellId = area.getX() + "-"
                        + area.getY() + "-"
                        + row + "-" + col;

                    activeCells.add(cellId);
                }
            }
        }
    }

    // Release keys belonging to cells no longer eligible
    rescueKeys.keySet().retainAll(activeCells);

    // Assign available keys to eligible cells
    for (section area : world.getSections()) {
        for (int row = 0; row < area.getGridRows(); row++) {
            for (int col = 0; col < area.getGridCols(); col++) {

                String cellId = area.getX() + "-"
                    + area.getY() + "-"
                    + row + "-" + col;

                if (activeCells.contains(cellId)
                        && !rescueKeys.containsKey(cellId)) {
                    getRescueKey(area, row, col);
                }
            }
        }
    }

    // All available rescue keys have been allocated
 
if (rescueKeys.size() >= AVAILABLE_KEYS.length()) {

    if (!overloadWarning) {
        overloadWarning = true;
        overloadDeadline =
            System.nanoTime() + 5_000_000_000L;
    }

    long remaining =
        overloadDeadline - System.nanoTime();

    overloadTimeLeft = (int) Math.max(
        0,
        (remaining + 999_999_999L) / 1_000_000_000L
    );

    if (remaining <= 0) {
        world.endGameDueToOverload();
        overloadWarning = false;
        overloadDeadline = 0;
    }

} else {
    // Player freed a rescue key, cancel overload
    overloadWarning = false;
    overloadDeadline = 0;
    overloadTimeLeft = 5;
}

    }
}

