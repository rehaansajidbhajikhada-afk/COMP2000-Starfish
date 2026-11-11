public class PowerMeterTest {

    public static void main(String[] args) {

        PowerMeter meter = new PowerMeter();

        meter.addObserver(power ->
            System.out.println("HUD power: " + power + "%")
        );

        meter.addObserver(power -> {
            if (power == 100) {
                System.out.println("BOOSTER READY!");
            }
        });

        meter.charge(40);
        meter.charge(60);

        System.out.println("Ready: " + meter.isReady());

        meter.consume();
    }
}