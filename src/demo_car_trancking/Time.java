package demo_car_trancking;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Time {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public String getTime() { return LocalTime.now().format(FORMAT); }
}
