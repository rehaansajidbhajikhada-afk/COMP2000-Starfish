import java.util.ArrayList;
import java.util.List;

public class GridCell<T extends Entity> {
    private List<T> entities;
    private int gridX;
    private int gridY;

    public GridCell(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.entities = new ArrayList<>();
    }

    public void addEntity(T entity) {
        entities.add(entity);
    }

    public void removeEntity(T entity) {
        entities.remove(entity);
    }

    public List<T> getEntities() {
        return entities;
    }

    public int getEntityCount() {
        return entities.size();
    }

    public int getHealthyCount() {
        int count = 0;
        for (T entity : entities) {
            if (entity.getState() == cellState.HEALTHY) {
                count++;
            }
        }
        return count;
    }

    public int getInfectedCount() {
        int count = 0;
        for (T entity : entities) {
            if (entity.getState() == cellState.INFECTED) {
                count++;
            }
        }
        return count;
    }

    public int getEnhancedHealthyCount() {
        int count = 0;
        for (T entity : entities) {
            if (entity.getState() == cellState.ENHANCED_HEALTHY) {
                count++;
            }
        }
        return count;
    }

    public int getDefenderCount() {
        int count = 0;
        for (T entity : entities) {
            if (entity.getState() == cellState.DEFENDER) {
                count++;
            }
        }
        return count;
    }

    public int getDeadCount() {
        int count = 0;

        for (T entity : entities) {
            if (entity.getState() == cellState.DEAD) {
                count++;
            }
        }

        return count;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }
}
