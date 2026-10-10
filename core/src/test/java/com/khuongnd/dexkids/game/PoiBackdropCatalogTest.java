package com.khuongnd.dexkids.game;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class PoiBackdropCatalogTest {
    @Test void everySourcedLiveAndSamplePoiHasAuthoredArtwork() throws Exception {
        PoiBackdropCatalog catalog;
        try (var stream = Files.newInputStream(Path.of("..", "assets/poi/backdrop-map.tsv"))) {
            catalog = PoiBackdropCatalog.parse(stream);
        }
        assertTrue(catalog.size() >= 40, "Expected the expanded source-backed POI art map");
        for (String file : new String[]{"sample-hcm.tsv", "live-landmarks.tsv"}) {
            for (String line : Files.readAllLines(Path.of("..", "assets/poi", file))) {
                if (line.isBlank() || line.startsWith("#") || line.startsWith("id\t")) continue;
                String poiId=line.split("\t")[0];
                String key=catalog.artFor(poiId);
                assertNotNull(key, "Missing named art for " + poiId);
                assertTrue(Files.isRegularFile(Path.of("..", "art/assets-source/backdrops",
                        key + ".svg")), "Missing editable art " + key);
            }
        }
        assertNull(catalog.artFor("osm:node:999999999999999"), "Unknown POI stays procedural");
    }
    @Test void malformedCatalogIsRejected() {
        assertThrows(java.io.IOException.class, () -> PoiBackdropCatalog.parse(
                new java.io.ByteArrayInputStream("osm:node:123\t../../bad\n".getBytes())));
        assertThrows(java.io.IOException.class, () -> PoiBackdropCatalog.parse(
                new java.io.ByteArrayInputStream("osm:node:123\tcity\nosm:node:123\tbeach\n".getBytes())));
    }
    @Test void busPreservesPhysicalAspectAndPassengersBelongToSameVehicle() {
        assertTrue(VehicleLayout.sourceAspectError() < .01f, "Distorted bus chassis");
        assertTrue(VehicleLayout.wheelCenterX(0) < VehicleLayout.wheelCenterX(1));
        assertTrue(VehicleLayout.wheelCenterY() < VehicleLayout.Y + 100);
        assertTrue(VehicleLayout.SAU_X > VehicleLayout.X);
        assertTrue(VehicleLayout.ONG_X + VehicleLayout.CHILD_W < VehicleLayout.X + VehicleLayout.WIDTH);
        assertTrue(VehicleLayout.CAPYBARA_X + VehicleLayout.CAPYBARA_W < VehicleLayout.X + VehicleLayout.WIDTH);
        assertTrue(VehicleLayout.CHILD_Y > VehicleLayout.Y);
        assertTrue(VehicleLayout.CHILD_Y + VehicleLayout.CHILD_H <
                VehicleLayout.Y + VehicleLayout.HEIGHT + 10);
    }
}
