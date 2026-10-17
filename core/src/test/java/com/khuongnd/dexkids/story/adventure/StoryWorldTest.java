package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.game.*;
import com.khuongnd.dexkids.story.VisualCue;
import com.khuongnd.dexkids.story.VisualCue.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisode.*;

class StoryWorldTest {
    private StoryEpisode first(StoryEpisode e,Beat beat) {
        var scenes=new ArrayList<>(e.scenes());var scene=scenes.get(0);var beats=new ArrayList<>(scene.beats());
        beats.set(0,beat);scenes.set(0,new Scene(scene.id(),scene.backdrop(),beats,scene.question()));
        return new StoryEpisode(e.id(),e.schemaVersion(),e.revision(),e.title(),e.audience(),e.sauAge(),e.ongAge(),e.educationalGoal(),e.synopsis(),scenes);
    }
    @Test void showcaseRoundtripsAllAudiencesAndReachesEntireCatalog() {
        var props=EnumSet.noneOf(Prop.class);var actions=EnumSet.noneOf(Action.class);var emotions=EnumSet.noneOf(Emotion.class);var themes=EnumSet.noneOf(Theme.class);
        for(Audience audience:Audience.values()) {
            var e=StoryWorldFixtures.showcase(audience);assertEquals(e,StoryEpisodeCodec.parse(StoryEpisodeCodec.toJson(e)));
            for(var scene:e.scenes()) {themes.add(scene.backdrop());for(var beat:scene.beats()) {
                props.add(beat.prop());actions.add(beat.action());emotions.add(beat.emotion());
                for(var support:beat.supportingProps())props.add(support.prop());
            }}
        }
        assertEquals(60,StoryWorldCatalog.objects().size());
        assertTrue(props.containsAll(StoryWorldCatalog.objects().stream().map(StoryWorldCatalog.ObjectSpec::prop).toList()));
        assertEquals(EnumSet.allOf(Action.class),actions);assertEquals(EnumSet.allOf(Emotion.class),emotions);
        assertEquals(8,themes.size());
    }
    @Test void suppliedWorldPackIsTheValidatedThreeAudienceShowcase() throws Exception {
        var pack=StoryPackCodec.parse(java.nio.file.Files.readAllBytes(java.nio.file.Path.of("../docs/story-source/stories.world.example.json")));
        assertEquals(3,pack.size());
        for(Audience a:Audience.values())assertEquals(StoryWorldFixtures.showcase(a),pack.stream().filter(e->e.audience()==a).findFirst().orElseThrow());
    }
    @Test void v1SerializationOmitsAllNewFieldsAndRoundtripsUnchanged() {
        Map<Audience,String> expected=Map.of(Audience.SAU,"8b98b0199038de54814ff79fb7a216d2bb7d5b8a7b9aac0391c121f135e1e883",
            Audience.ONG,"16474f4d1bba93c31fb3b0e598d0184961107868976031d2155b0b1232151d05",
            Audience.BOTH,"1e872ce8c56f718e5329a92a40f9418a175520678f64d664de9952bbc10f518b");
        for(Audience a:Audience.values()) {
            var e=StoryFixtures.educational(a);var json=StoryEpisodeCodec.toJson(e);
            assertEquals(expected.get(a),StoryPackCodec.digest(json));
            assertFalse(json.contains("supportingProps"));assertEquals(json,StoryEpisodeCodec.toJson(StoryEpisodeCodec.parse(json)));
            assertEquals(e,StoryEpisodeCodec.parse(json));
        }
    }
    @Test void schemaV1RejectsNewVocabularyAndSupportingFieldEvenWhenEmpty() {
        var e=StoryWorldFixtures.showcase(Audience.BOTH);String json=StoryEpisodeCodec.toJson(e);
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(json.replace("\"schemaVersion\":2","\"schemaVersion\":1")));
        var legacy=StoryEpisodeCodec.toJson(StoryFixtures.educational(Audience.BOTH));
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(legacy.replaceFirst("\"actor\":","\"supportingProps\":[],\"actor\":")));
    }
    @Test void objectsMustUseUniqueBoundedSlotsAndKnownNames() {
        var e=StoryWorldFixtures.showcase(Audience.BOTH);var b=e.scenes().get(0).beats().get(0);
        for(var bad:List.of(List.of(new SupportingProp(Prop.NONE,ObjectSlot.LEFT)),
            List.of(new SupportingProp(Prop.BOOK,ObjectSlot.LEFT),new SupportingProp(Prop.CUP,ObjectSlot.LEFT)))) {
            assertThrows(IllegalArgumentException.class,()->StoryEpisodeValidator.validate(first(e,new Beat(b.actor(),b.utterance(),b.emotion(),b.action(),b.prop(),bad))));
        }
        String json=StoryEpisodeCodec.toJson(e);
        for(String bad:List.of(json.replace("BACK_LEFT","ABOVE_FACE"),json.replace("\"TABLE\"","\"EXECUTE\""),json.replace("\"slot\":","\"x\":10,\"slot\":")))
            assertThrows(IllegalArgumentException.class,()->StoryEpisodeCodec.parse(bad));
        var six=new ArrayList<SupportingProp>(b.supportingProps());six.add(new SupportingProp(Prop.BOOK,ObjectSlot.LEFT));
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeValidator.validate(first(e,new Beat(b.actor(),b.utterance(),b.emotion(),b.action(),b.prop(),six))));
    }
    @Test void countingCannotSmuggleVisibleQuantityThroughSupports() {
        var e=StoryWorldFixtures.showcase(Audience.BOTH);var b=e.scenes().get(0).beats().get(0);
        assertThrows(IllegalArgumentException.class,()->StoryEpisodeValidator.validate(first(e,new Beat(b.actor(),b.utterance(),b.emotion(),Action.COUNT,Prop.NONE,b.supportingProps()))));
    }
    @Test void actionTargetsHaveRealPhysicalRoles() {
        for(Action a:List.of(Action.PICK_UP,Action.PUT_DOWN,Action.CARRY)) {
            assertTrue(StoryActionCatalog.compatible(a,Prop.TEDDY_BEAR));assertFalse(StoryActionCatalog.compatible(a,Prop.HOUSE));assertFalse(StoryActionCatalog.compatible(a,Prop.NONE));
        }
        assertTrue(StoryActionCatalog.compatible(Action.EAT,Prop.BANANA));assertFalse(StoryActionCatalog.compatible(Action.EAT,Prop.ROCK));
        assertTrue(StoryActionCatalog.compatible(Action.DRINK,Prop.CUP));assertFalse(StoryActionCatalog.compatible(Action.DRINK,Prop.BANANA));
        assertTrue(StoryActionCatalog.compatible(Action.DRAW,Prop.NOTEBOOK));assertFalse(StoryActionCatalog.compatible(Action.DRAW,Prop.PENCIL));
    }
    @Test void compilerPreservesImmutableAtomicWorldMetadata() {
        var e=StoryWorldFixtures.showcase(Audience.BOTH);var b=e.scenes().get(0).beats().get(0);var v=new StorySceneCompiler().compile(e,0,0);
        assertTrue(v.world());assertEquals(b.actor(),v.actor());assertEquals(b.emotion(),v.emotion());assertEquals(b.supportingProps(),v.supportingProps());
        assertThrows(UnsupportedOperationException.class,()->v.supportingProps().clear());
    }
    @Test void recallKeepsBackdropButClearsNarrativeObjectsAndGestures() {
        var d=new StoryPlaybackDirector();var e=StoryWorldFixtures.showcase(Audience.BOTH);d.start(e);
        int questions=0;
        for(int i=0;i<200;i++) {
            var event=d.next();
            if(event instanceof StoryPlaybackDirector.Speech speech) {
                if(speech.kind()!=StoryPlaybackDirector.Kind.NARRATION) {
                    assertTrue(speech.visual().world());assertEquals(Prop.NONE,speech.visual().cue().prop());assertTrue(speech.visual().supportingProps().isEmpty());
                    assertEquals(Action.IDLE,speech.visual().cue().action());assertEquals(e.scenes().get(speech.sceneIndex()).backdrop(),speech.visual().cue().theme());
                    if(speech.kind()==StoryPlaybackDirector.Kind.QUESTION)questions++;
                }d.speechCompleted(speech.token());
            }else if(event instanceof StoryPlaybackDirector.Listen)d.tickListening(5);
            else if(event instanceof StoryPlaybackDirector.Finished)break;
        }
        assertEquals(3,questions);assertEquals(StoryPlaybackDirector.State.COMPLETE,d.state());
    }
    @Test void badV2PackDoesNotParsePartially() {
        String json=StoryEpisodeCodec.toJson(StoryWorldFixtures.showcase(Audience.BOTH));
        assertThrows(IllegalArgumentException.class,()->StoryPackCodec.parse(("{\"schemaVersion\":1,\"stories\":["+json+","+json.replace("BACK_LEFT","BAD")+"]}").getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
    @Test void bundledCatalogFitsExistingAndroidGatewayPromptBudget() {
        // Android gateway caps system prompts at 6000 chars; leave 1800 for author/review instructions.
        assertTrue(StoryActionCatalog.promptCatalog().length()<=4200,"Catalog exceeds the existing gateway budget");
    }
    @Test void supportSlotsReserveCaptionAndBottomRightStatusAreas() {
        for(ObjectSlot slot:ObjectSlot.values())for(var object:StoryWorldCatalog.objects()) {
            var b=StoryWorldLayout.support(slot,object.prop());
            assertFalse(b.x()<1890 && b.x()+b.width()>1470 && b.y()<180,"Status card would hide "+object.prop());
            assertFalse(b.x()<1105 && b.y()+b.height()>750,"Caption card would hide "+object.prop());
        }
    }
    @Test void singleAudienceForegroundDoesNotOccupyTheHeroLane() {
        for(Audience audience:List.of(Audience.SAU,Audience.ONG))for(Prop prop:List.of(Prop.BLANKET,Prop.TEDDY_BEAR,Prop.BOOK)) {
            var box=StoryWorldLayout.support(ObjectSlot.FOREGROUND,prop,audience);
            assertTrue(box.x()>1200,"Foreground overlaps single hero at x1100");
        }
    }
    @Test void physicalScaleSeparatesHandheldObjectsFurnitureAndAnimals() {
        float h=StoryStageGeometry.CHILD_HEIGHT;
        assertTrue(StoryStageGeometry.size(Prop.CUP,h).height()<h*.14f);
        assertTrue(StoryStageGeometry.size(Prop.BOOK,h).width()>StoryStageGeometry.size(Prop.CUP,h).width()*2);
        assertTrue(StoryStageGeometry.size(Prop.CHAIR,h).height()>h*.5f);
        assertTrue(StoryStageGeometry.size(Prop.TABLE,h).width()>h*.8f);
        assertTrue(StoryStageGeometry.size(Prop.ELEPHANT,h).height()>h);
        assertTrue(StoryStageGeometry.size(Prop.SNAIL,h).height()<h*.1f);
        for(Prop prop:Prop.values())if(prop!=Prop.NONE) {
            var size=StoryStageGeometry.size(prop,h);assertTrue(size.width()>0&&size.height()>0,prop.name());
        }
    }
    @Test void twoBoneGripReachesActualTargetWithMirroredElbows() {
        for(boolean left:new boolean[]{true,false})for(float dy:new float[]{-.20f,-.10f,.08f}) {
            float dx=left?.04f:-.04f;
            var a=StoryStageGeometry.reach(dx,dy,.135f,.15f,left);
            var elbow=StoryStageGeometry.endpoint(0,0,.135f,a.upper());
            var hand=StoryStageGeometry.endpoint(elbow.x(),elbow.y(),.15f,a.upper()+a.fore());
            assertEquals(dx,hand.x(),.0001f);assertEquals(dy,hand.y(),.0001f);
            assertTrue(left?a.fore()>0:a.fore()<0);
        }
        var outside=StoryStageGeometry.reach(9,9,.135f,.15f,false);
        assertTrue(Float.isFinite(outside.upper())&&Float.isFinite(outside.fore()));
    }
    @Test void everyJointIsFiniteBoundedAndPausedSamplingIsStable() {
        for(Action action:Action.values())for(float t:new float[]{-1,0,.5f,1,2,3.9f,99999,Float.NaN,Float.POSITIVE_INFINITY}) {
            var p=StoryRigMotion.sample(action,t);assertSame(p,StoryRigMotion.sample(action,t));
            for(float v:new float[]{p.leftUpper(),p.leftFore(),p.rightUpper(),p.rightFore(),p.leftLeg(),p.rightLeg(),p.head(),p.body()})assertTrue(Float.isFinite(v)&&Math.abs(v)<=180);
            assertTrue(Math.abs(p.x())<=40);assertTrue(Math.abs(p.y())<=40);
        }
        assertTrue(StoryWorldRenderer.MAX_LIVE_TEXTURE_BYTES<=32L*1024*1024);
        for(ObjectSlot slot:ObjectSlot.values())for(var object:StoryWorldCatalog.objects()) {
            var box=StoryWorldLayout.support(slot,object.prop());assertTrue(box.x()>=0&&box.y()>=0&&box.x()+box.width()<=1920&&box.y()+box.height()<=1080);
        }
    }
}
