
public class Cell {

    private double x;
    private double y;
    private double velocityX, velocityY;
    protected static final double CONTACT_RADIUS = 8.0;
    private cellState state;

    public Cell(double x, double y, cellState state) {
        this.x = x;
        this.y = y;
        this.state = state;
    }

    public void move(double boundsWidth, double boundsHeight) {
        x += velocityX;
        y += velocityY;

        if (x <= 0 || x >= boundsWidth) {
            velocityX *= -1;
        }
        if (y <= 0 || y >= boundsHeight) {
            velocityY *= -1;
        }
    }

    public boolean isTouching(Cell other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double distanceSquared = dx * dx + dy * dy;
        return distanceSquared <= CONTACT_RADIUS * CONTACT_RADIUS;
    }

    public void infect() {
        if (state == cellState.HEALTHY) {
            state = cellState.INFECTED;
        }
    }

    public void cure() {
        if (state == cellState.INFECTED) {
            state = cellState.HEALTHY;
        }
    }

    public cellState getState() {
        return state;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }
}
