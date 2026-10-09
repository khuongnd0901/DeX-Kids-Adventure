package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.ScreenAdapter;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KidsGameLifecycleTest {
    @Test void closingGameDisposesItsOwnedScreenExactlyOnce() {
        Graphics original = Gdx.graphics;
        Gdx.graphics = (Graphics) Proxy.newProxyInstance(Graphics.class.getClassLoader(),
                new Class<?>[]{Graphics.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getWidth" -> 1920;
                    case "getHeight" -> 1080;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        try {
            var game = new KidsGame();
            var screen = new CountingScreen();
            game.setScreen(screen);
            game.dispose();
            assertEquals(1, screen.disposals, "Game must release atlas/batch resources through its screen");
            assertTrue(screen.hiddenBeforeDispose);
            game.dispose();
            assertEquals(1, screen.disposals, "Shutdown must not double-dispose GL resources");
        } finally { Gdx.graphics = original; }
    }
    private static final class CountingScreen extends ScreenAdapter {
        int disposals;
        boolean hidden;
        boolean hiddenBeforeDispose;
        @Override public void hide() { hidden = true; }
        @Override public void dispose() { hiddenBeforeDispose = hidden; disposals++; }
    }
}
