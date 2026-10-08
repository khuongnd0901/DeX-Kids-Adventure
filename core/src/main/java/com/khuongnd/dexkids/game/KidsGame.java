
package com.khuongnd.dexkids.game;

import com.badlogic.gdx.Game;
import com.khuongnd.dexkids.journey.DemoJourneyFeed;
import com.khuongnd.dexkids.journey.JourneyFeed;

public final class KidsGame extends Game {
    private final JourneyFeed journey;

    public KidsGame() {
        this(new DemoJourneyFeed());
    }

    public KidsGame(JourneyFeed journey) {
        this.journey = journey;
    }

    @Override public void create() {
        setScreen(new AdventureScreen(journey));
    }
}
