package com.khuongnd.dexkids.story;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RouteKnowledgeCatalogTest {
    private static RouteKnowledgeCatalog load(String text) {
        return RouteKnowledgeCatalog.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }
    @Test void fiveRoutesContainDiverseSourceAttributedCardsInOriginalOrder() throws Exception {
        try (InputStream input = Files.newInputStream(Path.of("..", "assets", "routes", "knowledge.tsv"))) {
            var catalog = RouteKnowledgeCatalog.parse(input);
            assertEquals(35, catalog.size());
            Set<String> all = new HashSet<>();
            Set<String> topics = new HashSet<>();
            for (var route : RouteKnowledgeCatalog.ROUTES) {
                assertEquals(route.title(), RouteKnowledgeCatalog.routeTitle(route.id()));
                var cards = catalog.cardsFor(route.id(), 4);
                assertEquals(7, cards.size(), "Every route needs 7 age-appropriate topics: " + route.id());
                assertEquals(7, catalog.cardsFor(route.id(), 2).size());
                for (var card : cards) {
                    assertTrue(all.add(card.id()));
                    assertTrue(card.source().getScheme().equals("https"));
                    assertTrue(card.source().getHost() != null);
                    assertFalse(card.textVi().contains("đang đi qua"));
                    assertTrue(card.textVi().length() <= 220);
                    topics.add(card.topic());
                }
            }
            assertTrue(topics.containsAll(Set.of("dia-ly","thien-nhien","van-hoa","khoa-hoc","moi-truong")));
            assertTrue(catalog.cardsFor("unknown", 4).isEmpty());
            assertTrue(catalog.cardsFor(RouteKnowledgeCatalog.ROUTES.get(0).id(), 7).isEmpty());
        }
    }
    @Test void noLocationClaimsOrUnattributedFactsAreAccepted() {
        String first = "# dexkids-route-knowledge-v1\n" + RouteKnowledgeCatalog.HEADER + "\n";
        String card = "demo-one\tdong-nai-vung-tau\tthien-nhien\tBãi Sau\tBãi Sau là nơi có sóng biển.\thttps://vietnamtourism.vn/index.php/tourism/items/2119/4\t2\t6\n";
        assertEquals(1, load(first + card).size());
        assertThrows(IllegalArgumentException.class, () -> load(first + card + card));
        assertThrows(IllegalArgumentException.class, () -> load(first + card.replace("https://","http://")));
        assertThrows(IllegalArgumentException.class, () -> load(first + card.replace("đong-nai","unknown").replace("dong-nai-vung-tau","unlisted")));
        assertThrows(IllegalArgumentException.class, () -> load(first + card.replace("Bãi Sau là nơi có sóng biển.", "Chúng ta đang đi qua Bãi Sau.")));
        assertThrows(IllegalArgumentException.class, () -> load(first + card.replace("Bãi Sau là nơi có sóng biển.", "Đây là\t đoạn văn hỏng.")));
        assertThrows(IllegalArgumentException.class, () -> load(first + card.replace("2\t6", "0\t10")));
    }
    @Test void oversizedOrMalformedPacksFailClosed() {
        assertThrows(IllegalArgumentException.class, () -> load(""));
        String oversize = "# dexkids-route-knowledge-v1\n" + RouteKnowledgeCatalog.HEADER + "\n" + "a".repeat(RouteKnowledgeCatalog.MAX_BYTES + 10);
        assertThrows(IllegalArgumentException.class, () -> load(oversize));
    }
}
