package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;
import java.util.Objects;

/** POI sourced narration/backdrops outrank estimated offline OSM biome art. */
public final class DynamicSceneComposer {
    public PoiSceneDirector.Scene select(PoiSceneDirector.Scene poi,
                                         PoiSceneDirector.Scene geo) {
        Objects.requireNonNull(poi);
        Objects.requireNonNull(geo);
        if(poi.active())return poi;
        if(geo.active())return geo;
        return new PoiSceneDirector.Scene(Biome.GENERAL,0f,0L,"",false);
    }
}
