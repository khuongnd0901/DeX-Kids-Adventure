package com.khuongnd.dexkids.desktop;

import java.nio.file.Files;
import java.nio.file.Path;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.khuongnd.dexkids.game.KidsGame;
import com.khuongnd.dexkids.journey.DemoJourneyFeed;
import com.khuongnd.dexkids.journey.GpxReplayFeed;
import com.khuongnd.dexkids.journey.JourneyFeed;

/** Desktop options: --gpx PATH --width N --height N --smoke-frames N --screenshot PATH. */
public final class DesktopLauncher {
    private DesktopLauncher() {}
    public static void main(String[] args) throws Exception {
        int width = 1280, height = 720, frames = 0;
        String gpxPath = null, screenshot = null;
        boolean samplePreview = false;
        double startSeconds = 0;
        if (args.length % 2 != 0) throw new IllegalArgumentException("Expected flag/value pairs");
        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "--gpx" -> gpxPath = args[i + 1];
                case "--width" -> width = Integer.parseInt(args[i + 1]);
                case "--height" -> height = Integer.parseInt(args[i + 1]);
                case "--smoke-frames" -> frames = Integer.parseInt(args[i + 1]);
                case "--screenshot" -> screenshot = args[i + 1];
                case "--sample-preview" -> samplePreview = Boolean.parseBoolean(args[i + 1]);
                case "--start-seconds" -> startSeconds = Double.parseDouble(args[i + 1]);
                default -> throw new IllegalArgumentException("Unsupported argument: " + args[i]);
            }
        }
        if (!Double.isFinite(startSeconds) || startSeconds < 0 || startSeconds > 3600)
            throw new IllegalArgumentException("Invalid seek time");
        if (samplePreview && gpxPath == null)
            throw new IllegalArgumentException("Sample POIs need explicit GPX replay");
        if (width < 320 || width > 4096 || height < 240 || height > 2160)
            throw new IllegalArgumentException("Unsupported desktop screenshot size");
        JourneyFeed feed = new DemoJourneyFeed();
        if (gpxPath != null) {
            try (var input = Files.newInputStream(Path.of(gpxPath))) {
                feed = GpxReplayFeed.fromGpx(input);
            }
        }
        if (startSeconds > 0) {
            if (!(feed instanceof GpxReplayFeed replay)) throw new IllegalArgumentException("Seeking needs GPX");
            int steps = (int) Math.round(startSeconds * 10);
            for (int i = 0; i < steps && !replay.finished(); i++) replay.update(0.1f);
        }
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DeX Kids Adventure - SIMULATION / NOT REAL POI");
        config.setWindowedMode(width, height);
        config.setForegroundFPS(30);
        config.useVsync(true);
        new Lwjgl3Application(new KidsGame(feed, frames, screenshot, samplePreview, 4), config);
    }
}
