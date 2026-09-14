import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class section {
    private GridCell<Entity>[][] grid;
    private double x;
    private double y;
    private double width;
    private double height;
    private int gridRows;
    private int gridCols;
    private double infectionChance = 0.3;
    private int ticks;
    private Random random = new Random();
    
@SuppressWarnings("unchecked")
    public section(double x, double y, double width, double height, int gridRows, int gridCols, int initialEntityCount) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.gridRows = gridRows;
        this.gridCols = gridCols;

        // Initialize grid
        grid = new GridCell[gridRows][gridCols];
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                grid[row][col] = new GridCell<>(col, row);
            }
        }

        // Populate with initial entities (leave some cells empty for movement)
        int entitiesAdded = 0;
        while (entitiesAdded < initialEntityCount) {
            int randomRow = random.nextInt(gridRows);
            int randomCol = random.nextInt(gridCols);
            
            if (grid[randomRow][randomCol].getEntityCount() == 0) {
                Entity entity = new Entity(randomCol, randomRow, cellState.HEALTHY);
                grid[randomRow][randomCol].addEntity(entity);
                entitiesAdded++;
            }
        }
    }

    public void setInitialInfection() {
        // Always ensure at least one infected entity exists on startup.
        // Prefer an isolated cell, but always fall back to a valid random cell if needed.
        for (int attempts = 0; attempts < 100; attempts++) {
            int row = random.nextInt(gridRows);
            int col = random.nextInt(gridCols);

            boolean adjacentToHealthy = false;
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int adjRow = row + dr;
                    int adjCol = col + dc;
                    if (adjRow >= 0 && adjRow < gridRows && adjCol >= 0 && adjCol < gridCols) {
                        if (grid[adjRow][adjCol].getEntityCount() > 0 &&
                            grid[adjRow][adjCol].getEntities().get(0).getState() == cellState.HEALTHY) {
                            adjacentToHealthy = true;
                        }
                    }
                }
            }

            if (!adjacentToHealthy) {
                if (grid[row][col].getEntityCount() == 0) {
                    grid[row][col].addEntity(new Entity(col, row, cellState.INFECTED));
                } else {
                    grid[row][col].getEntities().get(0).infect();
                }
                return;
            }
        }

        // Last-resort guarantee: infect a random cell even if it is adjacent to healthy entities.
        int fallbackRow = random.nextInt(gridRows);
        int fallbackCol = random.nextInt(gridCols);
        if (grid[fallbackRow][fallbackCol].getEntityCount() == 0) {
            grid[fallbackRow][fallbackCol].addEntity(new Entity(fallbackCol, fallbackRow, cellState.INFECTED));
        } else {
            grid[fallbackRow][fallbackCol].getEntities().get(0).infect();
        }
    }

    public void spreadInfectionToEmptyCells(int infectedCellsToAdd) {
        // Spread infection to empty cells when threshold is met
        for (int i = 0; i < infectedCellsToAdd; i++) {
            List<int[]> emptyCells = new ArrayList<>();
            
            // Find all empty cells
            for (int row = 0; row < gridRows; row++) {
                for (int col = 0; col < gridCols; col++) {
                    if (grid[row][col].getEntityCount() == 0) {
                        emptyCells.add(new int[]{row, col});
                    }
                }
            }
            
            if (emptyCells.isEmpty()) break;
            
            // Add infected entity to random empty cell
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            Entity entity = new Entity(cell[1], cell[0], cellState.INFECTED);
            grid[cell[0]][cell[1]].addEntity(entity);
        }
    }

    public void tick() {
        ticks++;
        List<Entity> allEntities = new ArrayList<>();

        // Collect all entities
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                allEntities.addAll(grid[row][col].getEntities());
            }
        }

        // Move entities randomly to adjacent empty cells
        for (Entity entity : allEntities) {
            if (entity.getState() == cellState.DEAD) {
               continue;
}
            int currentRow = entity.getGridY();
            int currentCol = entity.getGridX();

            // Random movement to adjacent cell with 50% chance
            if (random.nextDouble() < 0.5) {
                // Find adjacent empty cells
                List<int[]> emptyNeighbors = new ArrayList<>();
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue; // Skip current cell
                        
                        int newRow = currentRow + dr;
                        int newCol = currentCol + dc;
                        
                        // Boundary checking
                        if (newRow >= 0 && newRow < gridRows && newCol >= 0 && newCol < gridCols) {
                            if (grid[newRow][newCol].getEntityCount() == 0) {
                                emptyNeighbors.add(new int[]{newRow, newCol});
                            }
                        }
                    }
                }

                // Move to a random empty neighbor if available
                if (!emptyNeighbors.isEmpty()) {
                    int[] target = emptyNeighbors.get(random.nextInt(emptyNeighbors.size()));
                    grid[currentRow][currentCol].removeEntity(entity);
                    entity.setGridX(target[1]);
                    entity.setGridY(target[0]);
                    grid[target[0]][target[1]].addEntity(entity);
                }
            }
        }

        developEnhancedHealthy();
        developDefenders();
        cureNearbyInfections();

        // Handle infection spread to adjacent cells
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                GridCell <Entity>cell = grid[row][col];
                if (cell.getInfectedCount() > 0) {
                    // Infect healthy entities in adjacent cells
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            
                            int adjRow = row + dr;
                            int adjCol = col + dc;
                            
                            if (adjRow >= 0 && adjRow < gridRows && adjCol >= 0 && adjCol < gridCols) {
                                GridCell<Entity> adjCell = grid[adjRow][adjCol];
                                if (adjCell.getHealthyCount() > 0) {
                                    if (random.nextDouble() < infectionChance) {
                                        adjCell.getEntities().get(0).infect();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void developEnhancedHealthy() {
        if (ticks % 3 != 0) {
            return;
        }

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                for (Entity entity : grid[row][col].getEntities()) {
                    if (entity.getState() == cellState.HEALTHY && random.nextDouble() < 0.12) {
                        entity.enhanceHealthy();
                    }
                }
            }
        }
    }

    private void developDefenders() {
        if (ticks < 6 || ticks % 3 != 0 || getTotalInfectedCount() == 0 || random.nextDouble() >= 0.25) {
            return;
        }

        List<GridCell<Entity>> healthyCells = new ArrayList<>();
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                if (grid[row][col].getHealthyCount() > 0) {
                    healthyCells.add(grid[row][col]);
                }
            }
        }

        if (!healthyCells.isEmpty()) {
            GridCell<Entity> cell = healthyCells.get(random.nextInt(healthyCells.size()));
            Entity defender = cell.getEntities().get(0);
            defender.setState(cellState.DEFENDER);
        }
    }

    private void cureNearbyInfections() {
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                if (grid[row][col].getDefenderCount() == 0) {
                    continue;
                }

                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int adjRow = row + dr;
                        int adjCol = col + dc;
                        if (adjRow >= 0 && adjRow < gridRows && adjCol >= 0 && adjCol < gridCols) {
                            for (Entity entity : grid[adjRow][adjCol].getEntities()) {
                                if (entity.getState() == cellState.INFECTED && random.nextDouble() < 0.45) {
                                    entity.cure();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void setInfectionChance(double infectionChance) {
        this.infectionChance = infectionChance;
    }

    public GridCell<Entity> getGridCell(int row, int col) {
        if (row >= 0 && row < gridRows && col >= 0 && col < gridCols) {
            return grid[row][col];
        }
        return null;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int getGridCols() {
        return gridCols;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
    public boolean exposeCell(int row, int col) {

    if (row < 0 || row >= gridRows || col < 0 || col >= gridCols) {
        return false;
    }

    GridCell<Entity> gridCell = grid[row][col];

    for (Entity entity : gridCell.getEntities()) {
        if (entity.getState() == cellState.HEALTHY) {
            entity.infect();
            return true;
        }
    }

    return false;
}

    public int getTotalEntityCount() {
        int count = 0;
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                count += grid[row][col].getEntityCount();
            }
        }
        return count;
    }

    public int getTotalHealthyCount() {
        int count = 0;
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                count += grid[row][col].getHealthyCount();
            }
        }
        return count;
    }

    public int getTotalInfectedCount() {
        int count = 0;
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                count += grid[row][col].getInfectedCount();
            }
        }
        return count;
    }

    public int getTotalEnhancedHealthyCount() {
        int count = 0;
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                count += grid[row][col].getEnhancedHealthyCount();
            }
        }
        return count;
    }

    public int getTotalDefenderCount() {
        int count = 0;
        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                count += grid[row][col].getDefenderCount();
            }
        }
        return count;
    }
    public int getTotalDeadCount() {
    int count = 0;

    for (int row = 0; row < gridRows; row++) {
        for (int col = 0; col < gridCols; col++) {
            count += grid[row][col].getDeadCount();
        }
    }

    return count;
}
}


