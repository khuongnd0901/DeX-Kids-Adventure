package com.khuongnd.dexkids.story;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OfflineNarrationCatalogTest {
    private OfflineNarrationCatalog load(String body) {
        return OfflineNarrationCatalog.parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }
    @Test void approvedOrPreviewTextMustHavePoiAndSource() {
        String a="# dexkids-narration-v1\n"+OfflineNarrationCatalog.HEADER+"\n"+
            "one\tosm:way:39598493\tĐây là Dinh Độc Lập.\thttps://dinhdoclap.gov.vn/\t2026-10-09T08:30:00Z\t2\t6\n";
        var catalog=load(a);
        assertEquals(1,catalog.size());
        assertEquals("Đây là Dinh Độc Lập.",catalog.findByPoiId("osm:way:39598493").orElseThrow().textVi());
        assertTrue(catalog.findByPoiId("osm:way:1234").isEmpty());
        assertThrows(IllegalArgumentException.class,()->load(a+a.substring(a.indexOf("one\t"))));
        assertThrows(IllegalArgumentException.class,()->load(a.replace("https://dinhdoclap.gov.vn/","http://bad.example/")));
    }
    @Test void malformedOrTooLongContentFailsClosed() {
        assertThrows(IllegalArgumentException.class, ()->load("wrong"));
        String tooLong="x".repeat(242);
        String bad="# dexkids-narration-v1\n"+OfflineNarrationCatalog.HEADER+"\n"+
            "one\tosm:way:39598493\t"+tooLong+"\thttps://dinhdoclap.gov.vn/\t2026-10-09T08:30:00Z\t2\t6\n";
        assertThrows(IllegalArgumentException.class, ()->load(bad));
    }
}
