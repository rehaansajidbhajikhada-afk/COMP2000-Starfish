import java.util.ArrayList;
import java.util.List;

public class GridCell {
    private List<Entity> entities;
    private int gridX;
    private int gridY;

    public GridCell(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.entities = new ArrayList<>();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public int getEntityCount() {
        return entities.size();
    }

    public int getHealthyCount() {
        int count = 0;
        for (Entity entity : entities) {
            if (entity.getState() == cellState.HEALTHY) {
                count++;
            }
        }
        return count;
    }

    public int getInfectedCount() {
        int count = 0;
        for (Entity entity : entities) {
            if (entity.getState() == cellState.INFECTED) {
                count++;
            }
        }
        return count;
    }

    public int getEnhancedHealthyCount() {
        int count = 0;
        for (Entity entity : entities) {
            if (entity.getState() == cellState.ENHANCED_HEALTHY) {
                count++;
            }
        }
        return count;
    }

    public int getDefenderCount() {
        int count = 0;
        for (Entity entity : entities) {
            if (entity.getState() == cellState.DEFENDER) {
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
