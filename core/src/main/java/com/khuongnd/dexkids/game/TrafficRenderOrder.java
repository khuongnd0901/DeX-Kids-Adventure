package com.khuongnd.dexkids.game;

/** Y-up painter depth: lower tire contact is closer to the camera. UI is always last. */
public final class TrafficRenderOrder {
    public enum Plane { FAR_TRAFFIC, BUS, NEAR_TRAFFIC, STORY_HUD }
    private TrafficRenderOrder() {}
    public static Plane plane(float trafficGroundY, float busGroundY) {
        return trafficGroundY >= busGroundY ? Plane.FAR_TRAFFIC : Plane.NEAR_TRAFFIC;
    }
}
