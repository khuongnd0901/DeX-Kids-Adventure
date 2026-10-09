package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Pure state/transform checks; these do not validate rendered frames or audio output. */
class AnimationCadenceMatrixTest {
    @Test void cap001DefaultIdleAndCap002BlinkCadenceAcrossFrameRates() {
        var initial = new CharacterAnimationController();
        assertEquals(CharacterAnimationController.State.IDLE, initial.state());
        assertEquals(CharacterAnimationController.Frame.IDLE, initial.frame());
        for (double elapsed : new double[]{4.2, 4.4, 8.5, 8.7}) {
            var slow = atElapsed(30, elapsed);
            var fast = atElapsed(60, elapsed);
            assertEquals(elapsed, slow.elapsedTotal(), 1e-9);
            assertEquals(elapsed, fast.elapsedTotal(), 1e-9);
            var expected = elapsed == 4.2 || elapsed == 8.5
                    ? CharacterAnimationController.Frame.BLINK
                    : CharacterAnimationController.Frame.IDLE;
            assertEquals(expected, slow.frame());
            assertEquals(expected, fast.frame());
        }
    }

    @Test void cap009ZeroDeltaPreservesStateTimeAndFrameEvenWithPendingTransition() {
        var actor = atElapsed(60, 4.2);
        double elapsed = actor.elapsedTotal(), stateTime = actor.stateTime();
        var state = actor.state();
        var frame = actor.frame();
        actor.requestWave();
        actor.drive(0, 10);
        assertEquals(state, actor.state());
        assertEquals(frame, actor.frame());
        assertEquals(elapsed, actor.elapsedTotal());
        assertEquals(stateTime, actor.stateTime());
        actor.drive(0.01, 10);
        assertEquals(CharacterAnimationController.State.WAVING, actor.state());
    }

    @Test void cap004RollingThresholdAndCap009SlowFrameRemainFinite() {
        var actor = new CharacterAnimationController();
        actor.drive(0.1, Math.nextDown(0.65));
        assertEquals(CharacterAnimationController.State.IDLE, actor.state());
        actor.drive(0.1, 0.65);
        assertEquals(CharacterAnimationController.State.ROLLING, actor.state());
        actor.drive(5, 10);
        assertEquals(CharacterAnimationController.State.ROLLING, actor.state());
        assertTrue(Double.isFinite(actor.stateTime()));
        assertEquals(5.2, actor.elapsedTotal(), 1e-9);
        assertNotNull(actor.frame());
    }

    @Test void cap008SurpriseDoesNotOverrideExplicitNarrationFrames() {
        var actor = new CharacterAnimationController();
        actor.setSurprised(true);
        actor.setNarrationActive(true); // Test signal only, not evidence of real speech.
        actor.drive(0.1, 0);
        assertEquals(CharacterAnimationController.Frame.TALK_OPEN, actor.frame());
        actor.drive(0.2, 0);
        assertEquals(CharacterAnimationController.Frame.TALK_CLOSED, actor.frame());
        actor.setNarrationActive(false);
        actor.drive(0.1, 0);
        assertEquals(CharacterAnimationController.Frame.SURPRISED, actor.frame());
        assertNotEquals(CharacterAnimationController.State.TALKING, actor.state());
    }

    @Test void bus001StationaryDistanceNeverRotatesOrBobsAcrossManyFrames() {
        var bus = new VehicleMotionModel();
        float parkedAngle = bus.wheelDegrees(123.4);
        for (int frame = 0; frame < 600; frame++) {
            bus.advance(1.0 / 60, 0);
            assertEquals(parkedAngle, bus.wheelDegrees(123.4));
            assertEquals(0f, bus.bodyBob(0), 1e-6f);
            assertEquals(0f, bus.bodyPitchDegrees(), 1e-6f);
        }
    }

    @Test void bus002Through006AccelerationBrakingAndDistanceAgreeAcrossCadences() {
        var slow = new VehicleMotionModel();
        var fast = new VehicleMotionModel();
        for (int second = 0; second < 20; second++) {
            double speed = second < 10 ? second * 2 : (19 - second) * 2;
            for (int frame = 0; frame < 30; frame++) {
                slow.advance(1.0 / 30, speed);
                assertTrue(Math.abs(slow.bodyPitchDegrees()) <= 3.5);
            }
            for (int frame = 0; frame < 60; frame++) {
                fast.advance(1.0 / 60, speed);
                assertTrue(Math.abs(fast.bodyPitchDegrees()) <= 3.5);
            }
            double distance = second * 37.25;
            assertEquals(slow.wheelDegrees(distance), fast.wheelDegrees(distance));
        }
        assertEquals(0f, slow.bodyBob(0), 1e-6f);
        assertEquals(0f, fast.bodyBob(0), 1e-6f);
    }

    private static CharacterAnimationController atElapsed(int fps, double seconds) {
        var actor = new CharacterAnimationController();
        int frames = (int) Math.round(seconds * fps);
        for (int i = 0; i < frames; i++) actor.drive(seconds / frames, 0);
        return actor;
    }
}
