package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.geo.OfflinePoiCatalog;
import com.khuongnd.dexkids.geo.OfflinePoiEngine;
import com.khuongnd.dexkids.journey.GpxReplayFeed;
import com.khuongnd.dexkids.world.Biome;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Actual bundled OSM-derived PREVIEW asset pack + synthetic GPX exercise M4->M6. */
class HcmSceneJourneyIntegrationTest {
    private record Event(String id, Biome theme, boolean simulated) {}

    private List<Event> replay() throws Exception {
        OfflinePoiCatalog data;
        try (var src = Files.newInputStream(Path.of("..","assets/poi/sample-hcm.tsv"))) {
            data = OfflinePoiCatalog.parse(src);
        }
        GpxReplayFeed feed;
        try (var src = Files.newInputStream(Path.of("..","test-data/gps/synthetic-hcm-poi-loop.gpx"))) {
            feed = GpxReplayFeed.fromGpx(src);
        }
        var poiEngine = new OfflinePoiEngine(data);
        var scenes = new PoiSceneDirector();
        var events = new ArrayList<Event>();
        boolean becameVisible = false;
        long baseMillis = 1791510000000L;
        for (int i=0; i<12_000 && !feed.finished(); i++) {
            feed.update(.1f);
            scenes.update(.1f,feed.distanceMeters());
            var where=feed.position(baseMillis+i*100L);
            if (where.isPresent()) {
                var event=poiEngine.observe(where.get(),baseMillis+i*100L);
                if (event.isPresent() && scenes.onNotice(event.orElseThrow(),feed.distanceMeters())) {
                    var scene=scenes.scene();
                    assertTrue(scene.simulated(), "GPX is not a real recorded trip");
                    assertEquals(SceneryLayout.firstChunk(feed.distanceMeters())+2,scene.fromChunk(),
                        "No snapping an already visible chunk");
                    events.add(new Event(event.orElseThrow().entry().poi().id(),
                            PoiSceneDirector.biomeFor(event.orElseThrow().entry().type()),true));
                }
            }
            if (scenes.scene().active() && scenes.scene().alpha()>0.01) becameVisible=true;
        }
        assertTrue(feed.finished());
        assertTrue(becameVisible, "A source-backed event must produce visible fade-in");
        assertFalse(events.isEmpty(), "HCMC test replay must hit a source-backed POI");
        feed.reset();
        scenes.update(.1f, feed.distanceMeters());
        assertFalse(scenes.scene().active(), "Restart clears old POI art theme");
        return events;
    }

    @Test void hcmPreviewProducesReproducibleSceneEventOrderAndNoRealPlaceClaim() throws Exception {
        var actual=replay();
        assertEquals(actual,replay(),"Identical synthetic GPS/POI replay must yield identical scene requests");
        assertTrue(actual.stream().allMatch(Event::simulated));
    }
}
