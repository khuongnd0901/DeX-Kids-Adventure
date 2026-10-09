package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.geo.OfflinePoiCatalog;
import com.khuongnd.dexkids.geo.OfflinePoiEngine;
import com.khuongnd.dexkids.world.Biome;
import java.util.Objects;

/**
 * Source-aware, purely illustrative POI-to-art transition. It is NOT road
 * matching, navigation, a geographic biome survey or proof of passing a POI.
 * All time is simulated render delta: opening the parent menu freezes fades.
 */
public final class PoiSceneDirector {
    public record Scene(Biome biome, float alpha, long fromChunk, String poiId, boolean simulated) {
        public Scene {
            Objects.requireNonNull(biome);
            Objects.requireNonNull(poiId);
            if (!Float.isFinite(alpha) || alpha < 0f || alpha > 1f)
                throw new IllegalArgumentException("Invalid fade");
        }
        public boolean active() { return alpha > 0f && !poiId.isBlank(); }
    }
    private record Request(Biome biome, String id, boolean simulated) {}

    private static final float FADE_SECONDS = 2.5f;
    private static final float HOLD_SECONDS = 26f;
    private static final double MAX_THEME_TRAVEL_METERS = 800.0;
    private Request current;
    private Request queued;
    private long startChunk;
    private float opacity;
    private float age;
    private boolean fadingOut;
    private double originDistance;
    private double lastDistance;

    public static Biome biomeFor(OfflinePoiCatalog.Type type) {
        return switch (Objects.requireNonNull(type)) {
            case PARK -> Biome.PARK;
            case NATURE -> Biome.COUNTRYSIDE;
            case RIVER -> Biome.RIVER;
            case BRIDGE -> Biome.BRIDGE;
            case LANDMARK, MUSEUM -> Biome.URBAN;
        };
    }

    public boolean onNotice(OfflinePoiEngine.Notice notice, double distanceMeters) {
        if (notice == null || !Double.isFinite(distanceMeters) || distanceMeters < 0
                || notice.event() == OfflinePoiEngine.EventKind.PASSING_CANDIDATE
                || notice.confidence() < 0.55 || notice.meters() > 200) return false;
        Request request = new Request(biomeFor(notice.entry().type()),
                notice.entry().poi().id(), notice.simulated());
        if (current == null) {
            begin(request, distanceMeters);
            return true;
        }
        if (current.id().equals(request.id())) return false;
        // Change art only AFTER old layer fades to zero, never instant-swap.
        if (queued == null || !queued.id().equals(request.id())) {
            queued = request;
            fadingOut = true;
        }
        return true;
    }

    public void update(float renderDelta, double distanceMeters) {
        if (!Float.isFinite(renderDelta) || renderDelta < 0 ||
                !Double.isFinite(distanceMeters) || distanceMeters < 0)
            throw new IllegalArgumentException("Invalid scene movement");
        // GPX restart/seek must not leave a stale named scene on a new journey.
        if (distanceMeters + 3 < lastDistance) {
            reset();
            lastDistance = distanceMeters;
            return;
        }
        lastDistance = distanceMeters;
        if (current == null || renderDelta == 0) return;
        age += renderDelta;
        if (age >= HOLD_SECONDS || distanceMeters - originDistance >= MAX_THEME_TRAVEL_METERS)
            fadingOut = true;
        if (fadingOut) opacity = Math.max(0f, opacity - renderDelta / FADE_SECONDS);
        else opacity = Math.min(1f, opacity + renderDelta / FADE_SECONDS);
        if (fadingOut && opacity <= 0f) {
            if (queued != null) {
                Request next = queued;
                queued = null;
                begin(next, distanceMeters);
            } else {
                current = null;
                fadingOut = false;
            }
        }
    }

    private void begin(Request request, double distanceMeters) {
        current = request;
        startChunk = SceneryLayout.firstChunk(distanceMeters) + 2;
        originDistance = lastDistance = distanceMeters;
        age = opacity = 0f;
        fadingOut = false;
    }

    public Scene scene() {
        if (current == null) return new Scene(Biome.GENERAL, 0f, 0, "", false);
        return new Scene(current.biome(), opacity, startChunk, current.id(), current.simulated());
    }
    public void reset() {
        current = queued = null;
        opacity = age = 0f;
        lastDistance = originDistance = 0;
        fadingOut = false;
        startChunk = 0;
    }
}
