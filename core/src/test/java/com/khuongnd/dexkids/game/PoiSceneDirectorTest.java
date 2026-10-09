package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.geo.OfflinePoiCatalog;
import com.khuongnd.dexkids.geo.OfflinePoiEngine;
import com.khuongnd.dexkids.geo.Poi;
import com.khuongnd.dexkids.world.Biome;
import java.net.URI;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoiSceneDirectorTest {
    private static OfflinePoiEngine.Notice notice(OfflinePoiCatalog.Type type,
                                                  OfflinePoiEngine.EventKind kind,
                                                  String id, boolean simulated) {
        var poi=new Poi(id,"Test site",10,106,URI.create("https://www.openstreetmap.org/node/123"),
                Instant.parse("2026-10-09T00:00:00Z"));
        return new OfflinePoiEngine.Notice(new OfflinePoiCatalog.Entry(poi,type,"qa-fixture"),
                kind,48,0.91,simulated);
    }
    @Test void categoryMappingsAreIllustrativeNotRoadTruth() {
        assertEquals(Biome.PARK,PoiSceneDirector.biomeFor(OfflinePoiCatalog.Type.PARK));
        assertEquals(Biome.RIVER,PoiSceneDirector.biomeFor(OfflinePoiCatalog.Type.RIVER));
        assertEquals(Biome.BRIDGE,PoiSceneDirector.biomeFor(OfflinePoiCatalog.Type.BRIDGE));
        assertEquals(Biome.COUNTRYSIDE,PoiSceneDirector.biomeFor(OfflinePoiCatalog.Type.NATURE));
        assertEquals(Biome.URBAN,PoiSceneDirector.biomeFor(OfflinePoiCatalog.Type.MUSEUM));
        assertEquals(Biome.URBAN,PoiSceneDirector.biomeFor(OfflinePoiCatalog.Type.LANDMARK));
    }
    @Test void transitionIsSoftDistanceAnchoredAndFreezesWithMenu() {
        var d=new PoiSceneDirector();
        assertFalse(d.scene().active());
        var a=notice(OfflinePoiCatalog.Type.PARK,OfflinePoiEngine.EventKind.NEARBY,"osm:node:1",true);
        assertTrue(d.onNotice(a,60));
        assertFalse(d.scene().active()); // no instantaneous visible flash
        assertEquals(SceneryLayout.firstChunk(60)+2,d.scene().fromChunk());
        d.update(1.25f,60);
        assertEquals(0.5f,d.scene().alpha(),.0001f);
        d.update(0,60);
        assertEquals(0.5f,d.scene().alpha(),.0001f);
        d.update(1.25f,60);
        assertEquals(1f,d.scene().alpha(),.0001f);
        assertEquals(Biome.PARK,d.scene().biome());
        assertTrue(d.scene().simulated());
        assertFalse(d.onNotice(a,60)); // same ID never restarts duration
        assertEquals(1f,d.scene().alpha());
    }
    @Test void duplicatePassingWeakOrDistantFixNeverChangesScene() {
        var d=new PoiSceneDirector();
        var bridge=notice(OfflinePoiCatalog.Type.BRIDGE,
                OfflinePoiEngine.EventKind.PASSING_CANDIDATE,"osm:node:3",true);
        assertFalse(d.onNotice(bridge,10));
        assertFalse(d.scene().active());
        var park=notice(OfflinePoiCatalog.Type.PARK,
                OfflinePoiEngine.EventKind.NEARBY,"osm:node:4",true);
        assertFalse(d.onNotice(new OfflinePoiEngine.Notice(park.entry(),park.event(),240,.91,true),10));
        assertFalse(d.onNotice(new OfflinePoiEngine.Notice(park.entry(),park.event(),40,.5,true),10));
        assertFalse(d.scene().active());
    }
    @Test void queuedBiomeSwitchWaitsForFadeOutAndDoesNotFlash() {
        var d=new PoiSceneDirector();
        d.onNotice(notice(OfflinePoiCatalog.Type.PARK,OfflinePoiEngine.EventKind.NEARBY,
                "osm:node:2",true),0);
        d.update(2.5f,0);
        var first=d.scene();
        assertEquals(Biome.PARK,first.biome());
        d.onNotice(notice(OfflinePoiCatalog.Type.RIVER,OfflinePoiEngine.EventKind.APPROACHING,
                "osm:node:3",false),150);
        d.update(1.25f,150);
        assertEquals(Biome.PARK,d.scene().biome());
        assertEquals(.5f,d.scene().alpha(),.0001f);
        d.update(1.25f,150);
        assertEquals(Biome.RIVER,d.scene().biome());
        assertFalse(d.scene().active());
        assertEquals(SceneryLayout.firstChunk(150)+2,d.scene().fromChunk());
        d.update(2.5f,150);
        assertEquals(1f,d.scene().alpha(),.0001f);
        assertFalse(d.scene().simulated());
    }
    @Test void journeyResetOrExceededRangeFadesSafely() {
        var d=new PoiSceneDirector();
        d.onNotice(notice(OfflinePoiCatalog.Type.MUSEUM,OfflinePoiEngine.EventKind.NEARBY,
                "osm:node:5",true),500);
        d.update(2.5f,500);
        d.update(.5f,1400); // >800m from anchor
        assertTrue(d.scene().alpha()<1f);
        d.update(3f,1400);
        assertFalse(d.scene().active());
        d.onNotice(notice(OfflinePoiCatalog.Type.NATURE,OfflinePoiEngine.EventKind.NEARBY,
                "osm:node:6",true),1400);
        d.update(1f,1400);
        d.update(.1f,0); // GPX reset
        assertFalse(d.scene().active());
        assertThrows(IllegalArgumentException.class,()->d.update(Float.NaN,0));
    }
}
