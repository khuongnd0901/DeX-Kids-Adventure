
package com.khuongnd.dexkids.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.khuongnd.dexkids.game.KidsGame;

public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DeX Kids Adventure - GPS Demo");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(30);
        config.useVsync(true);
        new Lwjgl3Application(new KidsGame(), config);
    }
}
