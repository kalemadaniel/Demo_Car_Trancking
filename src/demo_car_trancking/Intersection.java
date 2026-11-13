package demo_car_trancking;

/** Fixed cycle: green 10 s, yellow 5 s, red 5 s; paused with the simulation. */
public final class Intersection {
    private int position;
    private String name;
    private int redSeconds = 5;
    private double elapsed;
    private double delaySeconds;
    private int queueLength;
    private int maxQueueLength;
    public Intersection(int position) { this.position = position; this.name = "Carrefour " + position; }
    public String getName() { return name; }
    public int getRedSeconds() { return redSeconds; }
    void configure(String name, int position, int seconds) {
        if (name == null || name.trim().isEmpty() || position <= 0 || seconds < 1 || seconds > 3600)
            throw new IllegalArgumentException("Nom requis, distance positive et rouge entre 1 et 3600 secondes.");
        this.name = name.trim(); this.position = position; redSeconds = seconds;
        reset();
    }
    public int getPosition() { return position; }
    public String getColor() { return elapsed < 10 ? "Vert" : elapsed < 15 ? "Orange" : "Rouge"; }
    public boolean isRed() { return elapsed >= 15; }
    public double getDelaySeconds() { return delaySeconds; }
    public int getQueueLength() { return queueLength; }
    public int getMaxQueueLength() { return maxQueueLength; }
    void addDelay(double seconds) { delaySeconds += seconds; }
    void setQueueLength(int count) { queueLength = count; maxQueueLength = Math.max(maxQueueLength, count); }
    void advance(double seconds) { elapsed = (elapsed + seconds) % (15 + redSeconds); }
    void reset() { elapsed = 0; delaySeconds = 0; queueLength = 0; maxQueueLength = 0; }
}
