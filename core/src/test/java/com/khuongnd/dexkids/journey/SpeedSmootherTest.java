
package com.khuongnd.dexkids.journey;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpeedSmootherTest {
    @Test void smoothingIsFrameRateIndependent() {
        var a = new SpeedSmoother();
        var b = new SpeedSmoother();
        for (int i = 0; i < 100; i++) a.update(12, 0.01);
        for (int i = 0; i < 10; i++) b.update(12, 0.1);
        assertEquals(a.value(), b.value(), 1e-9);
    }

    @Test void negativesAreClampedAndInvalidNumbersRejected() {
        var smoother = new SpeedSmoother();
        assertEquals(0, smoother.update(-5, 1));
        assertThrows(IllegalArgumentException.class, () -> smoother.update(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> smoother.update(1, -1));
    }
}
