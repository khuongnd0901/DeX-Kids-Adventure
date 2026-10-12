package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.khuongnd.dexkids.world.Biome;
import java.util.ArrayList;

/** Optional, source-allowlisted M10 prop pages; loaded once on GL initialization. */
final class M10PropAtlas implements Disposable {
    private final TextureAtlas atlas;
    private final TextureRegion[][] groups = new TextureRegion[4][];

    M10PropAtlas() {
        FileHandle pageFile=Gdx.files.internal("generated/m10/props.atlas");
        FileHandle mapFile=Gdx.files.internal("generated/m10/props-map.tsv");
        if (!pageFile.exists()) pageFile=Gdx.files.internal("assets/generated/m10/props.atlas");
        if (!mapFile.exists()) mapFile=Gdx.files.internal("assets/generated/m10/props-map.tsv");
        TextureAtlas loaded=null;
        if (pageFile.exists() && mapFile.exists()) {
            try {
                loaded=new TextureAtlas(pageFile);
                @SuppressWarnings("unchecked")
                ArrayList<TextureRegion>[] bins=new ArrayList[4];
                for (int i=0;i<bins.length;i++) bins[i]=new ArrayList<>();
                for (String line:mapFile.readString("UTF-8").split("\n")) {
                    if (line.isBlank()) continue;
                    String[] parts=line.split("\t");
                    if (parts.length!=2) throw new IllegalArgumentException("Invalid M10 art mapping");
                    int group=switch(parts[0]) {
                        case "URB" -> 0;
                        case "VIL" -> 1;
                        case "FOR" -> 2;
                        case "FRM" -> 3;
                        default -> -1; // coast/mountain intentionally excluded from generic roads
                    };
                    if (group<0) continue;
                    TextureRegion region=loaded.findRegion(parts[1]);
                    if (region==null) throw new IllegalArgumentException("Unknown M10 region");
                    bins[group].add(region);
                }
                for(int i=0;i<bins.length;i++) groups[i]=bins[i].toArray(new TextureRegion[0]);
            } catch (RuntimeException exception) {
                if (loaded!=null) loaded.dispose();
                loaded=null;
                for(int i=0;i<groups.length;i++)groups[i]=null;
                Gdx.app.error("M10Art","Atlas rejected; retaining original scenery",exception);
            }
        }
        atlas=loaded;
    }

    TextureRegion choose(Biome biome,int choice) {
        if (atlas==null) return null;
        int group=switch(biome) {
            case URBAN -> 0;
            case RESIDENTIAL -> 1;
            case PARK -> 2;
            case COUNTRYSIDE -> 3;
            case BRIDGE, RIVER, GENERAL -> -1;
        };
        if(group<0 || groups[group]==null || groups[group].length==0)return null;
        TextureRegion[] pool=groups[group];
        return pool[Math.floorMod(choice,pool.length)];
    }

    @Override public void dispose() {
        if(atlas!=null)atlas.dispose();
    }
}
