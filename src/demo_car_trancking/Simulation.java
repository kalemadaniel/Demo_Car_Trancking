package demo_car_trancking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

/** Single-owner model: UI calls it only on Swing's event dispatch thread. */
public final class Simulation {
    public static final double MIN_SPACING = 7; // 4 m vehicle + 3 m clearance.
    public enum State { STOPPED, RUNNING, PAUSED }
    private State state = State.STOPPED;
    private final List<Car> cars = new ArrayList<>();
    private final List<Intersection> lights = new ArrayList<>();
    private double elapsed;
    private int laneCount = 4;
    public int getLaneCount() { return laneCount; }
    public void addLane() { laneCount++; }
    public double getRoadLength() { return lights.get(lights.size() - 1).getPosition(); }
    private double speedKmh = 50;
    public Simulation() { this(true); }
    Simulation(boolean initialCars) {
        for (int i = 0; i < 3; i++) addIntersection();
        if (initialCars) for (int i = 0; i < 4; i++) addCar(i);
    }
    public List<Car> getCars() { return Collections.unmodifiableList(cars); }
    public List<Intersection> getLights() { return Collections.unmodifiableList(lights); }
    public State getState() { return state; }
    public double getElapsed() { return elapsed; }
    public void setSpeedKmh(double speed) {
        if (!Double.isFinite(speed) || speed < 1 || speed > 180) throw new IllegalArgumentException("Vitesse entre 1 et 180 km/h");
        speedKmh = speed;
    }
    public double getTotalDelay() {
        double total = 0;
        for (Car car : cars) total += car.getDelaySeconds();
        return total;
    }
    public void addCar() { addCar(0); }
    public void addCar(int lane) {
        if (lane < 0 || lane >= laneCount) throw new IllegalArgumentException("Ligne inconnue");
        double length = getRoadLength();
        for (int offset = 0; offset < length; offset++) {
            double position = (cars.size() * 200.0 + offset) % length;
            if (canInsert(position, lane)) { addCarAt(position, lane); return; }
        }
        throw new IllegalStateException("Pas de place disponible : ajoutez un carrefour ou réinitialisez la simulation.");
    }
    private boolean canInsert(double position, int lane) {
        double length = getRoadLength();
        for (Car car : cars) {
            if (car.getLane() != lane) continue;
            double gap = Math.abs(position - car.getPosition());
            double initialGap = Math.abs(position - car.getInitialPosition());
            if (Math.min(gap, length - gap) < MIN_SPACING || Math.min(initialGap, length - initialGap) < MIN_SPACING) return false;
        }
        return true;
    }
    void addCarAt(double position) { addCarAt(position, 0); }
    void addCarAt(double position, int lane) {
        if (lane < 0 || lane >= laneCount) throw new IllegalArgumentException("Ligne inconnue");
        if (!Double.isFinite(position) || position < 0 || position >= getRoadLength() || !canInsert(position, lane))
            throw new IllegalArgumentException("Position occupée ou hors de la route");
        cars.add(new Car("Véhicule " + (cars.size() + 1), position, lane));
    }
    public void addIntersection() {
        int position = lights.isEmpty() ? 1000 : (int) getRoadLength() + 1000;
        Intersection light = new Intersection(position);
        light.configure("Carrefour " + (lights.size() + 1), position, 5);
        lights.add(light);
    }
    public void configureIntersection(int index, String name, int metres, int redSeconds) {
        if (state != State.STOPPED) throw new IllegalStateException("Arrêtez la simulation avant de modifier les carrefours.");
        if (index < 0 || index >= lights.size()) throw new IllegalArgumentException("Carrefour inconnu");
        if (metres <= (index == 0 ? 0 : lights.get(index - 1).getPosition()) ||
            (index + 1 < lights.size() && metres >= lights.get(index + 1).getPosition()))
            throw new IllegalArgumentException("Les distances depuis le départ doivent être croissantes.");
        if (index == lights.size() - 1)
            for (Car car : cars) if (car.getInitialPosition() + MIN_SPACING > metres)
                throw new IllegalArgumentException("Route trop courte pour les véhicules déjà présents.");
        lights.get(index).configure(name, metres, redSeconds);
        stop();
    }
    public void start() { state = State.RUNNING; }
    public void pause() {
        if (state == State.RUNNING) { state = State.PAUSED; for (Car car : cars) car.halt(); }
    }
    public void stop() {
        state = State.STOPPED;
        elapsed = 0;
        for (Car car : cars) car.reset();
        for (Intersection light : lights) light.reset();
    }
    public void advance(double seconds) {
        if (!Double.isFinite(seconds) || seconds < 0) throw new IllegalArgumentException("Durée invalide");
        if (state != State.RUNNING) return;
        // Small bounded steps prevent skipping a stop line or an entire light phase.
        while (seconds > 1e-9) {
            double step = Math.min(seconds, 0.02);
            advanceTraffic(step);
            for (Intersection light : lights) light.advance(step);
            elapsed += step;
            seconds -= step;
        }
    }

    private void advanceTraffic(double step) {
        int[] queues = new int[lights.size()];
        for (int lane = 0; lane < laneCount; lane++) advanceLane(step, lane, queues);
        for (int j = 0; j < lights.size(); j++) lights.get(j).setQueueLength(queues[j]);
    }
    private void advanceLane(double step, int lane, int[] queues) {
        List<Car> ordered = new ArrayList<>();
        for (Car car : cars) if (car.getLane() == lane) ordered.add(car);
        ordered.sort(Comparator.comparingDouble(Car::getPosition));
        int count = ordered.size();
        double length = getRoadLength(), free = speedKmh / 3.6 * step;
        double[] moves = new double[count];
        int[] causes = new int[count];
        for (int i = 0; i < count; i++) {
            moves[i] = free; causes[i] = -1;
            double position = ordered.get(i).getPosition();
            for (int j = 0; j < lights.size(); j++) {
                Intersection light = lights.get(j);
                double distance = light.getPosition() - position;
                if (distance < 0) distance += length;
                if (light.isRed() && distance <= moves[i]) { moves[i] = distance; causes[i] = j; }
            }
        }
        // Propagate each leader's constrained movement backwards through the queue.
        // Snapshot positions avoid dependence on vehicle insertion order.
        for (int pass = 0; pass < count && count > 1; pass++) {
            boolean changed = false;
            for (int i = count - 1; i >= 0; i--) {
                int leader = (i + 1) % count;
                double gap = ordered.get(leader).getPosition() - ordered.get(i).getPosition();
                if (leader == 0) gap += length;
                double allowed = Math.max(0, gap - MIN_SPACING + moves[leader]);
                if (allowed < moves[i] - 1e-10) {
                    moves[i] = allowed; causes[i] = causes[leader]; changed = true;
                }
            }
            if (!changed) break;
        }
        for (int i = 0; i < count; i++) {
            Car car = ordered.get(i);
            car.move(moves[i], step, speedKmh, length, causes[i]);
            if (causes[i] >= 0) {
                lights.get(causes[i]).addDelay(Math.max(0, step - moves[i] / (speedKmh / 3.6)));
                if (moves[i] < 1e-9) queues[causes[i]]++;
            }
        }
    }
}
