
package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.khuongnd.dexkids.journey.JourneyFeed;
import com.khuongnd.dexkids.world.ProceduralWorldGenerator;

public final class AdventureScreen extends ScreenAdapter {
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;
    private final JourneyFeed journey;
    private final Viewport viewport;
    private final ShapeRenderer shapes;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final WorldPainter painter;
    private float clock;

    public AdventureScreen(JourneyFeed journey) {
        this.journey = journey;
        viewport = new FitViewport(WIDTH, HEIGHT, new OrthographicCamera());
        viewport.getCamera().position.set(WIDTH / 2f, HEIGHT / 2f, 0);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.5f);
        painter = new WorldPainter(new ProceduralWorldGenerator(20261008L));
    }

    @Override
    public void render(float dt) {
        float delta = Math.min(0.1f, Math.max(0, dt));
        clock += delta;
        journey.update(delta);
        Gdx.gl.glClearColor(0.73f, 0.87f, 0.99f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapes.setProjectionMatrix(viewport.getCamera().combined);
        painter.paint(shapes, journey.distanceMeters(), clock);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        font.draw(batch, "DeX KIDS ADVENTURE", 50, 1015);
        font.draw(batch, "DEMO WORLD - NO REAL GPS / POI", 50, 968);
        font.draw(batch, String.format(java.util.Locale.US,
                "Simulated speed: %.0f km/h   Distance: %.1f km",
                journey.speedMetersPerSecond() * 3.6, journey.distanceMeters() / 1000.0),
                50, 914);
        batch.end();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void dispose() {
        painter.dispose();
        font.dispose();
        batch.dispose();
        shapes.dispose();
    }
}
