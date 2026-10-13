package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.geo.GeoWorldIndex;
import com.khuongnd.dexkids.geo.JourneyPosition;
import com.khuongnd.dexkids.world.Biome;
import java.util.Objects;

/** Render-thread-only coarse GPS -> OSM cell -> cautious stable illustrative theme. */
public final class GeoWorldDirector {
    private final GeoWorldIndex index;
    private Biome current, candidate, queued;
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
        if(found==current) { candidate=queued=null;matches=0;return; }
        if(found!=candidate || (lastCandidateMs>0 && nowMs-lastCandidateMs>10_000)) {
            candidate=found;matches=1;
        } else if (lastCandidateMs<0 || nowMs-lastCandidateMs>=500) matches++;
        lastCandidateMs=nowMs;
        if(matches>=3) {
            if (current==null) { current=found; opacity=0f; }
            else if (current!=found) queued=found; // fade old biome out before switching
            candidate=null;matches=0;
        }
    }
    public void update(float delta, double distance, long nowMs) {
        if(!Float.isFinite(delta)||delta<0||!Double.isFinite(distance)||distance<0)
            throw new IllegalArgumentException("geo update");
        if(lastDistance>=0 && distance+3<lastDistance)reset();
        lastDistance=distance;
        if(current==null)return;
        if (queued!=null && lastAcceptedMs>=0 && nowMs-lastAcceptedMs>20_000)
            queued=null; // Never switch to a biome after all source fixes went stale.
        if (queued!=null) {
            opacity=Math.max(0f,opacity-Math.min(delta,.1f)/2.5f);
            if(opacity<=0f){ current=queued; queued=null; }
        } else if(lastAcceptedMs>=0 && nowMs-lastAcceptedMs>20_000) {
            opacity=Math.max(0f,opacity-Math.min(delta,.1f)/2.5f);
            if(opacity<=0f){current=null;candidate=null;matches=0;}
        } else opacity=Math.min(1f,opacity+Math.min(delta,.1f)/2.5f);
    }
    private PoiSceneDirector.Scene cachedScene = PoiSceneDirector.Scene.inactive(false);
    public PoiSceneDirector.Scene scene(double distance, boolean simulated) {
        if(current==null || opacity<=0f)
            return PoiSceneDirector.Scene.inactive(simulated);
        long chunk=SceneryLayout.firstChunk(distance);
        if(cachedScene.biome()!=current || cachedScene.alpha()!=opacity ||
            cachedScene.fromChunk()!=chunk || cachedScene.simulated()!=simulated)
            cachedScene=new PoiSceneDirector.Scene(current,opacity,chunk,"geo:illustrative",simulated);
        return cachedScene;
    }
    public void reset() {
        cachedScene=PoiSceneDirector.Scene.inactive(false);
        current=candidate=queued=null;matches=0;opacity=0f;
        lastSample=lastAcceptedMs=lastCandidateMs=-1;lastDistance=-1;
    }
}
