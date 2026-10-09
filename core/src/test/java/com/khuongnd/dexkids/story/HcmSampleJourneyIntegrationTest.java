package com.khuongnd.dexkids.story;

import com.khuongnd.dexkids.geo.OfflinePoiCatalog;
import com.khuongnd.dexkids.geo.OfflinePoiEngine;
import com.khuongnd.dexkids.journey.GpxReplayFeed;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Uses the exact versioned asset pack, not an invented in-test POI dataset. */
class HcmSampleJourneyIntegrationTest {
    @Test void fourSourcedPreviewPoisAreDiscoverableWithoutNetwork() throws Exception {
        Path root = Path.of("..","assets");
        OfflinePoiCatalog pois;
        OfflineNarrationCatalog stories;
        try(var stream=Files.newInputStream(root.resolve("poi/sample-hcm.tsv"))) {
            pois=OfflinePoiCatalog.parse(stream);
        }
        try(var stream=Files.newInputStream(root.resolve("narration/sample-hcm.tsv"))) {
            stories=OfflineNarrationCatalog.parse(stream);
        }
        assertEquals(4, pois.entries().size());
        assertEquals(4,stories.size());
        for (var entry: pois.entries())
            assertTrue(stories.findByPoiId(entry.poi().id()).isPresent());
        GpxReplayFeed feed;
        try(var stream=Files.newInputStream(Path.of("..","test-data/gps/synthetic-hcm-poi-loop.gpx"))) {
            feed=GpxReplayFeed.fromGpx(stream);
        }
        var engine=new OfflinePoiEngine(pois);
        Set<String> discovered=new HashSet<>();
        // Advance synthetic timeline. These are geometric proximity events,
        // never confirmed road navigation or vehicle detection.
        long base=1791510000000L;
        for(int i=0;i<12_000&&!feed.finished();i++) {
            feed.update(.1f);
            long now=base+(long)(feed.replayTimeSeconds()*1000);
            var fix=feed.position(now);
            if(fix.isPresent()) {
                engine.observe(fix.get(),now).ifPresent(notice -> {
                    assertTrue(notice.simulated());
                    assertTrue(notice.confidence()>=.55);
                    assertTrue(stories.findByPoiId(notice.entry().poi().id()).isPresent());
                    discovered.add(notice.entry().poi().id());
                });
            }
        }
        assertTrue(feed.finished());
        assertTrue(discovered.size()>=2, "Synthetic central-city drive should approach multiple sample POIs");
    }
}
