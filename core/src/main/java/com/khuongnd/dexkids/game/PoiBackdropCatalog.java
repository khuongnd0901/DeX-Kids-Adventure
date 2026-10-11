package com.khuongnd.dexkids.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Curated POI-ID -> original SVG illustration mapping, separate from geographic
 * proximity detection. Missing IDs deliberately fall back to procedural scenery.
 */
public final class PoiBackdropCatalog {
    private final Map<String, String> scenes;
    private PoiBackdropCatalog(Map<String, String> scenes) {
        this.scenes = Collections.unmodifiableMap(new HashMap<>(scenes));
    }
    public static PoiBackdropCatalog empty() { return new PoiBackdropCatalog(Map.of()); }
    public String artFor(String poiId) { return scenes.get(poiId); }
    public int size() { return scenes.size(); }

    public static PoiBackdropCatalog parse(InputStream stream) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (++count > 512) throw new IOException("Oversized backdrop catalog");
                if (line.isBlank() || line.startsWith("#") || line.equals("poi_id\tbackdrop")) continue;
                String[] row = line.split("\t", -1);
                if (row.length != 2 || !row[0].matches("osm:(node|way|relation):[0-9]+")
                        || !row[1].matches("[a-z][a-z0-9_]{1,50}"))
                    throw new IOException("Malformed POI backdrop row at line " + count);
                if (map.putIfAbsent(row[0], row[1]) != null)
                    throw new IOException("Duplicate POI backdrop " + row[0]);
            }
        }
        return new PoiBackdropCatalog(map);
    }
}
