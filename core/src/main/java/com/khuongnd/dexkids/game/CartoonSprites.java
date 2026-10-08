package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/** Single atlas-backed child friendly bus and capybara, generated from tracked SVG sources. */
final class CartoonSprites implements Disposable {
    private final TextureAtlas atlas;
    private final TextureRegion bus;
    private final TextureRegion wheel;
    private final TextureRegion idle;
    private final TextureRegion blink;
    private final TextureRegion wave;
    private final TextureRegion talk;

    CartoonSprites() {
        atlas = new TextureAtlas(Gdx.files.internal("generated/kids.atlas"));
        bus = required("bus");
        wheel = required("wheel");
        idle = required("capybara_idle");
        blink = required("capybara_blink");
        wave = required("capybara_wave");
        talk = required("capybara_talk");
    }

    private TextureRegion required(String name) {
        TextureRegion region = atlas.findRegion(name);
        if (region == null) throw new IllegalStateException("Missing atlas region: " + name);
        return region;
    }

    void draw(SpriteBatch batch, float time, double distanceMeters, double speedMetersPerSecond) {
        final float baseX = 285f;
        final float baseY = 232f;
        final float bob = speedMetersPerSecond > 0.75 ? (float) Math.sin(time * 6.0) * 3f : 0f;
        final float x = baseX;
        final float y = baseY + bob;
        // Sprite from source SVG: pastel bus with transparent window regions.
        batch.draw(bus, x, y + 20, 610, 267);
        TextureRegion current = idle;
        float phase = time % 11.5f;
        if (phase > 7.8f && phase < 9.0f) current = wave;
        else if (phase > 10.7f && phase < 10.87f) current = blink;
        // Talking frame is available but intentionally unused without actual narration.
        batch.draw(current, x + 445, y + 126, 150, 156);
        final float rotation = (float) ((-distanceMeters * 29) % 360);
        drawWheel(batch, x + 128, y + 26, rotation);
        drawWheel(batch, x + 460, y + 26, rotation);
    }

    private void drawWheel(SpriteBatch batch, float x, float y, float rotation) {
        batch.draw(wheel, x, y, 46, 46, 92, 92, 1f, 1f, rotation);
    }

    @Override public void dispose() { atlas.dispose(); }
}
