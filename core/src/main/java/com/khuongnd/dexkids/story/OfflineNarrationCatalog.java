package com.khuongnd.dexkids.story;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/** Immutable reviewed-or-explicit-preview narration content. Never generates facts. */
public final class OfflineNarrationCatalog {
    public static final String HEADER = "id\tpoi_id\ttext_vi\tfact_url\tverified_at\tmin_age\tmax_age";
    private final Map<String, NarrationCue> byPoi;
    private OfflineNarrationCatalog(Map<String,NarrationCue> cues) { byPoi = Map.copyOf(cues); }
    public static OfflineNarrationCatalog empty() { return new OfflineNarrationCatalog(Map.of()); }
    public int size() { return byPoi.size(); }
    public Optional<NarrationCue> findByPoiId(String poiId) { return Optional.ofNullable(byPoi.get(poiId)); }

    public static OfflineNarrationCatalog parse(InputStream input) {
        Objects.requireNonNull(input);
        try {
            // readNBytes(int) is Android API 33+; support minSdk 30 without desugaring.
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = input.read(chunk)) != -1) {
                if (buffer.size() + n > 64 * 1024)
                    throw new IllegalArgumentException("Narration asset too large");
                buffer.write(chunk, 0, n);
            }
            byte[] all = buffer.toByteArray();
            if (all.length>64*1024) throw new IllegalArgumentException("Narration asset too large");
            String raw=new String(all,StandardCharsets.UTF_8);
            if (raw.indexOf('\uFFFD')>=0 || raw.indexOf('\0')>=0)
                throw new IllegalArgumentException("Invalid UTF-8");
            String[] lines=raw.split("\\R", -1);
            if (lines.length<2 || !lines[0].equals("# dexkids-narration-v1") ||
                    !lines[1].equals(HEADER)) throw new IllegalArgumentException("Invalid narration schema");
            Map<String,NarrationCue> found=new HashMap<>();
            for (int i=2; i<lines.length; i++) {
                if (lines[i].isEmpty()) continue;
                if (found.size()>=1000) throw new IllegalArgumentException("Too many cues");
                String[] f=lines[i].split("\\t",-1);
                if (f.length!=7 || !f[1].matches("osm:(node|way|relation):[1-9][0-9]{0,15}"))
                    throw new IllegalArgumentException("Invalid narration linkage");
                NarrationCue cue = new NarrationCue(f[0],f[1],f[2],URI.create(f[3]),
                    Instant.parse(f[4]),Integer.parseInt(f[5]),Integer.parseInt(f[6]));
                if (found.putIfAbsent(cue.poiId(),cue)!=null)
                    throw new IllegalArgumentException("Duplicate narration POI");
            }
            return new OfflineNarrationCatalog(found);
        } catch (Exception ex) { throw new IllegalArgumentException("Unsafe narration data",ex); }
    }
}
