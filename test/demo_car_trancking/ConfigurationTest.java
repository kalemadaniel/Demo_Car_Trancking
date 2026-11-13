package demo_car_trancking;

public final class ConfigurationTest {
    private static void check(boolean ok, String reason) { if (!ok) throw new AssertionError(reason); }
    public static void main(String[] args) {
        Simulation s = new Simulation(false);
        s.configureIntersection(0, "Marché", 800, 12);
        s.configureIntersection(2, "Gare", 2500, 8);
        check(s.getRoadLength() == 2500, "Custom road length");
        Intersection light = s.getLights().get(0);
        light.advance(15); check(light.isRed(), "Red starts after green/yellow");
        light.advance(11.9); check(light.isRed(), "Configured red persists");
        light.advance(0.2); check(!light.isRed(), "Configured red ends");
        s.stop();
        s.addCarAt(800, 0); s.addCarAt(793, 0); s.addCarAt(800, 1); s.addCarAt(793, 1);
        s.addCarAt(100, 2);
        light.advance(15); s.start(); s.advance(1);
        check(light.getQueueLength() == 4, "Queues aggregated across lanes");
        check(Math.abs(s.getTotalDelay() - 4) < 0.001, "Independent lane delays");
        check(s.getCars().get(4).getPosition() > 100, "Other lane moves freely");
        boolean rejected = false;
        try { s.configureIntersection(0, "X", 900, 5); } catch (IllegalStateException ex) { rejected = true; }
        check(rejected, "No geometry edits while running");
        s.stop(); check(light.getName().equals("Marché") && light.getRedSeconds() == 12, "Reset preserves configuration");
        rejected = false;
        try { s.configureIntersection(0, "X", 2200, 5); } catch (IllegalArgumentException ex) { rejected = true; }
        check(rejected && light.getPosition() == 800, "Invalid geometry rejected atomically");
        s.addLane(); s.addCar(4);
        check(s.getCars().get(s.getCars().size() - 1).getLane() == 4, "Add to selected lane");
        Simulation wrap = new Simulation(false);
        wrap.configureIntersection(2, "Fin", 2200, 9);
        wrap.addCarAt(2199, 0); wrap.setSpeedKmh(36); wrap.start(); wrap.advance(1);
        check(Math.abs(wrap.getCars().get(0).getPosition() - 9) < 0.001, "Wrap at custom length");
        System.out.println("PASS: lane independence, queue totals, red timing, names, geometry and custom loop");
    }
}
