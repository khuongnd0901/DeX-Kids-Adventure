package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import java.io.InputStream;

/**
 * One active 960x520 texture (~2 MiB RGBA), never 22 loaded simultaneously.
 * SVGs are rasterized by the existing build task. The whole backdrop is
 * illustrative—not a map, a photographed landmark, or a road match.
 */
final class PoiBackdropRenderer implements Disposable {
    private final PoiBackdropCatalog catalog;
    private Texture image;
    private String loadedKey = "";
    private boolean missing;

    PoiBackdropRenderer() {
        var mapping = Gdx.files.internal("poi/backdrop-map.tsv");
        if (!mapping.exists()) mapping = Gdx.files.internal("assets/poi/backdrop-map.tsv");
        PoiBackdropCatalog parsed = PoiBackdropCatalog.empty();
        if (mapping.exists()) {
            try (InputStream stream = mapping.read()) {
                parsed = PoiBackdropCatalog.parse(stream);
            } catch (Exception ex) {
                Gdx.app.error("PoiBackdrop", "Invalid illustration catalog, using fallback", ex);
            }
        }
        catalog = parsed;
    }

    boolean hasScene(PoiSceneDirector.Scene scene) {
        return scene != null && catalog.artFor(scene.poiId()) != null;
    }

    /** No texture creation if no POI or no authored illustration. */
    void draw(SpriteBatch batch, PoiSceneDirector.Scene scene) {
        if (scene == null || !scene.active()) return;
        String key = catalog.artFor(scene.poiId());
        if (key == null) return;
        if (!key.equals(loadedKey)) {
            if (image != null) image.dispose();
            image = null;
            missing = false;
            loadedKey = key;
            var file = Gdx.files.internal("generated/backdrops/" + key + ".png");
            if (!file.exists()) file = Gdx.files.internal("assets/generated/backdrops/" + key + ".png");
            if (!file.exists()) {
                missing = true;
                Gdx.app.error("PoiBackdrop", "Missing scene asset: " + key);
            } else {
                image = new Texture(file);
                image.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
        }
        if (missing || image == null) return;
        // Aspect 1200:650 preserved: 1920/1200 = 1.6, scene height = 1040.
        // Anchor to top of the 268px road; upper sky is naturally cropped.
        float r=batch.getColor().r, g=batch.getColor().g;
        float b=batch.getColor().b, a=batch.getColor().a;
        batch.setColor(r,g,b,a*scene.alpha());
        batch.draw(image,0,268,1920,1040);
        batch.setColor(r,g,b,a);
    }
    @Override public void dispose() {
        if (image != null) image.dispose();
        image = null;
    }
}
