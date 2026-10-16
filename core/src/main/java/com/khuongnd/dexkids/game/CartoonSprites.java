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
import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.world.WorldChunk;
import com.khuongnd.dexkids.world.WorldWindow;

/**
 * Bounded atlas-backed layered 2D scene. All scenery positions derive from the
 * journey's absolute smoothed distance, not a mutable per-frame camera offset.
 */
final class CartoonSprites implements Disposable {
    private final TrafficDepthModel traffic;
    private final boolean storybookStyle;
    private final TextureAtlas atlas;
    private final TextureAtlas extrasAtlas;
    private final StorySupportAtlas storySupport;
    private final TextureAtlas pngAtlas;
    private final TextureRegion compositeBus;
    private final TextureRegion[] pngPoses = new TextureRegion[CapybaraArtPose.values().length];
    private final CapybaraArtPose.Transition pngTransition = new CapybaraArtPose.Transition();
    private String capybaraBeatId = "welcome";
    private AnimationTimeline.Phase capybaraPhase = AnimationTimeline.Phase.INTERRUPTED;
    private static final String[] EXTRA_NAMES = {"palm","pine","banana_tree","rice_field",
            "tea_bush","flower_bed","water_lily","water_boat","mountain_rocks",
            "market_stall","traffic_light","road_sign","street_tree","duck","bird",
            "butterfly","buffalo","kite"};
    // Each category is a stable set of region indices, not a per-frame allocation.
    private static final int[] CITY = {9,10,11,12,14};
    private static final int[] VILLAGE = {2,9,12,16};
    private static final int[] PARK_PROPS = {5,12,14,15,17};
    private static final int[] WATER_PROPS = {6,7,13,14,15};
    private static final int[] BRIDGE_PROPS = {6,7,13,14};
    private static final int[] FARM_PROPS = {0,1,2,3,4,8,16,17};
    private final TextureRegion[] extras = new TextureRegion[EXTRA_NAMES.length];
    private final TextureRegion bus, wheel, idle, blink, wave, talk, sleep, surprised;
    private final TextureRegion tree, cloud, hills, building, house, bush, lamp, bridge, flower, glow, riverWater;
    private final TextureRegion[] friendSprites = new TextureRegion[SceneryCast.FRIENDS.length];
    private final TextureRegion[] trafficSprites = new TextureRegion[SceneryCast.TRAFFIC.length];
    private final WorldWindow scenery;
    // Uses existing biome prop pools; trip-specific visual variety, no frame allocations.
    private final M10SceneVariety variety;
    private final M10PropAtlas m10Art = new M10PropAtlas();
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
    // One bounded page: Sâu pose-specific garment layers plus optional COMPLETE Ong sprites.
    // Ong never draws clothing over a base character; protected source art stays intact.
    private final TextureAtlas childOutfitsAtlas;
    private final TextureRegion[][] sauHeroOutfits = new TextureRegion[OutfitRotation.COUNT][4];
    private final TextureRegion[][] sauPassengerOutfits = new TextureRegion[OutfitRotation.COUNT][4];
    private final TextureRegion[][] ongCompleteOutfits = new TextureRegion[OutfitRotation.COUNT][4];
    private final TextureRegion[][] ongCompletePassengers = new TextureRegion[OutfitRotation.COUNT][4];
    private final boolean ongCompleteAvailable;
    private final OutfitRotation outfitRotation = new OutfitRotation(System.nanoTime());
    private StoryCompanionPose storyPose = StoryCompanionPose.WAVE;
    private VisualCue visualCue = VisualCue.DEFAULT;
    private float storyMotionX, storyMotionY, storyMotionScale = 1f;
    private boolean storyAnswerPhase;
    private String storyFocus = "BOTH";
    private int storyFriend;
    private boolean storyFlower;
    private boolean authoredCountVisual;

    CartoonSprites(M10SceneVariety variety, WorldWindow scenery) {
        this.variety = java.util.Objects.requireNonNull(variety);
        this.scenery = java.util.Objects.requireNonNull(scenery);
        var path = Gdx.files.internal("generated/kids.atlas");
        if (!path.exists()) path = Gdx.files.internal("assets/generated/kids.atlas");
        atlas = new TextureAtlas(path);
        var extrasPath = Gdx.files.internal("generated/extras.atlas");
        if (!extrasPath.exists()) extrasPath = Gdx.files.internal("assets/generated/extras.atlas");
        extrasAtlas = new TextureAtlas(extrasPath);
        storySupport = new StorySupportAtlas();
        for (int i=0;i<EXTRA_NAMES.length;i++) {
            extras[i]=extrasAtlas.findRegion(EXTRA_NAMES[i]);
            if (extras[i]==null)throw new IllegalStateException("Missing extra: "+EXTRA_NAMES[i]);
        }
        // Repository switch is packaged with the atlas. Set to legacy and
        // regenerate/build to roll back; old art remains resident/available.
        var pngPath = Gdx.files.internal("generated/t048/t048.atlas");
        if (!pngPath.exists()) pngPath = Gdx.files.internal("assets/generated/t048/t048.atlas");
        var modePath = Gdx.files.internal("art-mode.txt");
        if (!modePath.exists()) modePath = Gdx.files.internal("assets/art-mode.txt");
        var stylePath = Gdx.files.internal("art-style.txt");
        if (!stylePath.exists()) stylePath = Gdx.files.internal("assets/art-style.txt");
        storybookStyle = stylePath.exists() && "storybook-v2".equals(stylePath.readString().trim());
        traffic = new TrafficDepthModel(20261008, storybookStyle);
        boolean legacy = modePath.exists() && "legacy".equals(modePath.readString().trim());
        pngAtlas = !legacy && pngPath.exists() ? new TextureAtlas(pngPath) : null;
        compositeBus = pngAtlas == null ? null : requiredPng("bus_side_with_driver");
        if (pngAtlas != null) for (var pose : CapybaraArtPose.values())
            pngPoses[pose.ordinal()] = requiredPng(pose.region);
        Gdx.app.log("T048Art", compositeBus == null ? "legacy" : "png composite + four poses");
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
        var outfitPath = Gdx.files.internal("generated/child-outfits.atlas");
        if (!outfitPath.exists()) outfitPath = Gdx.files.internal("assets/generated/child-outfits.atlas");
        childOutfitsAtlas = outfitPath.exists() ? new TextureAtlas(outfitPath) : null;
        if (childOutfitsAtlas != null) {
            String[] heroes = {"solar", "tech", "web", "thunder", "shield", "shadow"};
            String[] princesses = {"rose", "ocean", "sky", "forest", "starlight", "lavender"};
            for (int i = 0; i < OutfitRotation.COUNT; i++) {
                for (int pose = 0; pose < 4; pose++) {
                    sauHeroOutfits[i][pose] = childOutfitsAtlas.findRegion("sau_" + heroes[i] + "_story_" + pose);
                    ongCompleteOutfits[i][pose] = childOutfitsAtlas.findRegion("ong_complete_" + princesses[i] + "_story_" + pose);
                    sauPassengerOutfits[i][pose] = childOutfitsAtlas.findRegion("sau_" + heroes[i] + "_passenger_" + pose);
                    ongCompletePassengers[i][pose] = childOutfitsAtlas.findRegion("ong_complete_" + princesses[i] + "_passenger_" + pose);
                    if (sauHeroOutfits[i][pose] == null || sauPassengerOutfits[i][pose] == null)
                        throw new IllegalStateException("Missing T-053 pose-aware child outfit region " + i + "/" + pose);
                }
            }
        }
        boolean complete = childOutfitsAtlas != null;
        for(int style=0;style<OutfitRotation.COUNT;style++)for(int pose=0;pose<4;pose++)
            complete &= ongCompleteOutfits[style][pose]!=null && ongCompletePassengers[style][pose]!=null;
        ongCompleteAvailable = complete; // Atomic pack capability; never mix partial designs/poses.
        sauStoryTexture = loadStorySheet("sau", sauStory);
        ongStoryTexture = ongCompleteAvailable ? null : loadStorySheet("ong", ongStory);
        sauPassengerTexture = loadPassengerSheet("sau", sauPassengers);
        ongPassengerTexture = ongCompleteAvailable ? null : loadPassengerSheet("ong", ongPassengers);
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
    void setVisualCue(VisualCue cue) {
        visualCue=cue==null ? VisualCue.DEFAULT : cue;
        storyPose=StoryCompanionPose.fromCue(visualCue);
    }
    private boolean storyDirected;
    private com.khuongnd.dexkids.story.adventure.StoryEpisode.Actor storyActor;
    void setStoryActor(com.khuongnd.dexkids.story.adventure.StoryEpisode.Actor actor) {storyActor=actor;}
    private boolean guideActs() {return storyDirected && storyActor==com.khuongnd.dexkids.story.adventure.StoryEpisode.Actor.CAPYBARA;}
    private boolean storyBoth;
    private float storyDirectedElapsed;
    void setStoryDirected(boolean directed, float elapsed) {
        storyDirected = directed;
        storyDirectedElapsed = elapsed;
    }
    /** Small semantic gestures transform the original sprites and matching outfit together. */
    private float directedX(boolean active) {
        return storyDirected && active && !guideActs() ? StoryGesture.sample(visualCue.action(), storyDirectedElapsed).x() : 0;
    }
    private float directedY(boolean active) {
        return storyDirected && active && !guideActs() ? StoryGesture.sample(visualCue.action(), storyDirectedElapsed).y() : 0;
    }
    private float directedRotation(boolean active) {
        return storyDirected && active && !guideActs() ? StoryGesture.sample(visualCue.action(), storyDirectedElapsed).rotation() : 0;
    }
    void setStoryFocus(String focus) { storyFocus = focus; }
    void setStoryAnimation(float x, float y, float scale, boolean answer) {
        storyMotionX = Float.isFinite(x) ? Math.max(-40f, Math.min(40f, x)) : 0f;
        storyMotionY = Float.isFinite(y) ? Math.max(-40f, Math.min(40f, y)) : 0f;
        storyMotionScale = Float.isFinite(scale) ? Math.max(.9f, Math.min(1.1f, scale)) : 1f;
        storyAnswerPhase = answer;
        if (answer) storyPose = StoryCompanionPose.WAVE;
    }
    void setStoryBeat(String id) {
        authoredCountVisual = "sau-count".equals(id) || "ong-one".equals(id)
                || "both-count".equals(id);
        storyFriend = id.contains("fox") ? 1 : id.contains("panda") ? 2 :
            id.contains("cat") || id.contains("animals") ? 3 : 0;
        storyFlower = id.contains("flower");
    }
    void setCapybaraContext(String beatId, AnimationTimeline.Phase phase) {
        capybaraBeatId = beatId;
        capybaraPhase = phase;
    }
    private TextureRegion requiredPng(String name) {
        TextureRegion region = pngAtlas.findRegion(name);
        if (region == null) throw new IllegalStateException("Missing T-048 region: " + name);
        return region;
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
                PoiSceneDirector.Scene.inactive(false));
    }

    /** Crossfade POI-inspired props on an anchored chunk range. Background world
     * and fictional traffic remain deterministic; no GPS object is a road match. */
    void drawEnvironment(SpriteBatch batch, double distanceMeters,
                         WorldMoodResolver.Mood mood, PoiSceneDirector.Scene theme) {
        drawEnvironment(batch, distanceMeters, mood, theme, false);
    }

    void drawEnvironment(SpriteBatch batch, double distanceMeters,
                         WorldMoodResolver.Mood mood, PoiSceneDirector.Scene theme,
                         boolean authoredBackdrop) {
        long first = SceneryLayout.firstChunk(distanceMeters);
        scenery.prepare(first);
        for (long idx = first; idx <= first + 3; idx++) {
            WorldChunk chunk = scenery.get(idx);
            float x = SceneryLayout.left(idx, distanceMeters);
            float jitter = (chunk.detailSeed() & 31) - 16;
            Biome biome = authoredBackdrop && theme != null ? theme.biome() : chunk.biome();

            boolean themed = theme != null && theme.active()
                    && idx >= theme.fromChunk() && idx < theme.fromChunk() + 4
                    && biome != theme.biome();
            if (authoredBackdrop) {
                // Authored panorama owns its ground/water; suppress unrelated chunk lamps/trees.
            } else if (themed) {
                var original = batch.getColor();
                float r = original.r, g = original.g, b = original.b, a = original.a;
                float layer = Math.min(1f, Math.max(0f, theme.alpha()));
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
            if (!authoredBackdrop && SceneryCast.showFriend(idx, biome)) {
                int which = SceneryCast.friendIndex(idx, chunk.detailSeed());
                batch.draw(friendSprites[which], x + 452f, 381f, 124f, 143f);
            }
        }
    }

    void advanceTraffic(float delta, double distanceMeters, PoiSceneDirector.Scene scene) {
        long first = SceneryLayout.firstChunk(distanceMeters);
        scenery.prepare(first);
        Biome biome = scene != null && scene.active() ? scene.biome() : scenery.get(first).biome();
        traffic.advance(delta, biome);
    }
    void drawTraffic(SpriteBatch batch, TrafficRenderOrder.Plane plane) {
        drawTrafficSlot(batch, traffic.far(), plane);
        drawTrafficSlot(batch, traffic.near(), plane);
    }
    private void drawTrafficSlot(SpriteBatch batch, TrafficLayout slot, TrafficRenderOrder.Plane plane) {
        float busGround = VehicleLayout.groundY(compositeBus != null);
        if (!slot.visible || TrafficRenderOrder.plane(slot.groundY, busGround) != plane) return;
        TextureRegion sprite = trafficSprites[slot.kind];
        batch.draw(sprite, slot.x, slot.y,
                slot.height * sprite.getRegionWidth() / sprite.getRegionHeight(), slot.height);
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
    void drawExtras(SpriteBatch batch,double distanceMeters,PoiSceneDirector.Scene scene,String backdropKey){
        drawExtras(batch,distanceMeters,scene,backdropKey,0f);
    }
    void drawExtras(SpriteBatch batch,double distanceMeters,PoiSceneDirector.Scene scene,
                    String backdropKey,float elapsedSeconds){
        long first=SceneryLayout.firstChunk(distanceMeters);
        variety.beginVisibleWindow(first);
        scenery.prepare(first);
        for(long idx=first;idx<=first+3;idx++){
            WorldChunk chunk=scenery.get(idx);
            Biome biome=(scene!=null&&scene.active()&&idx>=scene.fromChunk()&&idx<scene.fromChunk()+4)?scene.biome():chunk.biome();
            if(backdropKey!=null) biome=ScenePropLayout.biome(backdropKey,biome);
            int[] pool=switch(biome){
                case URBAN->CITY;
                case RESIDENTIAL->VILLAGE;
                case PARK->PARK_PROPS;
                case RIVER->WATER_PROPS;
                case BRIDGE->BRIDGE_PROPS;
                case COUNTRYSIDE,GENERAL->FARM_PROPS;
            };
            int seed=chunk.detailSeed()^(int)(idx*0x9E3779B9L);
            int which=variety.pick(idx, biome, pool);
            float x=SceneryLayout.left(idx,distanceMeters)+105+(seed&31);
            float y=backdropKey==null?382f:282f;
            float w=170f,h=158f;
            TextureRegion upgrade = backdropKey == null && (seed & 3) != 0
                    ? m10Art.choose(biome, which) : null;
            // M10 IDs describe grounded props, even when the old selection
            // index described a flying butterfly/bird or bobbing animal.
            if (upgrade == null) {
                // Stylised scenery has one physical ground/water plane; no floating signs.
                if(which==6||which==7||which==13){
                    y=ScenePropLayout.waterY(backdropKey);w=200f;h=156f;
                    if(backdropKey!=null) x=510f+Math.floorMod(seed,3)*300f;
                }
                if(which==14||which==15||which==17){y=637f;w=150f;h=133f;}
                // V2 sparrow grips an authored branch: it is a perched sprite, not a flight pose.
                if(which==14 && storybookStyle){y=ScenePropLayout.PERCHED_BIRD_Y;w=110f;h=w*160f/180f;}
                if(which==0||which==1||which==2||which==12){h=230f;w=170f;}
                // Source-independent ambient cartoon motion; never assert real animals/traffic.
                if((which==14 && !storybookStyle) || which==15 || which==17) {
                    x += (float)Math.sin(elapsedSeconds*.8 + (seed&3))*22f;
                    y += (float)Math.sin(elapsedSeconds*1.9 + (seed&7))*10f;
                } else if(which==6 || which==7 || which==13) {
                    y += (float)Math.sin(elapsedSeconds*1.4 + (seed&3))*5f;
                } else if(which==5) {
                    y += (float)Math.sin(elapsedSeconds*1.6 + (seed&3))*3f;
                }
            }
            // At most two already-loaded 1024px resident prop pages; no GL
            // creation/IO in drawExtras. Keep existing art for water/POI scenes.
            if (upgrade != null) {
                float fittedW=ScenePropLayout.fitWidth(w,h,upgrade.getRegionWidth(),upgrade.getRegionHeight());
                x+=(w-fittedW)/2f;
                h=fittedW*upgrade.getRegionHeight()/upgrade.getRegionWidth();w=fittedW;
            }
            if (upgrade != null) batch.draw(upgrade,x,y,w,h);
            else batch.draw(extras[which],x,y,w,h);
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
        if (compositeBus != null) {
            batch.draw(compositeBus,x,y,VehicleLayout.PNG_WIDTH/2,VehicleLayout.PNG_HEIGHT/2,
                    VehicleLayout.PNG_WIDTH,VehicleLayout.PNG_HEIGHT,1f,1f,pitch);
            // Driver and wheels are baked into this image: no legacy overlays.
            // Keep actor motion and expression visible in the standing guide.
            pngTransition.advance(CapybaraArtPose.resolve(actor.frame(),capybaraBeatId,capybaraPhase),deltaSeconds);
            float alpha = pngTransition.alpha();
            if (alpha < 1) drawPngPose(batch,pngTransition.from(),1-alpha);
            drawPngPose(batch,pngTransition.current(),alpha);
            if(guideActs())drawSupportInteraction(batch,VehicleLayout.PNG_CAPYBARA_X,
                VehicleLayout.PNG_CAPYBARA_Y+passengerBob,VehicleLayout.PNG_CAPYBARA_W,VehicleLayout.PNG_CAPYBARA_H);
            if (mood == WorldMoodResolver.Mood.NIGHT)
                batch.draw(glow,x+VehicleLayout.PNG_WIDTH-70,y+100,145,90);
            return;
        }
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
    private void drawPngPose(SpriteBatch batch, CapybaraArtPose pose, float alpha) {
        batch.setColor(1,1,1,alpha);
        var motion=StoryGesture.sample(visualCue.action(),storyDirectedElapsed);
        float dx=guideActs()?motion.x()*.55f:0,dy=guideActs()?motion.y()*.55f:0,rotation=guideActs()?motion.rotation():0;
        batch.draw(pngPoses[pose.ordinal()],VehicleLayout.PNG_CAPYBARA_X+dx,
                VehicleLayout.PNG_CAPYBARA_Y+passengerBob+dy,VehicleLayout.PNG_CAPYBARA_W/2,0,
                VehicleLayout.PNG_CAPYBARA_W,VehicleLayout.PNG_CAPYBARA_H,1,1,rotation);
        batch.setColor(Color.WHITE);
    }
    void drawBusPassengers(SpriteBatch batch, float seconds, boolean sau, boolean ong) {
        if (sau && sauPassengerTexture != null) {
            float x = compositeBus != null ? VehicleLayout.PNG_SAU_X : VehicleLayout.SAU_X;
            int pose = (!ong || !"ONG".equals(storyFocus)) ? storyPose.ordinal() : 0;
            TextureRegion base = sauPassengers[pose];
            drawPassenger(batch, base, x, passengerBob);
            if (childOutfitsAtlas != null)
                drawPassengerOverlay(batch, sauPassengerOutfits[outfitRotation.sau(seconds)][pose], base, x, passengerBob);
        }
        if (ong && (ongCompleteAvailable || ongPassengerTexture != null)) {
            float x = compositeBus != null ? (sau ? VehicleLayout.PNG_ONG_X : VehicleLayout.PNG_SAU_X)
                    : (sau ? VehicleLayout.ONG_X : VehicleLayout.SAU_X);
            int pose = (!sau || !"SAU".equals(storyFocus)) ? storyPose.ordinal() : 0;
            TextureRegion completeOrOriginal = ongCompleteAvailable
                    ? ongCompletePassengers[outfitRotation.ong(seconds)][pose] : ongPassengers[pose];
            drawPassenger(batch, completeOrOriginal, x, passengerBob); // One complete sprite, no garment layer.
        }
    }
    private void drawPassengerOverlay(SpriteBatch batch, TextureRegion overlay, TextureRegion base, float x, float bounce) {
        float height = compositeBus != null ? VehicleLayout.PNG_CHILD_HEIGHT : VehicleLayout.CHILD_H;
        float slotWidth = compositeBus != null ? VehicleLayout.PNG_CHILD_WIDTH : VehicleLayout.CHILD_W;
        float y = compositeBus != null ? VehicleLayout.PNG_CHILD_Y : VehicleLayout.CHILD_Y;
        // Overlay is built against this exact alpha-trimmed pose, including its aspect ratio.
        float width = height * base.getRegionWidth() / base.getRegionHeight();
        batch.draw(overlay, x + (slotWidth - width) / 2f, y + bounce, width, height);
    }
    private void drawPassenger(SpriteBatch batch, TextureRegion pose, float x, float bounce) {
        float height = compositeBus != null ? VehicleLayout.PNG_CHILD_HEIGHT : VehicleLayout.CHILD_H;
        float slotWidth = compositeBus != null ? VehicleLayout.PNG_CHILD_WIDTH : VehicleLayout.CHILD_W;
        float y = compositeBus != null ? VehicleLayout.PNG_CHILD_Y : VehicleLayout.CHILD_Y;
        float width = height * pose.getRegionWidth() / pose.getRegionHeight();
        batch.draw(pose, x + (slotWidth - width) / 2f,y + bounce,width,height);
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
            sauStory[active && !guideActs() ? storyPose.ordinal() : StoryCompanionPose.WAVE.ordinal()];
        if (!active) batch.setColor(.82f, .82f, .82f, 1);
        float width=both?248f:285f, height=both?373f:427f;
        float breath=AnimationRegistry.Profile.SAU_IDLE.sample(elapsedSeconds,0);
        float childX = (both ? 1140f : 1310f) + directedX(active), childY = 315f + bounce + directedY(active);
        batch.draw(pose, childX, childY, width/2, 0,
                width, height, breath, breath, directedRotation(active));
        // Never substitute a plain costume for the expressive Story V2 pose.
        // Clothes are a second transparent layer at precisely the same transform.
        if (sauStoryTexture != null && childOutfitsAtlas != null)
            batch.draw(sauHeroOutfits[outfitRotation.sau(elapsedSeconds)][active && !guideActs() ? storyPose.ordinal() : 0],
                    childX, childY, width/2, 0, width, height, breath, breath, directedRotation(active));
        batch.setColor(Color.WHITE);
        if(active && !guideActs())drawSupportInteraction(batch,childX,childY,width,height);
        if (active) {
            // When BOTH are invited, share ONE illustration between the children.
            float propX = both && "BOTH".equals(storyFocus) ? 1380f :
                both ? 1220f : 1390f;
            drawStoryProp(batch, propX, elapsedSeconds, 3);
        }
    }
    void drawOngCompanion(SpriteBatch batch, float elapsedSeconds,
                          boolean enabled, String costume, boolean both) {
        if (!enabled || (!ongCompleteAvailable && ongCostumesTexture == null && ongStoryTexture == null)) return;
        float bounce = (float) Math.sin(elapsedSeconds * 1.9f + .6f) * 5f;
        boolean active = !both || !"SAU".equals(storyFocus);
        int poseIndex = active && !guideActs() ? storyPose.ordinal() : StoryCompanionPose.WAVE.ordinal();
        TextureRegion pose = ongCompleteAvailable ? ongCompleteOutfits[outfitRotation.ong(elapsedSeconds)][poseIndex]
                : ongStoryTexture == null ? ongCostumes[costumeIndex(costume)]
                : ongStory[active && !guideActs() ? storyPose.ordinal() : StoryCompanionPose.WAVE.ordinal()];
        if (!active) batch.setColor(.82f, .82f, .82f, 1);
        float width=both?224f:260f, height=both?336f:390f;
        float breath=AnimationRegistry.Profile.ONG_IDLE.sample(elapsedSeconds,0);
        float childX = (both ? 1555f : 1310f) + directedX(active), childY = 315f + bounce + directedY(active);
        batch.draw(pose, childX, childY, width/2, 0,
                width, height, breath, breath, directedRotation(active));
        // Complete Ong art already includes outfit/body/face. Never draw a dress overlay,
        // including the missing/partial optional pack fallback to original Ong artwork.
        batch.setColor(Color.WHITE);
        if(active && !guideActs() && !(both && "BOTH".equals(storyFocus)))drawSupportInteraction(batch,childX,childY,width,height);
        if (active && !(both && "BOTH".equals(storyFocus)))
            drawStoryProp(batch, both ? 1630f : 1390f, elapsedSeconds, 1);
    }
    /** Draw a local tree BEFORE the original children, keeping their faces unobscured.
     * The high flower is added afterwards at the canopy, matching LOOK_UP narration. */
    void drawStoryStage(SpriteBatch batch, String audience) {
        storyBoth = "BOTH".equals(audience);
        if(!storyDirected)return;
        var placement=StoryPropLayout.directed(visualCue.action(),visualCue.prop(),storyBoth);
        if(!placement.treeBehindFlower())return;
        float anchor=!storyBoth?1390f:"BOTH".equals(storyFocus)?1380f:
                "SAU".equals(storyFocus)?1220f:1630f;
        float flowerX=anchor+placement.xOffset();
        batch.draw(tree,flowerX-35,380,190,530);
    }

    private TextureRegion regionFor(VisualCue.Prop prop) {
        return switch(prop) {
            case BIRD -> extras[14];case BUTTERFLY -> extras[15];
            case CAT -> friendSprites[3];case RABBIT -> friendSprites[0];
            case FOX -> friendSprites[1];case PANDA -> friendSprites[2];case PENGUIN -> friendSprites[4];case DUCK -> extras[13];
            case CLOUD -> cloud;case FLOWER -> flower;case TREE -> tree;
            case BOAT -> extras[7];case VEHICLE -> trafficSprites[0];case KITE -> extras[17];
            default -> storySupport.prop(prop);
        };
    }
    /** Attach supporting objects to the acting hero/guide, below the original face. */
    private void drawSupportInteraction(SpriteBatch batch,float x,float y,float w,float h) {
        if(!storyDirected)return;
        var action=visualCue.action();
        if(!com.khuongnd.dexkids.story.adventure.StoryActionCatalog.attached(action)) {
            if(action==VisualCue.Action.JUMP)batch.draw(storySupport.shadow,x+w*.2f,y-directedY(true)-12,w*.6f,26);
            return;
        }
        var box=StoryInteractionLayout.primary(action);
        float t=StoryInteractionLayout.phase(storyDirectedElapsed);
        float offset=action==VisualCue.Action.SHARE?StoryInteractionLayout.offerOffset(t)*w:0;
        var region=regionFor(visualCue.prop());
        if(region!=null)batch.draw(region,x+box.x()*w+offset,y+box.y()*h,box.width()*w,box.height()*h);
        if(action==VisualCue.Action.READ) {
            // A moving page-edge highlight, not an invented arm animation.
            batch.setColor(1,1,1,.45f);batch.draw(storySupport.sparkle,x+w*(.40f+.06f*(float)Math.sin(t*2)),y+h*.43f,w*.12f,h*.07f);batch.setColor(Color.WHITE);
        } else if(action==VisualCue.Action.WATER) {
            batch.draw(storySupport.can,x+w*.56f,y+h*.25f,w*.235f,0,w*.47f,h*.25f,-1,1,-12);
            for(int i=0;i<3;i++) {
                float fall=(t*.8f+i/3f)%1;
                batch.draw(storySupport.drop,x+w*(.99f+fall*.06f),y+h*(.35f-fall*.17f),w*.055f,h*.045f);
            }
        } else if(action==VisualCue.Action.INSPECT) {
            batch.draw(storySupport.lens,x+w*(.58f+.03f*(float)Math.sin(t*2)),y+h*.28f,w*.36f,h*.23f);
        }
    }

    private void drawStoryProp(SpriteBatch batch, float x, float seconds, int count) {
        // Render a bounded, preloaded illustration picked from the safe VisualCue vocabulary.
        // Never interpret an AI string as an arbitrary file, image path or texture name.
        if(storyDirected && com.khuongnd.dexkids.story.adventure.StoryActionCatalog.attached(visualCue.action()))return;
        TextureRegion prop=regionFor(visualCue.prop());
        if(storyDirected) {
            var placement=StoryPropLayout.directed(visualCue.action(),visualCue.prop(),storyBoth);
            if(prop!=null && placement.visible()) {
                float px=x+placement.xOffset(),py=placement.y();
                if(!placement.treeBehindFlower()) { px+=storyMotionX;py+=storyMotionY; }
                if(visualCue.prop()==VisualCue.Prop.BOAT)
                    batch.draw(riverWater,px-12,py-2,150,28);
                batch.draw(prop,px,py,placement.width(),placement.height());
            }
            return;
        }
        if (prop != null) {
            float sky = switch (visualCue.prop()) {
                case BIRD, BUTTERFLY, CLOUD, KITE -> 740f;
                default -> 292f;
            };
            float bob = (float)Math.sin(seconds * 2.2f) * 5f;
            float w = switch (visualCue.prop()) {
                case CLOUD -> 142f;
                case CAT, RABBIT -> StoryPropLayout.PET_WIDTH;
                case FLOWER -> StoryPropLayout.FLOWER_WIDTH;
                default -> 105f;
            };
            float h = switch (visualCue.prop()) {
                case CLOUD -> 68f;
                case CAT, RABBIT -> StoryPropLayout.PET_HEIGHT;
                case FLOWER -> StoryPropLayout.FLOWER_HEIGHT;
                default -> 112f;
            };
            // A lesson's boat is a local illustration, not a GPS biome change.
            // Give it its own water surface even over a countryside backdrop.
            if (visualCue.prop() == VisualCue.Prop.BOAT)
                batch.draw(riverWater,x+48f+storyMotionX,sky+bob+storyMotionY-2f,150f,28f);
            batch.draw(prop,x+66f+(105f-w)/2f+storyMotionX,sky+bob+storyMotionY,
                    w*storyMotionScale,h*storyMotionScale);
        } else if (storyPose == StoryCompanionPose.PET) {
            batch.draw(friendSprites[storyFriend], x + 25, 290, StoryPropLayout.PET_WIDTH, StoryPropLayout.PET_HEIGHT);
        } else if (storyPose == StoryCompanionPose.LOOK_UP) {
            batch.draw(cloud, x - 22, 740, 155, 65);
        } else if (storyFlower) {
            batch.draw(flower, x + 40, 290, StoryPropLayout.FLOWER_WIDTH, StoryPropLayout.FLOWER_HEIGHT);
        } else if (storyPose == StoryCompanionPose.COUNT && authoredCountVisual) {
            // Three decorative flowers are used only in fixed authored counting beats.
            // AI-authored number questions never show a fabricated object count.
            for (int i = 0; i < count; i++)
                batch.draw(flower, x - 92 + i * 95,
                    718 + (float)Math.sin(seconds * 2 + i) * 3, StoryPropLayout.COUNT_WIDTH, StoryPropLayout.COUNT_HEIGHT);
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
        if (childOutfitsAtlas != null) childOutfitsAtlas.dispose();
        if (sauCostumesTexture != null) sauCostumesTexture.dispose();
        if (ongCostumesTexture != null) ongCostumesTexture.dispose();
        if (sauStoryTexture != null) sauStoryTexture.dispose();
        if (ongStoryTexture != null) ongStoryTexture.dispose();
        if (sauPassengerTexture != null) sauPassengerTexture.dispose();
        if (ongPassengerTexture != null) ongPassengerTexture.dispose();
        if (pngAtlas != null) pngAtlas.dispose();
        atlas.dispose();
        extrasAtlas.dispose();
        storySupport.dispose();
        m10Art.dispose();
    }
}
