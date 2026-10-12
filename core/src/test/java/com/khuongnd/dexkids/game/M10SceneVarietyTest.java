package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class M10SceneVarietyTest {
    @Test void keepsVisibleChunksStableAndSeeded() {
        var first=new M10SceneVariety(20261012L);
        var second=new M10SceneVariety(20261012L);
        int[] candidates={0,1,2,3,4,5,6,7,8,9,10,11};
        Set<Integer> observed=new HashSet<>();
        for(long i=0;i<120;i++){
            first.beginVisibleWindow(i);
            second.beginVisibleWindow(i);
            int a=first.pick(i,Biome.URBAN,candidates);
            assertEquals(a, first.pick(i,Biome.URBAN,candidates));
            assertEquals(a, second.pick(i,Biome.URBAN,candidates));
            observed.add(a);
        }
        assertEquals(12,observed.size());
    }

    @Test void avoidsLastEightWhenNineOrMoreChoicesExist() {
        var chooser=new M10SceneVariety(12);
        int[] options={0,1,2,3,4,5,6,7,8,9};
        int[] last=new int[8];
        java.util.Arrays.fill(last,-1);
        for(int i=0;i<50;i++){
            chooser.beginVisibleWindow(i);
            int result=chooser.pick(i,Biome.PARK,options);
            for(int previous:last) assertNotEquals(previous,result);
            last[i%8]=result;
        }
    }

    @Test void smallPoolsFallbackAndRewindRepeatDeterministically() {
        var selector=new M10SceneVariety(8);
        int[] small={2,4,6};
        int[] prior=new int[20];
        for(int i=0;i<prior.length;i++){
            selector.beginVisibleWindow(i);
            prior[i]=selector.pick(i,Biome.RIVER,small);
            assertTrue(prior[i]==2||prior[i]==4||prior[i]==6);
        }
        selector.beginVisibleWindow(0);
        for(int i=0;i<prior.length;i++){
            selector.beginVisibleWindow(i);
            assertEquals(prior[i],selector.pick(i,Biome.RIVER,small));
        }
        assertThrows(IllegalArgumentException.class,()->selector.pick(4,Biome.URBAN,new int[0]));
    }

    @Test void thirtyMinuteArtificialRouteReportsSceneDiversity() {
        // Simulated 30 minutes: 1800 seconds, every 10 seconds a new chunk.
        // This is not a real GPS trace, renderer FPS or child engagement result.
        var chooser = new M10SceneVariety(20261012L);
        int[] pool = {0,1,2,3,4,5,6,7,8,9,10,11};
        java.util.Map<Integer,Integer> histogram = new java.util.HashMap<>();
        int repeatedWithinEight = 0;
        int[] latest = new int[8];
        java.util.Arrays.fill(latest,-1);
        for (int step=0;step<180;step++) {
            chooser.beginVisibleWindow(step);
            int chosen=chooser.pick(step,Biome.URBAN,pool);
            histogram.merge(chosen,1,Integer::sum);
            for (int previous:latest) if (chosen==previous) repeatedWithinEight++;
            latest[step%8]=chosen;
        }
        System.out.println("M10 synthetic 30-min scene diversity: unique="
                +histogram.size()+"/"+pool.length+" recent-8-duplicates="+repeatedWithinEight);
        assertEquals(12,histogram.size());
        assertEquals(0,repeatedWithinEight);
    }

    @Test void differentTripsHaveDistinctSelections() {
        int[] options={0,1,2,3,4,5,6,7,8,9,10,11};
        var a=new M10SceneVariety(1);
        var b=new M10SceneVariety(2);
        int mismatches=0;
        for(int i=0;i<50;i++){
            a.beginVisibleWindow(i);b.beginVisibleWindow(i);
            if(a.pick(i,Biome.COUNTRYSIDE,options)!=b.pick(i,Biome.COUNTRYSIDE,options))
                mismatches++;
        }
        assertTrue(mismatches>=5);
    }
}
