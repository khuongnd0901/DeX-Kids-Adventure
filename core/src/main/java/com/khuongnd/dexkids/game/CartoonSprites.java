package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
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
    private final TextureAtlas extrasAtlas;
    private static final String[] EXTRA_NAMES = {"palm","pine","banana_tree","rice_field",
            "tea_bush","flower_bed","water_lily","water_boat","mountain_rocks",
            "market_stall","traffic_light","road_sign","street_tree","duck","bird",
            "butterfly","buffalo","kite"};
    // Each category is a stable set of region indices, not a per-frame allocation.
    private static final int[] CITY = {9,10,11,12,14};
    private static final int[] VILLAGE = {2,9,12,16};
    private static final int[] PARK_PROPS = {5,12,13,14,15,17};
    private static final int[] WATER_PROPS = {6,7,13,14,15};
    private static final int[] BRIDGE_PROPS = {7,10,11,14};
    private static final int[] FARM_PROPS = {0,1,2,3,4,8,16,17};
    private final TextureRegion[] extras = new TextureRegion[EXTRA_NAMES.length];
    private final TextureRegion bus, wheel, idle, blink, wave, talk, sleep, surprised;
    private final TextureRegion tree, cloud, hills, building, house, bush, lamp, bridge, flower, glow, riverWater;
    private final TextureRegion[] friendSprites = new TextureRegion[SceneryCast.FRIENDS.length];
    private final TextureRegion[] trafficSprites = new TextureRegion[SceneryCast.TRAFFIC.length];
    private final WorldWindow scenery = new WorldWindow(new ProceduralWorldGenerator(20261008L), 0, 3);
    private final CharacterAnimationController actor = new CharacterAnimationController();
    private final VehicleMotionModel vehicle = new VehicleMotionModel();
    private long lastWaveCycle = 0;
    private float passengerBob;
    private final Texture sauCostumesTexture;
    private final TextureRegion[] sauCostumes = new TextureRegion[4];
    private final Texture ongCostumesTexture;
    private final TextureRegion[] ongCostumes = new TextureRegion[4];
    private final Texture sauPassengerTexture, ongPassengerTexture;
    private final TextureRegion[] sauPassengers = new TextureRegion[4], ongPassengers = new TextureRegion[4];
    private final Texture sauStoryTexture;
    private final Texture ongStoryTexture;
    private final TextureRegion[] sauStory = new TextureRegion[4];
    private final TextureRegion[] ongStory = new TextureRegion[4];
    private StoryCompanionPose storyPose = StoryCompanionPose.WAVE;
    private String storyFocus = "BOTH";
    private int storyFriend;
    private boolean storyFlower;

    CartoonSprites() {
        var path = Gdx.files.internal("generated/kids.atlas");
        if (!path.exists()) path = Gdx.files.internal("assets/generated/kids.atlas");
        atlas = new TextureAtlas(path);
        var extrasPath = Gdx.files.internal("generated/extras.atlas");
        if (!extrasPath.exists()) extrasPath = Gdx.files.internal("assets/generated/extras.atlas");
        extrasAtlas = new TextureAtlas(extrasPath);
        for (int i=0;i<EXTRA_NAMES.length;i++) {
            extras[i]=extrasAtlas.findRegion(EXTRA_NAMES[i]);
            if (extras[i]==null)throw new IllegalStateException("Missing extra: "+EXTRA_NAMES[i]);
        }
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
        riverWater = required("river_water");
        for (int i = 0; i < friendSprites.length; i++)
            friendSprites[i] = required(SceneryCast.FRIENDS[i]);
        for (int i = 0; i < trafficSprites.length; i++)
            trafficSprites[i] = required(SceneryCast.TRAFFIC[i]);
        // Art is an explicit portrait-style fictional costume, never GPS evidence.
        // A single small sheet avoids extra texture uploads per event or frame.
        var sauPath = Gdx.files.internal("characters/sau-costumes.png");
        if (!sauPath.exists()) sauPath = Gdx.files.internal("assets/characters/sau-costumes.png");
        if (sauPath.exists()) {
            sauCostumesTexture = new Texture(sauPath);
            sauCostumesTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            int w = sauCostumesTexture.getWidth() / 2;
            int h = sauCostumesTexture.getHeight() / 2;
            for (int i = 0; i < 4; i++)
                sauCostumes[i] = new TextureRegion(sauCostumesTexture, (i % 2) * w,
                        (i / 2) * h, w, h);
        } else sauCostumesTexture = null; // no crash if optional child art missing
        var ongPath = Gdx.files.internal("characters/ong-costumes.png");
        if (!ongPath.exists()) ongPath = Gdx.files.internal("assets/characters/ong-costumes.png");
        if (ongPath.exists()) {
            ongCostumesTexture = new Texture(ongPath);
            ongCostumesTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            int w = ongCostumesTexture.getWidth() / 2;
            int h = ongCostumesTexture.getHeight() / 2;
            for (int i = 0; i < 4; i++)
                ongCostumes[i] = new TextureRegion(ongCostumesTexture, (i % 2) * w,
                        (i / 2) * h, w, h);
        } else ongCostumesTexture = null;
        sauStoryTexture = loadStorySheet("sau", sauStory);
        ongStoryTexture = loadStorySheet("ong", ongStory);
        sauPassengerTexture = loadPassengerSheet("sau", sauPassengers);
        ongPassengerTexture = loadPassengerSheet("ong", ongPassengers);
        actor.requestWave(); // welcoming nonverbal gesture, no automatic spoken claims
    }
    private static Texture loadPassengerSheet(String child, TextureRegion[] regions) {
        var path = Gdx.files.internal("characters/" + child + "-passenger-v3.png");
        if (!path.exists()) path = Gdx.files.internal("assets/characters/" + child + "-passenger-v3.png");
        if (!path.exists()) return null;
        // Trim transparent cell padding once so every waist sits at the window sill.
        Pixmap pixels = new Pixmap(path);
        Texture texture = new Texture(pixels);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        int w = pixels.getWidth() / 2, h = pixels.getHeight() / 2;
        for (int i = 0; i < 4; i++) {
            int cellX = (i % 2) * w, cellY = (i / 2) * h;
            int left = w, top = h, right = -1, bottom = -1;
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                if ((pixels.getPixel(cellX + x, cellY + y) & 255) < 128) continue;
                left = Math.min(left, x); right = Math.max(right, x);
                top = Math.min(top, y); bottom = Math.max(bottom, y);
            }
            regions[i] = right < left ? new TextureRegion(texture, cellX, cellY, w, h) :
                new TextureRegion(texture, cellX + left, cellY + top,
                        right - left + 1, bottom - top + 1);
        }
        pixels.dispose();
        return texture;
    }
    private static Texture loadStorySheet(String child, TextureRegion[] regions) {
        var path = Gdx.files.internal("characters/" + child + "-story-v2.png");
        if (!path.exists()) path = Gdx.files.internal("assets/characters/" + child + "-story-v2.png");
        if (!path.exists()) return null;
        Texture texture = new Texture(path);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        int w = texture.getWidth() / 2, h = texture.getHeight() / 2;
        for (int i = 0; i < 4; i++) regions[i] = new TextureRegion(texture, (i % 2) * w, (i / 2) * h, w, h);
        return texture;
    }
    void setStoryPose(StoryCompanionPose pose) { storyPose = pose == null ? StoryCompanionPose.WAVE : pose; }
    void setStoryFocus(String focus) { storyFocus = focus; }
    void setStoryBeat(String id) {
        storyFriend = id.contains("fox") ? 1 : id.contains("panda") ? 2 :
            id.contains("cat") || id.contains("animals") ? 3 : 0;
        storyFlower = id.contains("flower");
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
        for (int i = 0; i < 4; i++)
            batch.draw(hills, i * 640f - shift, 407, 648, 230);
        float cloudDrift = SceneryLayout.parallaxOffset(distanceMeters, 0.25, 910f)
                + (elapsedSeconds * 5f) % 910f;
        for (int i = 0; i < 3; i++) {
            float x = i * 910f + 245f - (cloudDrift % 910f);
            // Keep the development HUD legible on the left.
            batch.draw(cloud, x, 730f + (i % 2) * 67f, 290, 130);
        }
    }

    void drawEnvironment(SpriteBatch batch, double distanceMeters,
                         WorldMoodResolver.Mood mood) {
        drawEnvironment(batch, distanceMeters, mood,
                new PoiSceneDirector.Scene(Biome.GENERAL, 0f, 0L, "", false));
    }

    /** Crossfade POI-inspired props on an anchored chunk range. Background world
     * and fictional traffic remain deterministic; no GPS object is a road match. */
    void drawEnvironment(SpriteBatch batch, double distanceMeters,
                         WorldMoodResolver.Mood mood, PoiSceneDirector.Scene theme) {
        long first = SceneryLayout.firstChunk(distanceMeters);
        scenery.prepare(first);
        for (long idx = first; idx <= first + 3; idx++) {
            WorldChunk chunk = scenery.get(idx);
            float x = SceneryLayout.left(idx, distanceMeters);
            float jitter = (chunk.detailSeed() & 31) - 16;
            Biome biome = chunk.biome();

            boolean themed = theme != null && theme.active()
                    && idx >= theme.fromChunk() && idx < theme.fromChunk() + 4
                    && biome != theme.biome();
            if (themed) {
                var original = batch.getColor();
                float r = original.r, g = original.g, b = original.b, a = original.a;
                float layer = Math.min(0.9f, Math.max(0f, theme.alpha() * .9f));
                batch.setColor(r, g, b, a * (1f - layer));
                drawBiomeDecorations(batch, x, jitter, biome, mood);
                batch.setColor(r, g, b, a * layer);
                drawBiomeDecorations(batch, x, jitter, theme.biome(), mood);
                batch.setColor(r, g, b, a);
            } else {
                drawBiomeDecorations(batch, x, jitter, biome, mood);
            }
            // Fictional road traffic and roadside companions, deterministically positioned.
            // These sprites never represent nearby GPS-detected vehicles or people.
            if (SceneryCast.showTraffic(idx, biome)) {
                int which = SceneryCast.trafficIndex(idx, chunk.detailSeed());
                TextureRegion vehicle = trafficSprites[which];
                float spriteW = (which == 5 || which == 4) ? 230f : 275f;
                batch.draw(vehicle, x + 290f, 117f, spriteW, 140f);
            }
            if (SceneryCast.showFriend(idx, biome)) {
                int which = SceneryCast.friendIndex(idx, chunk.detailSeed());
                batch.draw(friendSprites[which], x + 452f, 381f, 124f, 143f);
            }
        }
    }

    private void drawBiomeDecorations(SpriteBatch batch, float x, float jitter,
                                      Biome biome, WorldMoodResolver.Mood mood) {
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
                    batch.draw(riverWater, x + 12, 335, 614, 120);
                    batch.draw(tree, x + 35, 403, 145, 187);
                    batch.draw(bush, x + 479, 382, 146, 89);
                }
                case BRIDGE -> {
                    batch.draw(riverWater, x + 12, 335, 614, 120);
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

    /** Fixed four visible chunks, deterministic props and no per-frame allocations. */
    void drawExtras(SpriteBatch batch,double distanceMeters,PoiSceneDirector.Scene scene){
        long first=SceneryLayout.firstChunk(distanceMeters);
        scenery.prepare(first);
        for(long idx=first;idx<=first+3;idx++){
            WorldChunk chunk=scenery.get(idx);
            Biome biome=(scene!=null&&scene.active())?scene.biome():chunk.biome();
            int[] pool=switch(biome){
                case URBAN->CITY;
                case RESIDENTIAL->VILLAGE;
                case PARK->PARK_PROPS;
                case RIVER->WATER_PROPS;
                case BRIDGE->BRIDGE_PROPS;
                case COUNTRYSIDE,GENERAL->FARM_PROPS;
            };
            int seed=chunk.detailSeed()^(int)(idx*0x9E3779B9L);
            int which=pool[Math.floorMod(seed,pool.length)];
            float x=SceneryLayout.left(idx,distanceMeters)+105+(seed&31);
            float y=382f;
            float w=170f,h=158f;
            // Stylised scenery has one physical ground/water plane; no floating signs.
            if(which==6||which==7||which==13){y=341f;w=200f;h=156f;}
            if(which==14||which==15||which==17){y=637f;w=150f;h=133f;}
            if(which==0||which==1||which==2||which==12){h=230f;w=170f;}
            batch.draw(extras[which],x,y,w,h);
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

        float x = VehicleLayout.X;
        passengerBob = vehicle.bodyBob(speedMetersPerSecond);
        float y = VehicleLayout.Y + passengerBob;
        float pitch = vehicle.bodyPitchDegrees();
        // Source bus has aspect 660:290; independently rotating wheels remain on road.
        batch.draw(bus, x, y, VehicleLayout.WIDTH / 2f, VehicleLayout.HEIGHT / 2f,
                VehicleLayout.WIDTH, VehicleLayout.HEIGHT, 1f, 1f, pitch);
        TextureRegion face = switch(actor.frame()) {
            case IDLE -> idle;
            case BLINK -> blink;
            case TALK_OPEN -> talk;
            case TALK_CLOSED -> idle;
            case WAVE -> wave;
            case SLEEP -> sleep;
            case SURPRISED -> surprised;
        };
        batch.draw(face, VehicleLayout.CAPYBARA_X,
                VehicleLayout.CAPYBARA_Y + passengerBob,
                VehicleLayout.CAPYBARA_W, VehicleLayout.CAPYBARA_H);
        float rotation = vehicle.wheelDegrees(distanceMeters);
        drawWheel(batch, VehicleLayout.wheelCenterX(0), VehicleLayout.wheelCenterY(), rotation);
        drawWheel(batch, VehicleLayout.wheelCenterX(1), VehicleLayout.wheelCenterY(), rotation);
        if (mood == WorldMoodResolver.Mood.NIGHT)
            batch.draw(glow, x + 810, y + 185, 145, 90);
    }
    void drawBusPassengers(SpriteBatch batch, float seconds, boolean sau, boolean ong) {
        if (sau && sauPassengerTexture != null) drawPassenger(batch,
                sauPassengers[StoryCompanionPose.WAVE.ordinal()], VehicleLayout.SAU_X, passengerBob);
        if (ong && ongPassengerTexture != null) drawPassenger(batch,
                ongPassengers[StoryCompanionPose.WAVE.ordinal()],
                sau ? VehicleLayout.ONG_X : VehicleLayout.SAU_X, passengerBob);
    }
    private void drawPassenger(SpriteBatch batch, TextureRegion pose, float x, float bounce) {
        float width = VehicleLayout.CHILD_H * pose.getRegionWidth() / pose.getRegionHeight();
        batch.draw(pose, x + (VehicleLayout.CHILD_W - width) / 2f,
                VehicleLayout.CHILD_Y + bounce, width, VehicleLayout.CHILD_H);
    }
    /**
     * The personalized Sâu companion appears only for SAU and BOTH audiences.
     * Cropped quadrants: explorer/firefighter/pilot/police. No per-frame allocations.
     */
    void drawSauCompanion(SpriteBatch batch, float elapsedSeconds,
                          boolean enabled, String costume) {
        drawSauCompanion(batch, elapsedSeconds, enabled, costume, false);
    }
    void drawSauCompanion(SpriteBatch batch, float elapsedSeconds,
                          boolean enabled, String costume, boolean both) {
        if (!enabled || (sauCostumesTexture == null && sauStoryTexture == null)) return;
        float bounce = (float) Math.sin(elapsedSeconds * 1.7f) * 5f;
        boolean active = !both || !"ONG".equals(storyFocus);
        TextureRegion pose = sauStoryTexture == null ? sauCostumes[costumeIndex(costume)] :
            sauStory[active ? storyPose.ordinal() : StoryCompanionPose.WAVE.ordinal()];
        if (!active) batch.setColor(.82f, .82f, .82f, 1);
        batch.draw(pose, both ? 1140f : 1310f,
                315f + bounce, both ? 248f : 285f, both ? 373f : 427f);
        batch.setColor(Color.WHITE);
        if (active) drawStoryProp(batch, both ? 1220f : 1390f, elapsedSeconds, 3);
    }
    void drawOngCompanion(SpriteBatch batch, float elapsedSeconds,
                          boolean enabled, String costume, boolean both) {
        if (!enabled || (ongCostumesTexture == null && ongStoryTexture == null)) return;
        float bounce = (float) Math.sin(elapsedSeconds * 1.9f + .6f) * 5f;
        boolean active = !both || !"SAU".equals(storyFocus);
        TextureRegion pose = ongStoryTexture == null ? ongCostumes[costumeIndex(costume)] :
            ongStory[active ? storyPose.ordinal() : StoryCompanionPose.WAVE.ordinal()];
        if (!active) batch.setColor(.82f, .82f, .82f, 1);
        batch.draw(pose, both ? 1555f : 1310f,
                315f + bounce, both ? 224f : 260f, both ? 336f : 390f);
        batch.setColor(Color.WHITE);
        if (active) drawStoryProp(batch, both ? 1630f : 1390f, elapsedSeconds, 1);
    }
    private void drawStoryProp(SpriteBatch batch, float x, float seconds, int count) {
        // Visible toy props connect each pose with the current fictional activity.
        if (storyPose == StoryCompanionPose.PET) batch.draw(friendSprites[storyFriend], x + 88, 293, 76, 87);
        else if (storyPose == StoryCompanionPose.LOOK_UP) batch.draw(cloud, x - 22, 740, 155, 65);
        else if (storyFlower) batch.draw(flower, x + 85, 293, 68, 76);
        else if (storyPose == StoryCompanionPose.COUNT) {
            for (int i = 0; i < count; i++) batch.draw(flower, x - 5 + i * 39,
                718 + (float)Math.sin(seconds * 2 + i) * 3, 30, 38);
        }
    }
    private static int costumeIndex(String costume) {
        return switch (costume == null ? "" : costume) {
            case "FIREFIGHTER" -> 1;
            case "PILOT" -> 2;
            case "POLICE" -> 3;
            default -> 0;
        };
    }

    private void drawWheel(SpriteBatch batch, float centerX, float centerY, float rotation) {
        float r = VehicleLayout.WHEEL_RADIUS;
        batch.draw(wheel, centerX - r, centerY - r, r, r, r * 2, r * 2, 1f, 1f, rotation);
    }
    @Override public void dispose() {
        if (sauCostumesTexture != null) sauCostumesTexture.dispose();
        if (ongCostumesTexture != null) ongCostumesTexture.dispose();
        if (sauStoryTexture != null) sauStoryTexture.dispose();
        if (ongStoryTexture != null) ongStoryTexture.dispose();
        if (sauPassengerTexture != null) sauPassengerTexture.dispose();
        if (ongPassengerTexture != null) ongPassengerTexture.dispose();
        atlas.dispose();
        extrasAtlas.dispose();
    }
}
