package com.khuongnd.dexkids.story;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Sourced offline, age-neutral question cards bound to specific OSM features. */
public final class PoiDialogueCatalog {
    public record Dialogue(String poiId, String quiz, String answer, String chat) {
        public Dialogue {
            if (poiId == null || !poiId.matches("osm:(node|way|relation):[1-9][0-9]{0,15}") ||
                    !valid(quiz) || !valid(answer) || !valid(chat))
                throw new IllegalArgumentException("Unsafe dialogue");
        }
        private static boolean valid(String s) {
            return s != null && s.length() >= 8 && s.length() <= 190 &&
                    s.codePoints().noneMatch(c -> c < 32 || c == 127);
        }
    }
    private final Map<String,Dialogue> dialogues;
    private PoiDialogueCatalog(Map<String,Dialogue> items) { dialogues = Map.copyOf(items); }
    public static PoiDialogueCatalog empty() { return new PoiDialogueCatalog(Map.of()); }
    public int size() { return dialogues.size(); }
    public Optional<Dialogue> find(String poiId) { return Optional.ofNullable(dialogues.get(poiId)); }
    public static PoiDialogueCatalog parse(InputStream in) {
        Objects.requireNonNull(in);
        try {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            byte[] block = new byte[4096];
            for (int n; (n = in.read(block)) != -1;) {
                if (n + data.size() > 64 * 1024)
                    throw new IllegalArgumentException("Oversized POI conversation data");
                data.write(block,0,n);
            }
            String text = new String(data.toByteArray(),StandardCharsets.UTF_8);
            if (text.indexOf('\uFFFD') >= 0 || text.indexOf('\0') >= 0)
                throw new IllegalArgumentException("Invalid UTF-8");
            String[] lines = text.split("\\R", -1);
            if (lines.length < 2 || !lines[0].equals("# dexkids-poi-dialogue-v1") ||
                    !lines[1].equals("poi_id\tquiz_vi\tanswer_vi\tchat_vi"))
                throw new IllegalArgumentException("Wrong dialogue schema");
            Map<String,Dialogue> cards = new HashMap<>();
            for (int i = 2; i < lines.length; i++) {
                if (lines[i].isEmpty()) continue;
                if (cards.size() >= 200) throw new IllegalArgumentException("Too many dialogues");
                String[] cols = lines[i].split("\\t", -1);
                if (cols.length != 4) throw new IllegalArgumentException("Malformed dialogue row");
                Dialogue item = new Dialogue(cols[0],cols[1],cols[2],cols[3]);
                if (cards.putIfAbsent(item.poiId(),item) != null)
                    throw new IllegalArgumentException("Duplicate dialogue");
            }
            return new PoiDialogueCatalog(cards);
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("Cannot load POI dialogues",e);
        }
    }
}
