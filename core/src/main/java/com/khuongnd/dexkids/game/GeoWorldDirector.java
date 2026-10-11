package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.geo.GeoWorldIndex;
import com.khuongnd.dexkids.geo.JourneyPosition;
import com.khuongnd.dexkids.world.Biome;
import java.util.Objects;

/** Render-thread-only coarse GPS -> OSM cell -> cautious stable illustrative theme. */
public final class GeoWorldDirector {
    private final GeoWorldIndex index;
    private Biome current, candidate;
    private int matches;
    private float opacity;
    private long lastSample=-1, lastAcceptedMs=-1, lastCandidateMs=-1;
    private double lastDistance=-1;
    public GeoWorldDirector(GeoWorldIndex index) { this.index=Objects.requireNonNull(index); }
    public Biome biome() { return current; }
    public float opacity() { return opacity; }
    public void observe(JourneyPosition fix, long nowMs) {
        if (fix==null || !fix.sufficientQuality() || nowMs<=0 || fix.sampleId()<=lastSample) return;
        lastSample=fix.sampleId();
        Biome found=index.at(fix.latitude(),fix.longitude());
        if (found==null) {
            candidate=null; matches=0;
            return;
        }
        lastAcceptedMs=nowMs;
        if(found==current) { candidate=null;matches=0;return; }
        if(found!=candidate || (lastCandidateMs>0 && nowMs-lastCandidateMs>10_000)) {
            candidate=found;matches=1;
        } else if (lastCandidateMs<0 || nowMs-lastCandidateMs>=500) matches++;
        lastCandidateMs=nowMs;
        if(matches>=3) {
            current=found;opacity=0f;candidate=null;matches=0;
        }
    }
    public void update(float delta, double distance, long nowMs) {
        if(!Float.isFinite(delta)||delta<0||!Double.isFinite(distance)||distance<0)
            throw new IllegalArgumentException("geo update");
        if(lastDistance>=0 && distance+3<lastDistance)reset();
        lastDistance=distance;
        if(current==null)return;
        if(lastAcceptedMs>=0 && nowMs-lastAcceptedMs>20_000) {
            opacity=Math.max(0f,opacity-Math.min(delta,.1f)/2.5f);
            if(opacity<=0f){current=null;candidate=null;matches=0;}
        } else opacity=Math.min(1f,opacity+Math.min(delta,.1f)/2.5f);
    }
    public PoiSceneDirector.Scene scene(double distance, boolean simulated) {
        if(current==null || opacity<=0f)
            return new PoiSceneDirector.Scene(Biome.GENERAL,0f,0L,"",simulated);
        return new PoiSceneDirector.Scene(current,opacity,SceneryLayout.firstChunk(distance),
                "geo:illustrative",simulated);
    }
    public void reset() {
        current=candidate=null;matches=0;opacity=0f;
        lastSample=lastAcceptedMs=lastCandidateMs=-1;lastDistance=-1;
    }
}
