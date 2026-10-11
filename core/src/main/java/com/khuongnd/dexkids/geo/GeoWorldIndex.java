package com.khuongnd.dexkids.geo;

import com.khuongnd.dexkids.world.Biome;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Sparse offline OSM landcover raster (0.002 degree cells; illustrative only).
 * Index is loaded once; queries allocate nothing and never contact a server.
 * The builder samples cell CENTER inside a source polygon, not a route match.
 */
public final class GeoWorldIndex {
    public static final double CELL_DEGREES = .002;
    private final Map<Long, Biome> cells;
    private GeoWorldIndex(Map<Long,Biome> rows) { cells=Map.copyOf(rows); }
    public static GeoWorldIndex empty() { return new GeoWorldIndex(Map.of()); }
    public int size() { return cells.size(); }

    public Biome at(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90 ||
                !Double.isFinite(longitude) || longitude < -180 || longitude > 180)
            return null;
        return cells.get(key(cell(latitude),cell(longitude)));
    }
    public static int cell(double degree) { return (int)Math.floor(degree / CELL_DEGREES); }
    private static long key(int latitude, int longitude) {
        return ((long)latitude << 32) | (longitude & 0xffffffffL);
    }
    public static GeoWorldIndex parse(InputStream data) throws IOException {
        Objects.requireNonNull(data);
        Map<Long,Biome> index=new HashMap<>();
        try (var reader=new BufferedReader(new InputStreamReader(data,StandardCharsets.UTF_8))) {
            String header=reader.readLine();
            if (!"# dexkids-geo-cells-v1".equals(header))
                throw new IOException("Invalid geo world header");
            String line; int rows=0;
            while ((line=reader.readLine())!=null) {
                if (line.isBlank() || line.startsWith("#"))continue;
                if (++rows>100_000)throw new IOException("Geo world capacity exceeded");
                String[] fields=line.split("\\t",-1);
                if(fields.length!=4)throw new IOException("Geo cell fields invalid");
                try {
                    int lat=Integer.parseInt(fields[0]),lon=Integer.parseInt(fields[1]);
                    if(lat<-45000||lat>45000||lon<-90000||lon>90000)
                        throw new IOException("Geo cell out of bounds");
                    Biome biome=Biome.valueOf(fields[2]);
                    if (!fields[3].matches("osm:(?:way|relation):[1-9][0-9]{0,15}"))
                        throw new IOException("Unverified geometry provenance");
                    index.putIfAbsent(key(lat,lon),biome);
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Invalid geo world cell",ex);
                }
            }
        }
        return new GeoWorldIndex(index);
    }
}
