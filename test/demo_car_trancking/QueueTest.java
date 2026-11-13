package demo_car_trancking;

public final class QueueTest {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    private static void near(double actual, double expected, String message) {
        check(Math.abs(actual - expected) < 0.001, message + ": " + actual);
    }
    private static void spacing(Simulation s) {
        double length = s.getLights().size() * 1000.0;
        for (int i = 0; i < s.getCars().size(); i++) for (int j = i + 1; j < s.getCars().size(); j++) {
            double gap = Math.abs(s.getCars().get(i).getPosition() - s.getCars().get(j).getPosition());
            check(Math.min(gap, length - gap) >= Simulation.MIN_SPACING - 1e-7, "No vehicle overlap");
        }
    }
    public static void main(String[] args) {
        Simulation s = new Simulation(false);
        s.addCarAt(1000); s.addCarAt(993); s.addCarAt(986);
        s.getLights().get(0).advance(15);
        s.setSpeedKmh(36); s.start(); s.advance(2);
        near(s.getCars().get(0).getPosition(), 1000, "Leader at red");
        near(s.getCars().get(1).getPosition(), 993, "Follower behind leader");
        near(s.getTotalDelay(), 6, "Three cars delayed two seconds");
        near(s.getLights().get(0).getDelaySeconds(), 6, "Delay attributed to correct light");
        check(s.getLights().get(0).getQueueLength() == 3, "Three queued cars");
        near(s.getCars().get(2).getStoppedSeconds(), 2, "Stopped duration");
        s.pause(); s.advance(50);
        near(s.getTotalDelay(), 6, "Pause excluded");
        check(s.getLights().get(0).getQueueLength() == 3, "Paused queue preserved");
        s.start(); s.advance(3.1);
        check(s.getCars().get(2).getPosition() > 986, "Queue restarts at green");
        check(s.getLights().get(0).getQueueLength() == 0, "Queue clears");
        check(s.getLights().get(0).getMaxQueueLength() == 3, "Peak retained");
        spacing(s);
        s.stop(); near(s.getTotalDelay(), 0, "Reset delay");
        check(s.getLights().get(0).getMaxQueueLength() == 0, "Reset peak");
        spacing(s);

        Simulation wrap = new Simulation(false);
        wrap.addCarAt(0); wrap.addCarAt(2993); wrap.addCarAt(2986);
        wrap.getLights().get(2).advance(15);
        wrap.setSpeedKmh(36); wrap.start();
        for (int i = 0; i < 400; i++) { wrap.advance(0.02); spacing(wrap); }
        check(wrap.getLights().get(2).getMaxQueueLength() == 2, "Last intersection queue");
        check(wrap.getCars().get(1).getPosition() < 100, "Loop resumes after green");

        Simulation partial = new Simulation(false);
        partial.addCarAt(999.9); partial.getLights().get(0).advance(15);
        partial.setSpeedKmh(36); partial.start(); partial.advance(0.02);
        near(partial.getTotalDelay(), 0.01, "Partial-step delay");
        near(partial.getLights().get(0).getDelaySeconds(), 0.01, "Partial-step attribution");

        Simulation dense = new Simulation(false);
        for (int i = 0; i < 40; i++) dense.addCarAt(720 + i * 7);
        dense.getLights().get(0).advance(15); dense.start();
        for (int i = 0; i < 500; i++) { dense.advance(0.1); spacing(dense); }
        double sum = 0;
        for (Intersection light : dense.getLights()) sum += light.getDelaySeconds();
        near(sum, dense.getTotalDelay(), "All delay attributed exactly once");
        check(dense.getLights().get(0).getMaxQueueLength() >= 30, "Long queue propagates");
        for (int i = 0; i < 20; i++) dense.addCar();
        spacing(dense); dense.stop(); spacing(dense);
        System.out.println("PASS: " + checks + " queue, spacing and delay checks");
    }
}
