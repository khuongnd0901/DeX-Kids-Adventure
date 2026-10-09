package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VehicleMotionModelTest {
    @Test void wheelRotationDependsOnDistanceNotRenderingCadence() {
        var fast = new VehicleMotionModel();
        var slow = new VehicleMotionModel();
        for(int i=0;i<60;i++) fast.advance(1.0/60, 10);
        for(int i=0;i<30;i++) slow.advance(1.0/30, 10);
        assertEquals(fast.wheelDegrees(1200), slow.wheelDegrees(1200), 0.0001);
        assertEquals(0f, new VehicleMotionModel().wheelDegrees(0), 0.0001);
        assertEquals(0f, new VehicleMotionModel().bodyBob(0), 0.0001);
    }
    @Test void suspensionRemainsBoundedDuringGpsSpike() {
        var motion = new VehicleMotionModel();
        motion.advance(0.016, 5);
        motion.advance(0.016, 50);
        assertTrue(Math.abs(motion.bodyPitchDegrees()) <= 3.5);
        assertThrows(IllegalArgumentException.class, ()->motion.advance(-1, 2));
        assertThrows(IllegalArgumentException.class, ()->motion.wheelDegrees(-9));
    }
}
