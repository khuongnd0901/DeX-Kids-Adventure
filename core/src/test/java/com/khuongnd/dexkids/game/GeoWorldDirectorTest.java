package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.geo.*;
import com.khuongnd.dexkids.world.Biome;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class GeoWorldDirectorTest {
    @Test void requiresThreeReliableFixesAndExpires() throws Exception {
        int a=GeoWorldIndex.cell(10.95),b=GeoWorldIndex.cell(106.82);
        var index=GeoWorldIndex.parse(new ByteArrayInputStream(("# dexkids-geo-cells-v1\n"+
            a+"\t"+b+"\tPARK\tosm:way:123\n").getBytes(StandardCharsets.UTF_8)));
        var world=new GeoWorldDirector(index);
        for(int i=1;i<=2;i++) {
            world.observe(new JourneyPosition(10.95,106.82,8f,2f,i,true),1000+i*1000);
            assertNull(world.biome());
        }
        world.observe(new JourneyPosition(10.95,106.82,8f,2f,3,true),4000);
        assertEquals(Biome.PARK,world.biome());
        world.update(1f,0,4500);
        assertTrue(world.scene(0,true).active());
        world.observe(new JourneyPosition(11.0,107.0,70f,2f,4,true),5000);
        assertEquals(Biome.PARK,world.biome());
        for(int i=0;i<30;i++)world.update(.1f,50,30000+i*100);
        assertNull(world.biome());
        assertFalse(world.scene(50,true).active());
    }
}
