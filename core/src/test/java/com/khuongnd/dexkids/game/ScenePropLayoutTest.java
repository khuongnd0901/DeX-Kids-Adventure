package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScenePropLayoutTest {
    @Test void beachAndLakeUseWaterEvenWhenPoiTypeIsNatureOrPark() {
        for (String key : new String[]{"vungtau_beach", "nha_trang_beach", "dalat_lake"}) {
            assertEquals(Biome.RIVER, ScenePropLayout.biome(key, Biome.COUNTRYSIDE));
            assertFalse(SceneryCast.showFriend(1, ScenePropLayout.biome(key, Biome.PARK)));
            assertTrue(ScenePropLayout.waterY(key) >= 430);
        }
        assertEquals(Biome.PARK, ScenePropLayout.biome(null, Biome.PARK));
        assertEquals(Biome.URBAN, ScenePropLayout.biome("hcm_palace", Biome.URBAN));
        assertEquals(341f, ScenePropLayout.waterY(null));
    }
    @Test void activityObjectsRemainLargeAndBelowTheHud() {
        assertTrue(StoryPropLayout.PET_WIDTH >= 200);
        assertTrue(StoryPropLayout.FLOWER_WIDTH >= 160);
        assertTrue(StoryPropLayout.COUNT_WIDTH >= 70);
        assertTrue(290 + StoryPropLayout.PET_HEIGHT < 718);
        assertTrue(718 + StoryPropLayout.COUNT_HEIGHT < 918);
    }
}
