package com.khuongnd.dexkids.game;

import com.khuongnd.dexkids.world.Biome;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrafficDepthModelTest {
    @Test void farAlwaysBehindBothBusModes() {
        for (boolean png : new boolean[]{false, true})
            assertEquals(TrafficRenderOrder.Plane.FAR_TRAFFIC,
                    TrafficRenderOrder.plane(TrafficDepthModel.Lane.FAR.groundY, VehicleLayout.groundY(png)));
    }
    @Test void nearAlwaysInFrontOfBothBusModes() {
        for (boolean png : new boolean[]{false, true})
            assertEquals(TrafficRenderOrder.Plane.NEAR_TRAFFIC,
                    TrafficRenderOrder.plane(TrafficDepthModel.Lane.NEAR.groundY, VehicleLayout.groundY(png)));
    }
    @Test void crossingLanesHaveStableDistinctDepths() {
        TrafficLayout far = new TrafficLayout(), near = new TrafficLayout();
        for (float x = -400; x < 2000; x += 10) {
            far.place(0, TrafficDepthModel.Lane.FAR, x, true, true);
            near.place(2, TrafficDepthModel.Lane.NEAR, 1000-x, true, true);
            assertTrue(far.groundY > near.groundY);
            assertEquals(TrafficRenderOrder.Plane.FAR_TRAFFIC, TrafficRenderOrder.plane(far.groundY, near.groundY));
        }
    }
    @Test void tireContactsStayOnRoadAndSeparateFromBus() {
        for (boolean storybook : new boolean[]{false, true}) for (var lane : TrafficDepthModel.Lane.values())
            for (int kind = 0; kind < 6; kind++) {
                TrafficLayout layout = new TrafficLayout(); layout.place(kind, lane, 500, true, storybook);
                assertEquals(lane.groundY, layout.y + layout.height * (1-TrafficLayout.contact(kind, storybook)), .001);
                assertTrue(layout.groundY > 0 && layout.groundY < 263);
                assertTrue(Math.abs(layout.groundY - VehicleLayout.groundY(true)) > 50);
            }
    }
    @Test void sizesAndAspectRespectVehicleKindsAndPerspective() {
        for (int kind = 0; kind < 6; kind++) {
            TrafficLayout a = new TrafficLayout(), b = new TrafficLayout();
            a.place(kind, TrafficDepthModel.Lane.FAR, 0, true, true);
            b.place(kind, TrafficDepthModel.Lane.NEAR, 0, true, true);
            assertEquals(360f/185, a.width/a.height, .00001);
            assertEquals(.74, a.height/b.height, .00001);
            assertTrue(b.width < VehicleLayout.PNG_WIDTH * .6);
            if (kind >= 4) assertTrue(b.height < TrafficLayout.height(0, TrafficDepthModel.Lane.NEAR));
        }
        assertTrue(TrafficLayout.height(2, TrafficDepthModel.Lane.NEAR) > TrafficLayout.height(0, TrafficDepthModel.Lane.NEAR));
    }
    @Test void crossingChunkBoundaryDoesNotResetMovementOrType() {
        TrafficDepthModel m = new TrafficDepthModel(20261008, true);
        m.advance(4, Biome.URBAN);
        long chunk = SceneryLayout.firstChunk(640/7.5-.001);
        float before=m.near().x; int kind=m.near().kind;
        assertEquals(chunk+1, SceneryLayout.firstChunk(640/7.5+.001));
        m.advance(.002f, Biome.URBAN);
        assertEquals(kind, m.near().kind);
        assertEquals(before+TrafficDepthModel.speed(kind, TrafficDepthModel.Lane.NEAR)*.002f,m.near().x,.001);
    }
    @Test void pauseFreezesAndResumeKeepsDepthAndIdentity() {
        TrafficDepthModel m = new TrafficDepthModel(99, true); m.advance(4, Biome.URBAN);
        float x=m.near().x, y=m.near().groundY; int kind=m.near().kind;
        for(int i=0;i<100;i++) m.advance(JourneyRenderPause.effectiveDelta(.1f,true),Biome.PARK);
        assertEquals(x,m.near().x); assertEquals(kind,m.near().kind); assertEquals(y,m.near().groundY);
        m.advance(JourneyRenderPause.effectiveDelta(.1f,false),Biome.PARK);
        assertTrue(m.near().x>x); assertEquals(y,m.near().groundY); assertEquals(kind,m.near().kind);
    }
    @Test void nearCanvasCannotCoverStoryOrCabinAndUiIsLast() {
        for (int kind=0;kind<6;kind++) {
            TrafficLayout layout=new TrafficLayout(); layout.place(kind,TrafficDepthModel.Lane.NEAR,500,true,true);
            assertTrue(layout.y+layout.height<315); // companion interaction begins y315
            assertTrue(layout.y+layout.height<VehicleLayout.CHILD_Y-5);
            assertTrue(layout.y+layout.height<VehicleLayout.PNG_CHILD_Y-5);
        }
        assertTrue(TrafficRenderOrder.Plane.STORY_HUD.ordinal()>TrafficRenderOrder.Plane.NEAR_TRAFFIC.ordinal());
    }
    @Test void deterministicTypeSpeedsAndOffscreenSlotReplacement() {
        TrafficDepthModel a=new TrafficDepthModel(20261008,true), b=new TrafficDepthModel(20261008,true);
        int mask=0;
        for(int i=0;i<22000;i++) {
            float oldX=a.near().x; int oldKind=a.near().kind; boolean visible=a.near().visible;
            a.advance(.1f,Biome.URBAN); b.advance(.1f,Biome.URBAN);
            assertEquals(a.near().x,b.near().x); assertEquals(a.far().kind,b.far().kind);
            if(a.near().visible) mask|=1<<a.near().kind;
            if(oldKind!=a.near().kind) { assertFalse(visible); assertFalse(a.near().visible); }
            else if(visible && a.near().visible) assertTrue(a.near().x>oldX);
        }
        assertEquals(63,mask);
        for(var lane:TrafficDepthModel.Lane.values()) for(int kind=0;kind<6;kind++) {
            float width=TrafficLayout.height(kind,lane)*360/185;
            assertTrue((-width-8+TrafficDepthModel.SLOT_SECONDS*TrafficDepthModel.speed(kind,lane))>1920+50,
                    "old vehicle exits with safety gap before next entry");
        }
        assertTrue(TrafficDepthModel.speed(5,TrafficDepthModel.Lane.NEAR)<TrafficDepthModel.speed(0,TrafficDepthModel.Lane.NEAR));
    }
    @Test void biomeChangesDoNotPopVisibleTrafficOrSpawnInWater() {
        TrafficDepthModel m=new TrafficDepthModel(0,true);m.advance(5,Biome.URBAN);
        assertTrue(m.near().visible); int kind=m.near().kind;
        m.advance(.1f,Biome.RIVER);assertTrue(m.near().visible);assertEquals(kind,m.near().kind);
        m.advance(22,Biome.RIVER);assertFalse(m.near().visible);
    }
}
