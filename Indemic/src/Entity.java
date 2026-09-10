public class Entity {
    private cellState state;
    private int recoveryCount;
    private int gridX; // Grid position
    private int gridY; // Grid position

    public Entity(int gridX, int gridY, cellState state) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.state = state;
        this.recoveryCount = 0;
    }

    public void infect() {
        if (state == cellState.HEALTHY) {
            state = cellState.INFECTED;
        }
    }

    public void enhanceHealthy() {
        if (state == cellState.HEALTHY) {
            state = cellState.ENHANCED_HEALTHY;
        }
    }

    public void setState(cellState state) {
        this.state = state;
    }

  public void cure() {
    if (state == cellState.INFECTED) {

        if (recoveryCount >= 2) {
            state = cellState.DEAD;
        } else {
            state = cellState.HEALTHY;
            recoveryCount++;
        }
    }
}

    public cellState getState() {
        return state;
    }
    public int getRecoveryCount() {
    return recoveryCount;
}

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }
}
