package com.khuongnd.dexkids.qa;

import android.os.Bundle;
/** Live subscription tests never import credentials or silently enable consent. */
final class StoryAuthorValidation {
    static void run(LifecycleValidationRunner runner, Bundle result) {
        result.putString("t054_story_ai", "LIVE_AUTH_NOT_VERIFIED: owner must sign in and consent in Kids; use t054_author_safety and t054_author_failures for controlled regressions");
    }
}
