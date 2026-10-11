package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StoryCompanionPoseTest {
    @Test void storyTopicsSelectCorrespondingChildGestureAndTurn() {
        assertEquals(StoryCompanionPose.COUNT, AdventureHud.Prompt.fromBeat("sau-count", "Đếm ba").pose());
        assertEquals("SAU", AdventureHud.Prompt.fromBeat("sau-count", "Đếm ba").focus());
        assertEquals(StoryCompanionPose.LOOK_UP, AdventureHud.Prompt.fromBeat("ong-cloud", "Mây").pose());
        assertEquals("ONG", AdventureHud.Prompt.fromBeat("ong-cloud", "Mây").focus());
        assertEquals(StoryCompanionPose.PET, AdventureHud.Prompt.fromBeat("both-rabbit", "Thỏ").pose());
        assertEquals("BOTH", AdventureHud.Prompt.fromBeat("both-rabbit", "Thỏ").focus());
        assertEquals(StoryCompanionPose.WAVE, StoryCompanionPose.fromBeat(null));
        assertEquals(StoryCompanionPose.WAVE, StoryCompanionPose.fromBeat("sau-safe"));
    }
}
