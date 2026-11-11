
public class TransmissionUpgrade extends Upgrade {

    public TransmissionUpgrade() {
        super("Transmission");
    }

    @Override
    public boolean apply(world world) {
        return world.upgradeTransmission();
    }
}
