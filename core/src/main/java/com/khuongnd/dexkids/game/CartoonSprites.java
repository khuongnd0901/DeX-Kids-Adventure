package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.khuongnd.dexkids.world.Biome;
import com.khuongnd.dexkids.world.ProceduralWorldGenerator;
import com.khuongnd.dexkids.world.WorldChunk;
import com.khuongnd.dexkids.world.WorldWindow;

/**
 * Bounded atlas-backed layered 2D scene. All scenery positions derive from the
 * journey's absolute smoothed distance, not a mutable per-frame camera offset.
 */
final class CartoonSprites implements Disposable {
    private final TextureAtlas atlas;
    private final TextureRegion bus, wheel, idle, blink, wave, talk, sleep, surprised;
    private final TextureRegion tree, cloud, hills, building, house, bush, lamp, bridge, flower, glow;
    private final WorldWindow scenery = new WorldWindow(new ProceduralWorldGenerator(20261008L), 1, 4);
    private final CharacterAnimationController actor = new CharacterAnimationController();
    private final VehicleMotionModel vehicle = new VehicleMotionModel();
    private long lastWaveCycle = 0;

    CartoonSprites() {
        var path = Gdx.files.internal("generated/kids.atlas");
        if (!path.exists()) path = Gdx.files.internal("assets/generated/kids.atlas");
        atlas = new TextureAtlas(path);
        bus = required("bus");
        wheel = required("wheel");
        idle = required("capybara_idle");
        blink = required("capybara_blink");
        wave = required("capybara_wave");
        talk = required("capybara_talk");
        sleep = required("capybara_sleep");
        surprised = required("capybara_surprised");
        tree = required("tree");
        cloud = required("cloud");
        hills = required("hills");
        building = required("building");
        house = required("house");
        bush = required("bush");
        lamp = required("lamp");
        bridge = required("bridge");
        flower = required("flower");
        glow = required("headlight_glow");
        actor.requestWave(); // welcoming nonverbal gesture, no automatic spoken claims
    }
    private TextureRegion required(String name) {
        TextureRegion region = atlas.findRegion(name);
        if (region == null) throw new IllegalStateException("Missing atlas region: " + name);
        return region;
    }

    /** Reused scene geometry; clouds drift gently even when the vehicle is stopped. */
    void drawFar(SpriteBatch batch, double distanceMeters, float elapsedSeconds,
                 WorldMoodResolver.Mood mood) {
        float shift = SceneryLayout.parallaxOffset(distanceMeters, 1.1, 640f);
        for (int i = -1; i < 5; i++)
            batch.draw(hills, i * 640f - shift, 407, 648, 230);
        float cloudDrift = SceneryLayout.parallaxOffset(distanceMeters, 0.25, 910f)
                + (elapsedSeconds * 5f) % 910f;
        for (int i = -1; i < 4; i++) {
            float x = i * 910f + 245f - (cloudDrift % 910f);
            // Keep the development HUD legible on the left.
            batch.draw(cloud, x, 730f + (i % 2) * 67f, 290, 130);
        }
    }

    void drawEnvironment(SpriteBatch batch, double distanceMeters,
                         WorldMoodResolver.Mood mood) {
        long first = SceneryLayout.firstChunk(distanceMeters);
        scenery.prepare(first);
        for (long idx = first - 1; idx <= first + 4; idx++) {
            WorldChunk chunk = scenery.get(idx);
            float x = SceneryLayout.left(idx, distanceMeters);
            float jitter = (chunk.detailSeed() & 31) - 16;
            Biome biome = chunk.biome();
            switch (biome) {
                case URBAN -> {
                    batch.draw(building, x + 8, 392, 225, 332);
                    batch.draw(building, x + 247, 394, 178, 263);
                    drawLamp(batch, x + 510, mood);
                    batch.draw(bush, x + 400, 382, 149, 86);
                }
                case RESIDENTIAL -> {
                    batch.draw(house, x + 19, 386, 279, 272);
                    batch.draw(tree, x + 339 + jitter, 393, 190, 247);
                    batch.draw(bush, x + 498, 380, 142, 90);
                }
                case PARK -> {
                    batch.draw(tree, x + 47 + jitter, 375, 215, 280);
                    batch.draw(tree, x + 347, 386, 179, 233);
                    batch.draw(bush, x + 230, 380, 194, 112);
                    batch.draw(flower, x + 510, 380, 101, 95);
                }
                case RIVER -> {
                    batch.draw(tree, x + 35, 403, 145, 187);
                    batch.draw(bush, x + 479, 382, 146, 89);
                }
                case BRIDGE -> {
                    batch.draw(bridge, x + 86, 406, 442, 228);
                    batch.draw(bush, x + 520, 382, 120, 87);
                }
                case COUNTRYSIDE, GENERAL -> {
                    batch.draw(tree, x + 67 + jitter, 389, 192, 249);
                    batch.draw(house, x + 291, 385, 233, 232);
                    batch.draw(flower, x + 509, 376, 85, 82);
                }
            }
        }
    }

    private void drawLamp(SpriteBatch batch, float x, WorldMoodResolver.Mood mood) {
        if (mood != WorldMoodResolver.Mood.DAY)
            batch.draw(glow, x - 54, 533, 200, 180);
        batch.draw(lamp, x, 392, 95, 254);
    }

    /** Actual TALKING frame is explicitly controlled by a future reviewed narration event. */
    void setNarrationActive(boolean active) { actor.setNarrationActive(active); }
    void setSurprised(boolean active) { actor.setSurprised(active); }
    void requestWave() { actor.requestWave(); }

    void drawVehicle(SpriteBatch batch, float deltaSeconds, float elapsedSeconds,
                     double distanceMeters, double speedMetersPerSecond,
                     WorldMoodResolver.Mood mood) {
        vehicle.advance(deltaSeconds, speedMetersPerSecond);
        // A nonverbal greeting every 18 simulated seconds; speech NEVER auto-starts.
        long cycle = (long) Math.floor(elapsedSeconds / 18.0);
        if (cycle > lastWaveCycle && speedMetersPerSecond > 0.65) {
            actor.requestWave();
            lastWaveCycle = cycle;
        }
        actor.drive(deltaSeconds, speedMetersPerSecond);

        float x = 285f;
        float y = 231f + vehicle.bodyBob(speedMetersPerSecond);
        float pitch = vehicle.bodyPitchDegrees();
        // body + independent wheels (wheels remain grounded as the suspension moves)
        batch.draw(bus, x, y + 20f, 305f, 120f, 610f, 267f, 1f, 1f, pitch);
        TextureRegion face = switch(actor.frame()) {
            case IDLE -> idle;
            case BLINK -> blink;
            case TALK_OPEN -> talk;
            case TALK_CLOSED -> idle;
            case WAVE -> wave;
            case SLEEP -> sleep;
            case SURPRISED -> surprised;
        };
        batch.draw(face, x + 445, y + 126, 150f, 156f);
        float rotation = vehicle.wheelDegrees(distanceMeters);
        drawWheel(batch, x + 128, y + 25, rotation);
        drawWheel(batch, x + 415, y + 25, rotation);
        if (mood == WorldMoodResolver.Mood.NIGHT)
            batch.draw(glow, x + 555, y + 115, 145, 90);
    }
    private void drawWheel(SpriteBatch batch, float x, float y, float rotation) {
        batch.draw(wheel, x, y, 46, 46, 92, 92, 1f, 1f, rotation);
    }
    @Override public void dispose() { atlas.dispose(); }
}
