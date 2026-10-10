package com.khuongnd.dexkids.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.khuongnd.dexkids.world.Biome;
import com.khuongnd.dexkids.world.ProceduralWorldGenerator;
import com.khuongnd.dexkids.world.WorldChunk;
import com.khuongnd.dexkids.world.WorldWindow;

/**
 * All vector-like scenery is now drawn as textured quads in the SAME SpriteBatch
 * as the cartoon atlas. A single small RGBA atlas holds both a white pixel and
 * a pre-rasterized circle, avoiding per-frame tessellation and GL shape passes.
 * World positions, POI state, bus motion and source claims remain unchanged.
 */
final class WorldPainter implements Disposable {
    private static final float CHUNK = SceneryLayout.CHUNK_WIDTH;
    private static final Color SKY = new Color(0.64f, 0.86f, 0.98f, 1f);
    private static final Color GRASS = new Color(0.62f, 0.83f, 0.53f, 1f);
    private static final Color BUILDING_A = new Color(0.98f, 0.72f, 0.69f, 1f);
    private static final Color BUILDING_B = new Color(0.96f, 0.85f, 0.65f, 1f);
    private static final Color TREE_A = new Color(0.27f, 0.72f, 0.42f, 1f);
    private static final Color TREE_B = new Color(0.42f, 0.76f, 0.52f, 1f);
    private static final Color HUD_BG = new Color(0.09f, 0.20f, 0.30f, 0.82f);
    private static final Color HUD_ACCENT = new Color(0.97f, 0.74f, 0.26f, 1f);

    private final WorldWindow window;
    private final Texture primitiveAtlas;
    private final TextureRegion solid;
    private final TextureRegion round;

    WorldPainter(ProceduralWorldGenerator generator) {
        this.window = new WorldWindow(generator, 0, 3); // Four actually visible 640px tiles
        Pixmap pix = new Pixmap(130, 130, Pixmap.Format.RGBA8888);
        try {
            pix.setColor(0, 0, 0, 0);
            pix.fill();
            pix.setColor(Color.WHITE);
            pix.fillCircle(64, 64, 63);
            pix.drawPixel(129, 129); // same texture: solid quads cause no GPU texture switch
            primitiveAtlas = new Texture(pix);
        } finally {
            pix.dispose();
        }
        primitiveAtlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        solid = new TextureRegion(primitiveAtlas, 129, 129, 1, 1);
        round = new TextureRegion(primitiveAtlas, 0, 0, 128, 128);
    }

    void paintSky(SpriteBatch b, WorldMoodResolver.Mood mood) {
        rect(b, mood == WorldMoodResolver.Mood.NIGHT ? Color.NAVY :
            mood == WorldMoodResolver.Mood.DUSK ? Color.CORAL : SKY,
            0, 230, 1920, 850);
        b.setColor(mood == WorldMoodResolver.Mood.NIGHT ? Color.WHITE : Color.GOLD);
        b.draw(round, 1552, 772, 176, 176);
        b.setColor(Color.WHITE);
    }

    /** Render only chunks whose world-space quads overlap the 1920px viewport. */
    void paintGround(SpriteBatch b, double distanceMeters) {
        long first = SceneryLayout.firstChunk(distanceMeters);
        window.prepare(first);
        for (long idx = first; idx <= first + 3; idx++)
            chunk(b, window.get(idx), SceneryLayout.left(idx, distanceMeters));
        road(b, distanceMeters * 7.5);
        b.setColor(Color.WHITE);
    }

    /** HUD must be above bus and scenery, but below glyphs, with alpha blending. */
    void paintHud(SpriteBatch b) {
        rect(b, HUD_BG, 22f, 780f, 1295f, 266f);
        rect(b, HUD_ACCENT, 22f, 780f, 10f, 266f);
        b.setColor(Color.WHITE);
    }

    private void rect(SpriteBatch b, Color color, float x, float y, float w, float h) {
        b.setColor(color);
        b.draw(solid, x, y, w, h);
    }

    private void chunk(SpriteBatch b, WorldChunk c, float x) {
        rect(b, GRASS, x, 195, CHUNK + 1, 235);
        if (c.biome() == Biome.RIVER || c.biome() == Biome.BRIDGE) {
            b.setColor(0.35f, 0.72f, 0.91f, 1f);
            b.draw(solid, x, 258, CHUNK, 160);
            b.setColor(0.90f, 0.98f, 1f, 1f);
            for (int i = 0; i < 3; i++)
                b.draw(solid, x + 60 + i * 185, 325, 90, 6);
            if (c.biome() == Biome.BRIDGE) {
                b.setColor(0.72f, 0.68f, 0.64f, 1f);
                b.draw(solid, x + 25, 420, 585, 20);
                b.draw(solid, x + 110, 410, 25, 90);
                b.draw(solid, x + 510, 410, 25, 90);
            }
        } else if (c.biome() == Biome.URBAN || c.biome() == Biome.RESIDENTIAL) {
            for (int i = 0; i < 4; i++) {
                float bx = x + 20 + i * 156;
                float h = 185 + (Math.abs(c.detailSeed() >> (i * 5)) % 170);
                if (c.biome() == Biome.RESIDENTIAL) h *= 0.7f;
                rect(b, i % 2 == 0 ? BUILDING_A : BUILDING_B, bx, 384, 126, h);
                b.setColor(0.34f, 0.60f, 0.78f, 1f);
                for (int k = 0; k < 3; k++)
                    b.draw(solid, bx + 18 + 35 * k, 405 + h / 2, 23, 38);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                float tx = x + 80 + i * 120;
                float ty = 380 + (i % 2) * 35;
                b.setColor(0.55f, 0.35f, 0.25f, 1f);
                b.draw(solid, tx - 10, ty, 20, 112);
                b.setColor(i % 2 == 0 ? TREE_A : TREE_B);
                b.draw(round, tx - 65, ty + 61, 130, 130);
            }
        }
    }

    private void road(SpriteBatch b, double distancePx) {
        b.setColor(0.43f, 0.49f, 0.57f, 1f);
        b.draw(solid, 0, 0, 1920, 268);
        b.setColor(0.99f, 0.97f, 0.80f, 1f);
        int shift = (int) (distancePx % 230);
        for (int x = -230; x <= 1920; x += 230)
            b.draw(solid, x - shift, 115, 105, 11);
        b.setColor(0.83f, 0.88f, 0.77f, 1f);
        b.draw(solid, 0, 263, 1920, 15);
    }

    @Override public void dispose() { primitiveAtlas.dispose(); }
}
