import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.khuongnd.dexkids.game.*;
import com.khuongnd.dexkids.geo.*;
import com.khuongnd.dexkids.journey.DemoJourneyFeed;
import java.nio.file.*;

/** Explicit host-only visual fixture using sourced catalogue entries and the actual GL renderer. */
public class DesktopSceneVisualQa {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("POI BEAT OUTPUT");
        OfflinePoiCatalog catalogue;
        try (var in = Files.newInputStream(Path.of("assets/poi/live-landmarks.tsv"))) {
            catalogue = OfflinePoiCatalog.parse(in);
        }
        var entry = catalogue.entries().stream().filter(e -> e.poi().id().equals(args[0])).findFirst().orElseThrow();
        var game = new KidsGame(new DemoJourneyFeed(), 110, args[2], false, 4);
        game.showEntertainmentPrompt(args[1], "Illustration QA / sourced fixture / simulation");
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Explicit illustration QA / NOT real GPS");
        config.setWindowedMode(1920,1080);
        config.setForegroundFPS(30);
        new Lwjgl3Application(new ApplicationAdapter() {
            public void create() {
                game.create();
                try {
                    var field = AdventureScreen.class.getDeclaredField("sceneDirector");
                    field.setAccessible(true);
                    var director = (PoiSceneDirector) field.get(game.getScreen());
                    if (!director.onNotice(new OfflinePoiEngine.Notice(entry, OfflinePoiEngine.EventKind.NEARBY,0,1,true),0))
                        throw new AssertionError("Scene fixture not accepted");
                } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
            }
            public void render() { game.render(); }
            public void resize(int w,int h) { game.resize(w,h); }
            public void pause() { game.pause(); }
            public void resume() { game.resume(); }
            public void dispose() { game.dispose(); }
        },config);
    }
}
