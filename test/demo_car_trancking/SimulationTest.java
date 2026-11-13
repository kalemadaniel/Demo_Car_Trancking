package demo_car_trancking;

import java.util.Arrays;
import javax.swing.SwingUtilities;

public final class SimulationTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void near(double actual, double expected, String message) {
        check(Math.abs(actual - expected) < 0.001, message + ": " + actual);
    }
    public static void main(String[] args) throws Exception {
        Simulation s = new Simulation();
        s.pause(); s.advance(1);
        check(s.getState() == Simulation.State.STOPPED, "Pause before start");
        near(s.getCars().get(0).getPosition(), 0, "Stopped vehicle");
        s.setSpeedKmh(36); s.start(); s.advance(1);
        near(s.getCars().get(0).getPosition(), 10, "km/h conversion");
        s.pause(); s.advance(10);
        near(s.getElapsed(), 1, "Paused time");
        near(s.getCars().get(0).getPosition(), 10, "Paused position");
        near(s.getCars().get(0).getSpeed(), 0, "Paused speed");
        s.start(); s.advance(1);
        near(s.getCars().get(0).getPosition(), 20, "Resume");
        s.pause(); s.stop();
        near(s.getElapsed(), 0, "Stop while paused");
        near(s.getCars().get(0).getPosition(), 0, "Reset position");
        for (int i = 0; i < 20; i++) { s.start(); s.advance(0.1); s.stop(); }
        s.start(); s.advance(1);
        near(s.getCars().get(0).getPosition(), 10, "Repeated restart");
        Intersection a = new Intersection(1000), b = new Intersection(2000), c = new Intersection(3000);
        a.advance(15);
        Car car = new Car("Test", 999);
        car.advance(0.1, 180, Arrays.asList(a, b, c));
        near(car.getPosition(), 1000, "Clamp to red stop line");
        car.advance(0.1, 180, Arrays.asList(a, b, c));
        near(car.getPosition(), 1000, "Other green light must not release car");
        near(car.getSpeed(), 0, "Red speed");
        a.advance(5); car.advance(0.1, 180, Arrays.asList(a, b, c));
        near(car.getPosition(), 1005, "Release on own green");
        c.advance(15); car = new Car("End", 2999);
        car.advance(0.1, 180, Arrays.asList(a, b, c));
        near(car.getPosition(), 3000, "Final light before wrapping");
        c.advance(5); car.advance(0.1, 180, Arrays.asList(a, b, c));
        near(car.getPosition(), 5, "Wrap preserves excess distance");
        b.advance(10); check(b.getColor().equals("Orange"), "Yellow phase");
        car = new Car("Yellow", 1999); car.advance(0.1, 180, Arrays.asList(a, b, c));
        near(car.getPosition(), 2004, "Proceed on yellow");
        s.addCar(); s.addIntersection();
        check(s.getCars().size() == 5 && s.getLights().size() == 4, "Dynamic additions");
        check(s.getLights().get(3).getPosition() == 4000, "Intersection spacing");
        boolean invalid = false;
        try { s.setSpeedKmh(Double.NaN); } catch (IllegalArgumentException e) { invalid = true; }
        check(invalid, "Reject invalid speed");
        // Smoke-test construction and disposal on the actual Swing thread.
        SwingUtilities.invokeAndWait(() -> {
            CMSC335Project3 window = new CMSC335Project3();
            window.dispose();
        });
        System.out.println("PASS: " + checks + " checks and Swing construction/disposal");
    }
}
