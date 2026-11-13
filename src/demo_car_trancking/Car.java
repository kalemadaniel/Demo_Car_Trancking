package demo_car_trancking;

/** Vehicle positions are metres and speeds are kilometres per hour. */
public final class Car {
    private final String name;
    private final int lane;
    private final double initialPosition;
    private double position;
    private double speed;
    private double delaySeconds;
    private double stoppedSeconds;
    private int waitingAt = -1;

    public Car(String name, double position) { this(name, position, 0); }
    public Car(String name, double position, int lane) {
        this.lane = lane;
        if (!Double.isFinite(position) || position < 0) throw new IllegalArgumentException("Position invalide");
        this.name = name;
        this.initialPosition = position;
        this.position = position;
    }
    public int getLane() { return lane; }
    public String getName() { return name; }
    public double getPosition() { return position; }
    public double getSpeed() { return speed; }
    public double getDelaySeconds() { return delaySeconds; }
    public double getStoppedSeconds() { return stoppedSeconds; }
    public int getWaitingAt() { return waitingAt; }
    double getInitialPosition() { return initialPosition; }
    void reset() { position = initialPosition; speed = 0; delaySeconds = 0; stoppedSeconds = 0; waitingAt = -1; }
    void halt() { speed = 0; }

    void move(double distance, double seconds, double targetSpeed, double length, int cause) {
        double next = position + distance;
        position = distance > 0 ? (cause >= 0 && Math.abs(next - length) < 1e-9 ? length : next % length) : position;
        speed = distance / seconds * 3.6;
        double lost = Math.max(0, seconds - distance / (targetSpeed / 3.6));
        delaySeconds += lost;
        if (distance < 1e-9) stoppedSeconds += seconds;
        waitingAt = lost > 1e-9 ? cause : -1;
    }

    void advance(double seconds, double speedKmh, java.util.List<Intersection> lights) {
        double next = position + speedKmh / 3.6 * seconds;
        speed = speedKmh;
        for (Intersection light : lights) {
            if (light.isRed() && position <= light.getPosition() && next >= light.getPosition()) {
                position = light.getPosition();
                speed = 0;
                return;
            }
        }
        double roadLength = lights.get(lights.size() - 1).getPosition();
        position = next % roadLength;
    }
}
