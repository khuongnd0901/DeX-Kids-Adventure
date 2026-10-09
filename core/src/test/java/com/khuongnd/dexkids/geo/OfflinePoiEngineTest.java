package com.khuongnd.dexkids.geo;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OfflinePoiEngineTest {
    static OfflinePoiCatalog catalogue() {
        String text = "# dexkids-poi-v1\n" + OfflinePoiCatalog.HEADER + "\n"
                + "osm:node:12345\tSynthetic Park - test fixture\t10\t106\tPARK\t"
                + "https://www.openstreetmap.org/node/12345\t2026-10-08T00:00:00Z\tqa-fixture\n";
        return OfflinePoiCatalog.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }
    static JourneyPosition fix(double lat, float accuracy, float speed, long id) {
        return new JourneyPosition(lat,106,accuracy,speed,id,true);
    }
    @Test void rejectsUnreviewedUnknownAndMalformedInputs() {
        assertTrue(OfflinePoiCatalog.empty().entries().isEmpty());
        String header = "# dexkids-poi-v1\n"+ OfflinePoiCatalog.HEADER+"\n";
        assertTrue(OfflinePoiCatalog.parse(new ByteArrayInputStream(header.getBytes(StandardCharsets.UTF_8))).entries().isEmpty());
        String rows = "# dexkids-poi-v1\n" + OfflinePoiCatalog.HEADER + "\n"
                + "osm:node:12345\tExample\t10\t106\tPARK\t"
                + "https://example.com/node/12345\t2026-10-08T00:00:00Z\tqa\n";
        assertThrows(IllegalArgumentException.class, () -> OfflinePoiCatalog.parse(
                new ByteArrayInputStream(rows.getBytes(StandardCharsets.UTF_8))));
        rows = rows.replace("https://example.com/node/12345", "https://www.openstreetmap.org/node/12345")
                .replace("\tqa\n", "\t\n");
        String missingReviewer = rows;
        assertThrows(IllegalArgumentException.class, () -> OfflinePoiCatalog.parse(
                new ByteArrayInputStream(missingReviewer.getBytes(StandardCharsets.UTF_8))));
    }
    @Test void repeatedFixNeverSpamsAndHighUncertaintyCannotTrigger() {
        var engine = new OfflinePoiEngine(catalogue());
        assertTrue(engine.observe(fix(10.0003f, 50, 4, 1), 1_000).isEmpty());
        assertEquals(OfflinePoiEngine.EventKind.NEARBY,
                engine.observe(fix(10.0003f, 5, 0, 2), 2_000).orElseThrow().event());
        for(int i=3; i<60; i++)
            assertTrue(engine.observe(fix(10.0003f, 5, 0, i), i*1_000L).isEmpty());
    }
    @Test void geometricApproachingAndPassCandidateRequireMultipleAcceptedLocations() {
        var engine = new OfflinePoiEngine(catalogue());
        // 0.0011° ~= 122m; first fix NEAR then decreasing distance.
        assertEquals(OfflinePoiEngine.EventKind.NEARBY,
                engine.observe(fix(10.0011, 5, 6, 1), 40_000).orElseThrow().event());
        // Cooldown blocks fresh event, not internal detection state.
        assertTrue(engine.observe(fix(10.00025, 5, 6, 2), 41_000).isEmpty());
        engine.observe(fix(10.00010, 5, 6, 3), 42_000);
        var later=engine.observe(fix(9.9994, 5, 6, 4), 80_000);
        assertTrue(later.isPresent());
        assertEquals(OfflinePoiEngine.EventKind.PASSING_CANDIDATE,later.orElseThrow().event());
        assertTrue(later.orElseThrow().simulated());
        // Never repeats "passed", including repeated frame draws.
        assertTrue(engine.observe(fix(9.9994, 5, 6, 4), 130_000).isEmpty());
    }
    @Test void canResetAcrossIndependentJourneys() {
        var engine = new OfflinePoiEngine(catalogue());
        assertTrue(engine.observe(fix(10.0003,5,1,3),10_000).isPresent());
        assertTrue(engine.observe(fix(10.0003,5,1,3),100_000).isEmpty());
        engine.reset();
        assertTrue(engine.observe(fix(10.0003,5,1,3),110_000).isPresent());
    }
    @Test void noCatalogNeverFabricatesAPlace() {
        var engine = new OfflinePoiEngine(OfflinePoiCatalog.empty());
        assertTrue(engine.observe(fix(10,5,4,1),1_000).isEmpty());
    }
}
