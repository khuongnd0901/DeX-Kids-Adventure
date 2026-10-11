package com.khuongnd.dexkids.story;

import java.time.LocalTime;

/** Coarse local time only: no date, timezone, coordinates or device identifiers. */
public enum DayPeriod {
    MORNING("buổi sáng"), NOON("buổi trưa"), AFTERNOON("buổi chiều"), EVENING("buổi tối");

    private final String label;
    DayPeriod(String label) { this.label = label; }
    public String label() { return label; }
    public static DayPeriod current() { return atHour(LocalTime.now().getHour()); }
    public static DayPeriod atHour(int hour) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("Invalid local hour");
        if (hour >= 5 && hour < 11) return MORNING;
        if (hour >= 11 && hour < 14) return NOON;
        if (hour >= 14 && hour < 18) return AFTERNOON;
        return EVENING;
    }
}
