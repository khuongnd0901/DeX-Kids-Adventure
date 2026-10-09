package com.khuongnd.dexkids.geo;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/** Strict offline, immutable catalogue of manually approved OSM point representatives. */
public final class OfflinePoiCatalog {
    public static final String HEADER = "id\tname\tlat\tlon\ttype\tosm_url\tverified_at\treviewer";
    public static final int MAX_BYTES = 512 * 1024;
    public static final int MAX_ENTRIES = 5000;
    public enum Type { PARK, BRIDGE, RIVER, LANDMARK, MUSEUM, NATURE }
    public record Entry(Poi poi, Type type, String reviewer) {
        public Entry {
            Objects.requireNonNull(poi);
            Objects.requireNonNull(type);
            if (reviewer == null || !reviewer.matches("[a-zA-Z0-9_.-]{2,40}"))
                throw new IllegalArgumentException("Human review marker missing");
        }
    }
    private final List<Entry> entries;
    private OfflinePoiCatalog(List<Entry> items) { entries = List.copyOf(items); }
    public static OfflinePoiCatalog empty() { return new OfflinePoiCatalog(List.of()); }
    public List<Entry> entries() { return entries; }

    public static OfflinePoiCatalog parse(InputStream input) {
        Objects.requireNonNull(input);
        try {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            byte[] block = new byte[4096];
            int n;
            while ((n = input.read(block)) >= 0) {
                if (data.size() + n > MAX_BYTES) throw new IllegalArgumentException("POI catalogue too large");
                data.write(block, 0, n);
            }
            String raw = new String(data.toByteArray(), StandardCharsets.UTF_8);
            if (raw.indexOf('\uFFFD') >= 0 || raw.indexOf('\0') >= 0)
                throw new IllegalArgumentException("Invalid UTF-8 POI catalogue");
            String[] lines = raw.split("\\R", -1);
            if (lines.length < 2 || !lines[0].equals("# dexkids-poi-v1") || !lines[1].equals(HEADER))
                throw new IllegalArgumentException("Unsupported POI catalogue schema");
            List<Entry> results = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) continue;
                if (results.size() == MAX_ENTRIES) throw new IllegalArgumentException("Too many POIs");
                String[] f = line.split("\\t", -1);
                if (f.length != 8) throw new IllegalArgumentException("Invalid POI columns at row " + (i+1));
                if (!f[0].matches("osm:(node|way|relation):[1-9][0-9]{0,15}"))
                    throw new IllegalArgumentException("Invalid OSM ID");
                if (!ids.add(f[0])) throw new IllegalArgumentException("Duplicate POI ID");
                String name = f[1].strip();
                if (name.length() < 2 || name.length() > 90 || name.chars().anyMatch(ch -> ch < 32))
                    throw new IllegalArgumentException("Invalid place name");
                String[] identifier = f[0].split(":");
                String required = "https://www.openstreetmap.org/" + identifier[1] + "/" + identifier[2];
                if (!f[5].equals(required)) throw new IllegalArgumentException("POI URL mismatch");
                Poi poi = new Poi(f[0], name, Double.parseDouble(f[2]),
                        Double.parseDouble(f[3]), URI.create(f[5]), Instant.parse(f[6]));
                Type type = Type.valueOf(f[4]);
                results.add(new Entry(poi, type, f[7]));
            }
            return new OfflinePoiCatalog(results);
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("Cannot load reviewed offline POI data", e);
        }
    }
}
