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
    @Test void atlasSilhouettesFitWithoutDistortionOrEnvelopeOverflow() {
        for(int[] dimensions:new int[][]{{160,160},{80,150},{155,90},{180,160}}){
            float width=ScenePropLayout.fitWidth(170,158,dimensions[0],dimensions[1]);
            float height=width*dimensions[1]/dimensions[0];
            assertTrue(width<=170 && height<=158.001f);
            assertEquals((float)dimensions[0]/dimensions[1],width/height,.00001f);
        }
        assertEquals(158,ScenePropLayout.fitWidth(170,158,160,160));
        assertThrows(IllegalArgumentException.class,()->ScenePropLayout.fitWidth(170,158,0,160));
    }
}
