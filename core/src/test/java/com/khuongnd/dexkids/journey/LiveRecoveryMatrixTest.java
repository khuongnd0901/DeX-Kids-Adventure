package com.khuongnd.dexkids.journey;
import com.khuongnd.dexkids.geo.GeoFix;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LiveRecoveryMatrixTest {
    @Test void lossStopsMotionAndLongGapRecoveryRebasesWithoutTeleport() {
        var clock=new AtomicLong(1_000_000); var feed=new LiveJourneyFeed(clock::get);
        feed.accept(new GeoFix(10,106,4,0,clock.get())); feed.update(.1f);
        clock.addAndGet(1000); feed.accept(new GeoFix(10.0001,106,4,10,clock.get()));
        for(int i=0;i<20;i++) feed.update(.1f);
        assertTrue(feed.speedMetersPerSecond()>1); double before=feed.distanceMeters();
        clock.addAndGet(31000); for(int i=0;i<50;i++) feed.update(.1f);
        assertTrue(feed.speedMetersPerSecond()<.1);
        feed.accept(new GeoFix(10.1,106,4,0,clock.get())); feed.update(.1f);
        assertEquals(before,feed.distanceMeters(),1e-9);
        clock.addAndGet(1000); feed.accept(new GeoFix(10.1001,106,4,10,clock.get()));
        feed.update(.1f); assertTrue(feed.distanceMeters()>before);
        assertTrue(feed.distanceMeters()<before+20);
    }
    @Test void duplicateOutOfOrderAndPoorAccuracyDoNotRefreshFreshness() {
        for(int kind=0;kind<3;kind++) {
            var clock=new AtomicLong(1_000_000); var feed=new LiveJourneyFeed(clock::get);
            feed.accept(new GeoFix(10,106,4,0,clock.get())); feed.update(.1f);
            clock.addAndGet(1000); long acceptedAt=clock.get();
            feed.accept(new GeoFix(10.0001,106,4,10,acceptedAt));
            for(int i=0;i<20;i++) feed.update(.1f);
            double before=feed.distanceMeters(); clock.addAndGet(11000);
            long at=kind==0?acceptedAt:kind==1?acceptedAt-1000:clock.get();
            feed.accept(new GeoFix(10.0002,106,kind==2?90:4,10,at));
            for(int i=0;i<50;i++) feed.update(.1f);
            assertEquals(before,feed.distanceMeters(),1e-9);
            assertTrue(feed.speedMetersPerSecond()<.1,"Rejected fix kind="+kind);
        }
    }
}
