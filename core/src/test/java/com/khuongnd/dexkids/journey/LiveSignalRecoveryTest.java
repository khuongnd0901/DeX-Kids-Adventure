package com.khuongnd.dexkids.journey;
import com.khuongnd.dexkids.geo.GeoFix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LiveSignalRecoveryTest {
    @Test void rejectedJumpCannotRefreshMissingSignalMotion()  {
        var clock = new java.util.concurrent.atomic.AtomicLong(1_000_000);
        var feed = new LiveJourneyFeed(clock::get);
        long now = clock.get();
        feed.accept(new GeoFix(10,106,4,5,now-2000)); feed.update(.1f);
        feed.accept(new GeoFix(10.0001,106,4,5,now-1000));
        for(int i=0;i<20;i++) feed.update(.1f);
        assertTrue(feed.speedMetersPerSecond()>1);
        double before = feed.distanceMeters();
        clock.addAndGet(11000); // Synthetic core clock, never a device/GPS soak.
        feed.accept(new GeoFix(11,107,4,55,clock.get()));
        for(int i=0;i<50;i++) feed.update(.1f);
        assertEquals(before,feed.distanceMeters(),1e-9);
        assertTrue(feed.speedMetersPerSecond()<.1,"Rejected GPS jump must not refresh accepted-fix freshness");
    }
}
