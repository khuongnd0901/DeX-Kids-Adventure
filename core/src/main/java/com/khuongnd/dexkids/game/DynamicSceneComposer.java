package com.khuongnd.dexkids.game;

import java.util.Objects;

/** POI sourced narration/backdrops outrank estimated offline OSM biome art. */
public final class DynamicSceneComposer {
    public PoiSceneDirector.Scene select(PoiSceneDirector.Scene poi,
                                         PoiSceneDirector.Scene geo) {
        Objects.requireNonNull(poi);
        Objects.requireNonNull(geo);
        if(poi.active())return poi;
        if(geo.active())return geo;
        return PoiSceneDirector.Scene.inactive(false);
    }
}
