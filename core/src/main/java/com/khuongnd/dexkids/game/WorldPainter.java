package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.khuongnd.dexkids.world.Biome;
import com.khuongnd.dexkids.world.ProceduralWorldGenerator;
import com.khuongnd.dexkids.world.WorldChunk;
import com.khuongnd.dexkids.world.WorldWindow;

/**
 * Original code-drawn cartoon scenery: no third-party art or assets.
 * Screen-space bus stays anchored while world chunks flow underneath.
 */
final class WorldPainter implements Disposable {
    private static final float CHUNK = 640f;
    private static final Color SKY = new Color(0.64f, 0.86f, 0.98f, 1f);
    private static final Color GRASS = new Color(0.62f, 0.83f, 0.53f, 1f);
    private static final Color BUILDING_A = new Color(0.98f, 0.72f, 0.69f, 1f);
    private static final Color BUILDING_B = new Color(0.96f, 0.85f, 0.65f, 1f);
    private static final Color TREE_A = new Color(0.27f, 0.72f, 0.42f, 1f);
    private static final Color HUD_BG = new Color(0.09f, 0.20f, 0.30f, 0.82f);
    private static final Color HUD_ACCENT = new Color(0.97f, 0.74f, 0.26f, 1f);
    private static final Color TREE_B = new Color(0.42f, 0.76f, 0.52f, 1f);
    private final WorldWindow window;

    WorldPainter(ProceduralWorldGenerator generator) {
        this.window = new WorldWindow(generator, 1, 4);
    }

    /** Far background: sky is drawn first, with the sprite-based hills/clouds on top. */
    void paintSky(ShapeRenderer g, float time, WorldMoodResolver.Mood mood) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        g.begin(ShapeRenderer.ShapeType.Filled);
        sky(g, time, mood);
        g.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /** Chunk boundary positions are shared exactly with CartoonSprites/SceneryLayout. */
    void paintGround(ShapeRenderer g, double distanceMeters, float time) {
        double pixels = distanceMeters * 7.5;
        long first = SceneryLayout.firstChunk(distanceMeters);
        window.prepare(first);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        g.begin(ShapeRenderer.ShapeType.Filled);
        for (long idx = first - 1; idx <= first + 4; idx++)
            chunk(g, window.get(idx), SceneryLayout.left(idx, distanceMeters), time);
        road(g, pixels);
        g.setColor(HUD_BG);
        g.rect(22f, 817f, 795f, 229f);
        g.setColor(HUD_ACCENT);
        g.rect(22f, 817f, 10f, 229f);
        g.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void sky(ShapeRenderer g, float time, WorldMoodResolver.Mood mood) {
        g.setColor(mood == WorldMoodResolver.Mood.NIGHT ? Color.NAVY : mood == WorldMoodResolver.Mood.DUSK ? Color.CORAL : SKY);
        g.rect(0, 230, 1920, 850);
        g.setColor(mood == WorldMoodResolver.Mood.NIGHT ? Color.WHITE : Color.GOLD);
        g.circle(1640, 860, 88, 40);
        // Transparent vector clouds are drawn separately by CartoonSprites.
    }

    private void chunk(ShapeRenderer g, WorldChunk c, float x, float time) {
        g.setColor(GRASS);
        g.rect(x, 195, CHUNK + 1, 235);

        if (c.biome() == Biome.RIVER || c.biome() == Biome.BRIDGE) {
            g.setColor(0.35f, 0.72f, 0.91f, 1f);
            g.rect(x, 258, CHUNK, 160);
            g.setColor(0.90f, 0.98f, 1f, 1f);
            for (int i = 0; i < 3; i++) g.rect(x + 60 + i * 185, 325, 90, 6);
            if (c.biome() == Biome.BRIDGE) {
                g.setColor(0.72f, 0.68f, 0.64f, 1f);
                g.rect(x + 25, 420, 585, 20);
                g.rect(x + 110, 410, 25, 90);
                g.rect(x + 510, 410, 25, 90);
            }
        } else if (c.biome() == Biome.URBAN || c.biome() == Biome.RESIDENTIAL) {
            for (int i = 0; i < 4; i++) {
                float bx = x + 20 + i * 156;
                float h = 185 + (Math.abs(c.detailSeed() >> (i * 5)) % 170);
                if (c.biome() == Biome.RESIDENTIAL) h *= 0.7f;
                g.setColor(i % 2 == 0 ? BUILDING_A : BUILDING_B);
                g.rect(bx, 384, 126, h);
                g.setColor(0.34f, 0.60f, 0.78f, 1f);
                for (int k = 0; k < 3; k++) g.rect(bx + 18 + 35 * k, 405 + h / 2, 23, 38);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                float tx = x + 80 + i * 120;
                float ty = 380 + (i % 2) * 35;
                g.setColor(0.55f, 0.35f, 0.25f, 1f);
                g.rect(tx - 10, ty, 20, 112);
                g.setColor(i % 2 == 0 ? TREE_A : TREE_B);
                g.circle(tx, ty + 126, 65, 18);
            }
        }
    }

    private void road(ShapeRenderer g, double distancePx) {
        g.setColor(0.43f, 0.49f, 0.57f, 1f);
        g.rect(0, 0, 1920, 268);
        g.setColor(0.99f, 0.97f, 0.80f, 1f);
        int shift = (int) (distancePx % 230);
        for (int x = -230; x <= 1920; x += 230) g.rect(x - shift, 115, 105, 11);
        g.setColor(0.83f, 0.88f, 0.77f, 1f);
        g.rect(0, 263, 1920, 15);
    }

    @Override public void dispose() { /* no textures owned */ }
}
