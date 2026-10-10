package com.khuongnd.dexkids.geo;

import com.khuongnd.dexkids.story.OfflineNarrationCatalog;
import com.khuongnd.dexkids.story.PoiDialogueCatalog;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LiveGpsPoiConversationTest {
    private static Path assets(String path) { return Path.of("..", "assets", path); }

    @Test void fortySourceCheckedLivePoisLinkOneToOneWithStoriesAndQuestions() throws Exception {
        OfflinePoiCatalog locations;
        OfflineNarrationCatalog narration;
        PoiDialogueCatalog conversation;
        try (var in = Files.newInputStream(assets("poi/live-landmarks.tsv"))) {
            locations = OfflinePoiCatalog.parse(in);
        }
        try (var in = Files.newInputStream(assets("narration/live-landmarks.tsv"))) {
            narration = OfflineNarrationCatalog.parse(in);
        }
        try (var in = Files.newInputStream(assets("poi/live-dialogue.tsv"))) {
            conversation = PoiDialogueCatalog.parse(in);
        }
        assertEquals(40, locations.entries().size());
        assertEquals(40, narration.size());
        assertEquals(40, conversation.size());
        for (var e : locations.entries()) {
            assertTrue(e.reviewer().startsWith("source-audit-"));
            assertTrue(narration.findByPoiId(e.poi().id()).isPresent());
            var q = conversation.find(e.poi().id()).orElseThrow();
            assertTrue(q.quiz().contains("?") || q.quiz().contains("không") || q.quiz().contains("nào"));
            assertEquals("www.openstreetmap.org", e.poi().source().getHost());
        }
    }

    @Test void stableRealGpsTwoSamplesRequiredBeforeAnyPlaceDetection() throws Exception {
        OfflinePoiCatalog catalogue;
        try (var in = Files.newInputStream(assets("poi/live-landmarks.tsv"))) {
            catalogue = OfflinePoiCatalog.parse(in);
        }
        var filter = new LiveFixGate();
        var engine = new OfflinePoiEngine(catalogue);
        long now=1_000_000L;
        // Đá Ba Chồng OSM node on QL20. These are synthetic test fixes at
        // a real sourced point, NOT proof of actual driving-road alignment.
        var a = new JourneyPosition(11.19082,107.3484,5,3,now,false);
        var b = new JourneyPosition(11.19079,107.3484,5,3,now+2000,false);
        assertFalse(filter.accept(a));
        assertTrue(engine.observe(a,now).isPresent()); // Engine alone can respond on one fix.
        engine.reset();
        assertTrue(filter.accept(b));
        var notice = engine.observe(b,now+2000).orElseThrow();
        assertEquals("osm:node:9974267816",notice.entry().poi().id());
        assertFalse(notice.simulated());
        assertTrue(notice.meters()<50);
        assertEquals(OfflinePoiEngine.EventKind.NEARBY,notice.event());
        assertTrue(engine.observe(b,now+4000).isEmpty(), "One GPS timestamp cannot spam");
    }

    @Test void rejectsUnstableLowQualityOrSimulatedCoordinates() {
        var gate = new LiveFixGate();
        assertFalse(gate.accept(new JourneyPosition(11.19079,107.3484,40,4,1000,false)));
        assertFalse(gate.accept(new JourneyPosition(11.19079,107.3484,5,4,2000,true)));
        assertFalse(gate.accept(new JourneyPosition(11.19079,107.3484,5,4,3000,false)));
        assertFalse(gate.accept(new JourneyPosition(11.191,107.3484,5,4,3000,false)));
        assertFalse(gate.accept(new JourneyPosition(11.210,107.350,5,4,4000,false)),
                "GPS teleport must not become place evidence");
        gate.reset();
        assertFalse(gate.accept(new JourneyPosition(11.19079,107.3484,5,2,5000,false)));
        assertTrue(gate.accept(new JourneyPosition(11.19080,107.3484,5,2,7000,false)));
        assertFalse(gate.accept(new JourneyPosition(11.19080,107.3484,5,2,23000,false)),
                "Stale GPS sequence never qualifies");
    }
}
