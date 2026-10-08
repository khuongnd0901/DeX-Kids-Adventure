package com.khuongnd.dexkids.desktop;

import java.nio.file.Files;
import java.nio.file.Path;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.khuongnd.dexkids.game.KidsGame;
import com.khuongnd.dexkids.journey.GpxReplayFeed;

public final class DesktopLauncher {
    private DesktopLauncher() {}
    public static void main(String[] args) throws Exception {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DeX Kids Adventure - SIMULATION");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(30);
        config.useVsync(true);
        var game = new KidsGame();
        if (args.length == 2 && "--gpx".equals(args[0])) {
            try (var input = Files.newInputStream(Path.of(args[1]))) {
                game = new KidsGame(GpxReplayFeed.fromGpx(input));
            }
        }
        new Lwjgl3Application(game, config);
    }
}
