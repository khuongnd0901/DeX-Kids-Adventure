package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DynamicSceneComposerTest {
    @Test void prioritizesSourcedPoiThenCoarseGeographyThenNeutral() {
        var c=new DynamicSceneComposer();
        var none=new PoiSceneDirector.Scene(Biome.GENERAL,0,0,"",false);
        var geo=new PoiSceneDirector.Scene(Biome.PARK,.75f,10,"geo:illustrative",false);
        var poi=new PoiSceneDirector.Scene(Biome.RIVER,.6f,12,"osm:way:123",false);
        assertEquals(poi,c.select(poi,geo));
        assertEquals(geo,c.select(none,geo));
        assertFalse(c.select(none,none).active());
    }
}
