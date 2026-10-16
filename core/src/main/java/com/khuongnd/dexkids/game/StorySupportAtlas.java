package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Disposable;
import com.khuongnd.dexkids.story.VisualCue.Prop;

/** One mandatory bundled page (1 MiB RGBA), no per-frame loading or region lookup. */
final class StorySupportAtlas implements Disposable {
    private final TextureAtlas atlas;
    private final TextureRegion[] props=new TextureRegion[Prop.values().length];
    final TextureRegion drop,sparkle,shadow,can,lens;
    StorySupportAtlas() {
        var path=Gdx.files.internal("generated/story-support/story-support.atlas");
        if(!path.exists())path=Gdx.files.internal("assets/generated/story-support/story-support.atlas");
        atlas=new TextureAtlas(path);
        for(var prop:Prop.values())if(prop.ordinal()>=Prop.BOOK.ordinal() && prop.ordinal()<=Prop.MAGNIFYING_GLASS.ordinal())
            props[prop.ordinal()]=required(prop.name().toLowerCase(java.util.Locale.ROOT));
        drop=required("water_drop");sparkle=required("sparkle");shadow=required("shadow");
        can=required("watering_can");lens=required("magnifying_glass");
    }
    private TextureRegion required(String name) {var r=atlas.findRegion(name);if(r==null)throw new IllegalStateException("Missing story support region: "+name);return r;}
    TextureRegion prop(Prop prop) {return props[prop.ordinal()];}
    public void dispose(){atlas.dispose();}
}
