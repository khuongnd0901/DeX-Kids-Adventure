package com.khuongnd.dexkids.geo;

import com.khuongnd.dexkids.world.Biome;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class GeoWorldIndexTest {
    private static ByteArrayInputStream bytes(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
    @Test void readsOnlyOSMBackedCells() throws Exception {
        int lat=GeoWorldIndex.cell(10.95),lon=GeoWorldIndex.cell(106.82);
        var data=GeoWorldIndex.parse(bytes("# dexkids-geo-cells-v1\n"+
                lat+"\t"+lon+"\tCOUNTRYSIDE\tosm:way:123\n"));
        assertEquals(Biome.COUNTRYSIDE,data.at(10.95,106.82));
        assertNull(data.at(11.0,107.0));
        assertEquals(1,data.size());
    }
    @Test void blocksInvalidHeaderAndProvenance() {
        assertThrows(IOException.class,()->GeoWorldIndex.parse(bytes("bad\n")));
        assertThrows(IOException.class,()->GeoWorldIndex.parse(bytes(
                "# dexkids-geo-cells-v1\n1\t2\tPARK\tunknown\n")));
        assertThrows(IOException.class,()->GeoWorldIndex.parse(bytes(
                "# dexkids-geo-cells-v1\n1\t2\tSECRET\tosm:way:123\n")));
    }
}
