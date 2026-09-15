
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class world {

    private List<section> sections;
    private section[][] sectionGrid;
    private int infectionThreshold = 10;
    private double infectionChance = 0.3;
    private int dnaPoints;
    private int ticks;
    private section startingSection;
    private String statusMessage = "Select a section to start the infection";
    private Random random = new Random();

    public world(double width, double height) {
        double sectionWidth = width / 2;
        double sectionHeight = height / 2;

        sections = new ArrayList<>();
        sectionGrid = new section[2][2];

        // Create 2x2 grid of sections, each with a 5x5 grid of cells and a sparse starting population so there is still room to move.
        section s00 = new section(0.0, 0.0, sectionWidth, sectionHeight, 5, 5, 15);
        section s01 = new section(sectionWidth, 0.0, sectionWidth, sectionHeight, 5, 5, 15);
        section s10 = new section(0.0, sectionHeight, sectionWidth, sectionHeight, 5, 5, 15);
        section s11 = new section(sectionWidth, sectionHeight, sectionWidth, sectionHeight, 5, 5, 15);

        sections.add(s00);
        sections.add(s01);
        sections.add(s10);
        sections.add(s11);

        sectionGrid[0][0] = s00;
        sectionGrid[0][1] = s01;
        sectionGrid[1][0] = s10;
        sectionGrid[1][1] = s11;

    }

    public boolean exposeCell(int x, int y) throws InvalidPositionException {
        for (section selectedSection : sections) {

            int sectionX = (int) selectedSection.getX();
            int sectionY = (int) selectedSection.getY();
            int sectionWidth = (int) selectedSection.getWidth();
            int sectionHeight = (int) selectedSection.getHeight();

            if (x >= sectionX && x < sectionX + sectionWidth
                    && y >= sectionY && y < sectionY + sectionHeight) {

                int localX = x - sectionX;
                int localY = y - sectionY;

                int cellWidth = sectionWidth / selectedSection.getGridCols();
                int cellHeight = sectionHeight / selectedSection.getGridRows();

                int cellCol = localX / cellWidth;
                int cellRow = localY / cellHeight;

                return selectedSection.exposeCell(cellRow, cellCol);
            }
        }

        return false;
    }

    public void tick() {
        if (startingSection == null) {
            return;
        }

        ticks++;
        if (ticks % 4 == 0) {
            dnaPoints++;
            statusMessage = "DNA point earned - choose an upgrade";
        }

        // Tick all sections
        for (section currentSection : sections) {
            currentSection.tick();
        }

        // Check for inter-section infection spread
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                section currentSection = sectionGrid[row][col];

                int infectedCount = currentSection.getTotalInfectedCount();
                int totalCount = currentSection.getTotalEntityCount();
                boolean sectionIsFullyInfected = totalCount > 0 && infectedCount == totalCount;

                if (infectedCount >= infectionThreshold || sectionIsFullyInfected) {
                    // Spread to adjacent sections
                    spreadToAdjacentSection(row, col, 0, -1); // Up
                    spreadToAdjacentSection(row, col, 0, 1);  // Down
                    spreadToAdjacentSection(row, col, -1, 0); // Left
                    spreadToAdjacentSection(row, col, 1, 0);  // Right
                }
            }
        }
    }

    private void spreadToAdjacentSection(int currentRow, int currentCol, int dRow, int dCol) {
        int newRow = currentRow + dRow;
        int newCol = currentCol + dCol;

        // Boundary check
        if (newRow >= 0 && newRow < 2 && newCol >= 0 && newCol < 2) {
            section adjacentSection = sectionGrid[newRow][newCol];

            // Only spread if the adjacent section still has room to accept new infected entities.
            if (adjacentSection.getTotalEntityCount() < 20) {
                // Add 2-3 infected entities to the adjacent section
                int infectedToAdd = random.nextInt(2) + 2; // 2-3 entities
                adjacentSection.spreadInfectionToEmptyCells(infectedToAdd);
            }
        }
    }

    public List<section> getSections() {
        return sections;
    }

    public boolean selectStartingSection(int row, int col) {
        if (startingSection != null || row < 0 || row >= 2 || col < 0 || col >= 2) {
            return false;
        }

        startingSection = sectionGrid[row][col];
        startingSection.setInitialInfection();
        statusMessage = "Infection started in section " + (row * 2 + col + 1);
        return true;
    }

    public boolean upgradeInfectivity() {
        if (dnaPoints < 1) {
            statusMessage = "You need a DNA point for this upgrade";
            return false;
        }

        dnaPoints--;
        infectionChance = Math.min(0.8, infectionChance + 0.1);
        for (section currentSection : sections) {
            currentSection.setInfectionChance(infectionChance);
        }
        statusMessage = "Infectivity upgraded to " + (int) (infectionChance * 100) + "%";
        return true;
    }

    public boolean upgradeTransmission() {
        if (dnaPoints < 1) {
            statusMessage = "You need a DNA point for this upgrade";
            return false;
        }

        dnaPoints--;
        infectionThreshold = Math.max(3, infectionThreshold - 2);
        statusMessage = "Transmission threshold reduced to " + infectionThreshold;
        return true;
    }

    public int getDnaPoints() {
        return dnaPoints;
    }

    public double getInfectionChance() {
        return infectionChance;
    }

    public int getInfectionThreshold() {
        return infectionThreshold;
    }

    public boolean hasStarted() {
        return startingSection != null;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean rescueInfectedEntity(section targetSection, int row, int col) {

        GridCell<Entity> cell = targetSection.getGridCell(row, col);

        for (Entity entity : cell.getEntities()) {
            if (entity.rescue()) {
                statusMessage = "Infected entity rescued!";
                return true;
            }
        }

        statusMessage = "No infected entity available in this cell";
        return false;
    }
}
