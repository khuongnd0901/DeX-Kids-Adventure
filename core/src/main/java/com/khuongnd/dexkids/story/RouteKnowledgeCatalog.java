package com.khuongnd.dexkids.story;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Offline, source-attributed educational introductions selected by a parent.
 * No coordinates, route guidance, nearby assertions or road-matching in this pack.
 */
public final class RouteKnowledgeCatalog {
    public static final String HEADER = "id\troute_id\ttopic\ttitle\ttext_vi\tfact_url\tmin_age\tmax_age";
    public static final int MAX_BYTES = 128 * 1024;
    public record Route(String id, String title) {}
    public static final List<Route> ROUTES = List.of(
            new Route("dong-nai-vung-tau", "Đồng Nai → Vũng Tàu"),
            new Route("dong-nai-phan-thiet", "Đồng Nai → Phan Thiết"),
            new Route("dong-nai-bao-loc", "Đồng Nai → Bảo Lộc"),
            new Route("dong-nai-da-lat", "Đồng Nai → Đà Lạt"),
            new Route("dong-nai-nha-trang", "Đồng Nai → Nha Trang"));

    public record Card(String id, String routeId, String topic, String title,
                       String textVi, URI source, int minAge, int maxAge) {
        public Card {
            if (id == null || !id.matches("[a-z0-9-]{4,70}") ||
                    !isSupportedRoute(routeId) ||
                    topic == null || !topic.matches("(dia-ly|thien-nhien|van-hoa|khoa-hoc|moi-truong)") ||
                    !printable(title, 2, 75) || !printable(textVi, 8, 220) ||
                    source == null || !"https".equals(source.getScheme()) ||
                    source.getHost() == null || source.getUserInfo() != null ||
                    minAge < 2 || maxAge > 6 || minAge > maxAge)
                throw new IllegalArgumentException("Invalid offline route knowledge card");
            String lower = textVi.toLowerCase(java.util.Locale.ROOT);
            if (lower.contains("đang đi qua") || lower.contains("vừa đi qua") ||
                    lower.contains("chúng ta đang ở") || lower.contains("phía trước xe"))
                throw new IllegalArgumentException("Unverified location announcement");
        }
        private static boolean printable(String value, int min, int max) {
            return value != null && value.length() >= min && value.length() <= max &&
                    value.codePoints().noneMatch(ch -> ch < 32 || ch == 127);
        }
    }

    private final List<Card> cards;
    private RouteKnowledgeCatalog(List<Card> cards) { this.cards = List.copyOf(cards); }
    public int size() { return cards.size(); }

    public static boolean isSupportedRoute(String routeId) {
        return ROUTES.stream().anyMatch(route -> route.id().equals(routeId));
    }

    public static String routeTitle(String routeId) {
        return ROUTES.stream().filter(route -> route.id().equals(routeId))
                .map(Route::title).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown route"));
    }

    public List<Card> cardsFor(String routeId, int age) {
        if (!isSupportedRoute(routeId) || age < 2 || age > 6) return List.of();
        return cards.stream().filter(card -> card.routeId().equals(routeId)
                && age >= card.minAge() && age <= card.maxAge()).toList();
    }

    public static RouteKnowledgeCatalog parse(InputStream input) {
        Objects.requireNonNull(input);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = input.read(buffer)) != -1) {
                if (bytes.size() + n > MAX_BYTES)
                    throw new IllegalArgumentException("Route knowledge asset too large");
                bytes.write(buffer, 0, n);
            }
            String content = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            if (content.indexOf('\uFFFD') >= 0 || content.indexOf('\0') >= 0)
                throw new IllegalArgumentException("Invalid route asset UTF-8");
            String[] lines = content.split("\\R", -1);
            if (lines.length < 2 || !"# dexkids-route-knowledge-v1".equals(lines[0])
                    || !HEADER.equals(lines[1]))
                throw new IllegalArgumentException("Unsupported route knowledge asset");
            List<Card> parsed = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (int row = 2; row < lines.length; row++) {
                if (lines[row].isBlank()) continue;
                if (parsed.size() >= 200) throw new IllegalArgumentException("Too many cards");
                String[] cols = lines[row].split("\\t", -1);
                if (cols.length != 8) throw new IllegalArgumentException("Invalid card row " + (row + 1));
                Card card = new Card(cols[0], cols[1], cols[2], cols[3],
                        cols[4], URI.create(cols[5]), Integer.parseInt(cols[6]),
                        Integer.parseInt(cols[7]));
                if (!ids.add(card.id())) throw new IllegalArgumentException("Duplicate card ID");
                parsed.add(card);
            }
            return new RouteKnowledgeCatalog(parsed);
        } catch (IOException | RuntimeException ex) {
            throw new IllegalArgumentException("Cannot load offline route knowledge", ex);
        }
    }
}
