public class InfectivityUpgrade extends Upgrade {

    public InfectivityUpgrade() {
        super("Infectivity");
    }

    @Override
    public boolean apply(world world) {
        return world.upgradeInfectivity();
    }
}