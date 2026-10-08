package com.khuongnd.dexkids.game;

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
    private static final Color TREE_B = new Color(0.42f, 0.76f, 0.52f, 1f);
    private final WorldWindow window;
    private final CharacterAnimationController character = new CharacterAnimationController();

    WorldPainter(ProceduralWorldGenerator generator) {
        this.window = new WorldWindow(generator, 1, 4);
    }

    void paint(ShapeRenderer g, double distanceMeters, float time, WorldMoodResolver.Mood mood) {
        // Scale motion visually. The coordinate source is independent of frame rate.
        double pixels = distanceMeters * 7.5;
        long first = (long) Math.floor(pixels / CHUNK);
        float offset = (float) (pixels - first * CHUNK);
        window.prepare(first);
        character.setState(CharacterAnimationController.State.ROLLING);
        character.advance(Math.max(0, time - character.elapsedTotal()));

        g.begin(ShapeRenderer.ShapeType.Filled);
        sky(g, time, mood);
        for (long idx = first - 1; idx <= first + 4; idx++) {
            float left = (idx - first) * CHUNK - offset;
            chunk(g, window.get(idx), left, time);
        }
        road(g, pixels);
        bus(g, time, distanceMeters);
        g.end();
    }

    private void sky(ShapeRenderer g, float time, WorldMoodResolver.Mood mood) {
        g.setColor(mood == WorldMoodResolver.Mood.NIGHT ? Color.NAVY : mood == WorldMoodResolver.Mood.DUSK ? Color.CORAL : SKY);
        g.rect(0, 230, 1920, 850);
        g.setColor(mood == WorldMoodResolver.Mood.NIGHT ? Color.WHITE : Color.GOLD);
        g.circle(1640, 860, 88, 40);
        g.setColor(Color.WHITE);
        for (int i = 0; i < 4; i++) {
            float x = ((i * 540 + time * 13) % 2400) - 230;
            float y = 740 + (i % 2) * 100;
            g.ellipse(x, y, 165, 72);
            g.ellipse(x + 70, y + 25, 120, 78);
        }
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

    private void bus(ShapeRenderer g, float t, double distance) {
        float bob = (float) Math.sin(t * 5) * 4;
        float x = 265f;
        float y = 236f + bob;
        g.setColor(0.98f, 0.73f, 0.17f, 1f);
        g.rect(x, y, 480, 205);
        g.circle(x + 30, y + 174, 32, 20);
        g.circle(x + 449, y + 174, 32, 20);
        g.setColor(0.98f, 0.86f, 0.36f, 1f);
        g.rect(x + 24, y + 25, 428, 160);
        g.setColor(0.43f, 0.77f, 0.88f, 1f);
        g.rect(x + 35, y + 95, 150, 90);
        g.rect(x + 195, y + 95, 130, 90);
        g.rect(x + 334, y + 95, 112, 90);
        // Capybara sits visible in the front window.
        g.setColor(0.60f, 0.37f, 0.24f, 1f);
        g.ellipse(x + 344, y + 103, 84, 70);
        g.circle(x + 375, y + 167, 28, 28);
        g.circle(x + 413, y + 157, 15, 20);
        g.setColor(0.18f, 0.15f, 0.13f, 1f);
        g.circle(x + 404, y + 161, 4, 10);
        if (!character.isBlinking()) g.circle(x + 387, y + 172, 3, 8);
        g.circle(x + 427, y + 146, 6, 10);
        if (character.state() == CharacterAnimationController.State.TALKING) g.ellipse(x + 411, y + 132, 15, 9);
        // Explorer hat.
        g.setColor(0.92f, 0.81f, 0.46f, 1f);
        g.rect(x + 343, y + 192, 90, 11);
        g.rect(x + 359, y + 204, 58, 30);
        g.setColor(0.25f, 0.29f, 0.35f, 1f);
        g.circle(x + 108, y + 13, 51, 30);
        g.circle(x + 384, y + 13, 51, 30);
        g.setColor(0.87f, 0.91f, 0.94f, 1f);
        g.circle(x + 108, y + 13, 21, 24);
        g.circle(x + 384, y + 13, 21, 24);
        g.setColor(1f, 1f, 1f, 1f);
        g.rect(x + 20, y + 57, 431, 10);
    }

    @Override public void dispose() { /* no textures owned */ }
}
