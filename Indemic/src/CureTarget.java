
public class CureTarget {
    public final section area;
    public final int row;
    public final int col;
    public final Entity entity;
    public final int nearbyInfected;

    public CureTarget(section area, int row, int col,
                      Entity entity, int nearbyInfected) {
        this.area = area;
        this.row = row;
        this.col = col;
        this.entity = entity;
        this.nearbyInfected = nearbyInfected;
    }
}
