package com.khuongnd.dexkids.game;

import java.time.LocalTime;

/** Local time is used only for visuals; never sent to a server. */
public final class WorldMoodResolver {
    public enum Mood { DAY, DUSK, NIGHT }
    public Mood resolve(LocalTime local) {
        if (local == null) throw new IllegalArgumentException("time");
        int hour = local.getHour();
        if (hour < 6 || hour >= 19) return Mood.NIGHT;
        if (hour < 8 || hour >= 17) return Mood.DUSK;
        return Mood.DAY;
    }
}
