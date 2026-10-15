package com.khuongnd.dexkids.story.adventure;

/** Default local-atlas policy. No chat provider is assumed to offer image authoring.
 * No image provider is installed in P0; replay is unconditionally local, including cache misses. */
public final class StoryIllustrationPolicy {
    public enum Phase { AUTHORING, REPLAY }
    public enum Decision { LOCAL_ATLAS, PARENT_REVIEWED_CACHE, AUTHOR_WITH_EXPLICIT_COST_CONFIRMATION }
    public record Capabilities(boolean imageAuthoringSupported, boolean priceDisclosed) {}
    public Decision decide(Phase phase, boolean parentOptIn, Capabilities capabilities,
                           boolean reviewedCacheValid, boolean explicitCostConfirmation) {
        if(reviewedCacheValid)return Decision.PARENT_REVIEWED_CACHE;
        if(phase==Phase.AUTHORING && parentOptIn && capabilities!=null
                && capabilities.imageAuthoringSupported() && capabilities.priceDisclosed()
                && explicitCostConfirmation)return Decision.AUTHOR_WITH_EXPLICIT_COST_CONFIRMATION;
        return Decision.LOCAL_ATLAS;
    }
}
