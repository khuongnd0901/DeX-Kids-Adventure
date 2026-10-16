package com.khuongnd.dexkids.story.adventure;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.khuongnd.dexkids.story.VisualCue;
import java.util.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisode.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisodeValidator.require;

/** Bounded versioned JSON, rejecting unknown fields, duplicate keys and implicit coercions. */
public final class StoryEpisodeCodec {
    public static final int MAX_JSON_CHARS=48000;
    private StoryEpisodeCodec() {}
    public static StoryEpisode parse(String json) {
        require(json!=null && json.length()<=MAX_JSON_CHARS,"JSON_SIZE_INVALID");
        // Bound parser recursion before invoking the general-purpose JSON reader.
        int depth=0; boolean quoted=false,escape=false;
        for(char c:json.toCharArray()) {
            if(escape){escape=false;continue;}
            if(c=='\\' && quoted){escape=true;continue;}
            if(c=='"'){quoted=!quoted;continue;}
            if(!quoted){if(c=='{'||c=='[')require(++depth<=12,"JSON_DEPTH_INVALID");else if(c=='}'||c==']')require(--depth>=0,"JSON_INVALID");}
        }
        require(depth==0 && !quoted,"JSON_INVALID");
        try {
            new Grammar(json).check();
            JsonValue root=new JsonReader().parse(json);
            fields(root,"id","schemaVersion","revision","title","audience","sauAge","ongAge","educationalGoal","synopsis","scenes");
            List<Scene> scenes=new ArrayList<>();
            for(JsonValue s:array(root,"scenes",8)) {
                fields(s,"id","backdrop","beats","question");
                List<Beat> beats=new ArrayList<>();
                for(JsonValue b:array(s,"beats",6)) {
                    fields(b,"actor","utterance","emotion","action","prop");
                    beats.add(new Beat(Actor.valueOf(str(b,"actor")),str(b,"utterance"),Emotion.valueOf(str(b,"emotion")),VisualCue.Action.valueOf(str(b,"action")),VisualCue.Prop.valueOf(str(b,"prop"))));
                }
                Question question=null; JsonValue q=s.get("question");
                if(q!=null && !q.isNull()) {
                    fields(q,"target","prompt","acceptedConcepts","hint","feedback","silenceFallback","maxWaitSeconds");
                    List<String> concepts=new ArrayList<>();
                    for(JsonValue c:array(q,"acceptedConcepts",4)){require(c.isString(),"JSON_STRING_REQUIRED");concepts.add(c.asString());}
                    question=new Question(Audience.valueOf(str(q,"target")),str(q,"prompt"),concepts,str(q,"hint"),str(q,"feedback"),str(q,"silenceFallback"),integer(q,"maxWaitSeconds"));
                }
                scenes.add(new Scene(str(s,"id"),VisualCue.Theme.valueOf(str(s,"backdrop")),beats,question));
            }
            StoryEpisode episode=new StoryEpisode(str(root,"id"),integer(root,"schemaVersion"),integer(root,"revision"),str(root,"title"),Audience.valueOf(str(root,"audience")),integer(root,"sauAge"),integer(root,"ongAge"),str(root,"educationalGoal"),str(root,"synopsis"),scenes);
            StoryEpisodeValidator.validate(episode); return episode;
        } catch(RuntimeException failure) { throw new IllegalArgumentException("STORY_JSON_REJECTED",failure); }
    }
    public static String toJson(StoryEpisode e) {
        StoryEpisodeValidator.validate(e);
        JsonValue root=obj(); put(root,"id",e.id());put(root,"schemaVersion",e.schemaVersion());put(root,"revision",e.revision());put(root,"title",e.title());put(root,"audience",e.audience().name());put(root,"sauAge",e.sauAge());put(root,"ongAge",e.ongAge());put(root,"educationalGoal",e.educationalGoal());put(root,"synopsis",e.synopsis());
        JsonValue scenes=arr();root.addChild("scenes",scenes);
        for(Scene s:e.scenes()) {
            JsonValue scene=obj();scenes.addChild(scene);put(scene,"id",s.id());put(scene,"backdrop",s.backdrop().name());
            JsonValue beats=arr();scene.addChild("beats",beats);
            for(Beat b:s.beats()){JsonValue beat=obj();beats.addChild(beat);put(beat,"actor",b.actor().name());put(beat,"utterance",b.utterance());put(beat,"emotion",b.emotion().name());put(beat,"action",b.action().name());put(beat,"prop",b.prop().name());}
            Question q=s.question();
            if(q!=null){JsonValue question=obj();scene.addChild("question",question);put(question,"target",q.target().name());put(question,"prompt",q.prompt());JsonValue concepts=arr();question.addChild("acceptedConcepts",concepts);for(String c:q.acceptedConcepts())concepts.addChild(new JsonValue(c));put(question,"hint",q.hint());put(question,"feedback",q.feedback());put(question,"silenceFallback",q.silenceFallback());put(question,"maxWaitSeconds",q.maxWaitSeconds());}
        }
        String encoded=root.toJson(JsonWriter.OutputType.json);require(encoded.length()<=MAX_JSON_CHARS,"JSON_SIZE_INVALID");return encoded;
    }
    private static void fields(JsonValue v,String...allowed) {
        require(v!=null && v.isObject(),"JSON_OBJECT_REQUIRED");Set<String> seen=new HashSet<>(), keys=Set.of(allowed);
        for(JsonValue c:v)require(keys.contains(c.name) && seen.add(c.name),"JSON_UNKNOWN_OR_DUPLICATE_FIELD");
    }
    private static String str(JsonValue v,String key){JsonValue c=v.get(key);require(c!=null&&c.isString(),"JSON_STRING_REQUIRED");return c.asString();}
    private static int integer(JsonValue v,String key){JsonValue c=v.get(key);require(c!=null&&c.isLong()&&c.asLong()>=Integer.MIN_VALUE&&c.asLong()<=Integer.MAX_VALUE,"JSON_INTEGER_REQUIRED");return c.asInt();}
    private static JsonValue array(JsonValue v,String key,int max){JsonValue a=v.get(key);require(a!=null&&a.isArray()&&a.size<=max,"JSON_ARRAY_INVALID");return a;}
    private static JsonValue obj(){return new JsonValue(JsonValue.ValueType.object);}
    private static JsonValue arr(){return new JsonValue(JsonValue.ValueType.array);}
    private static void put(JsonValue v,String key,String value){v.addChild(key,new JsonValue(value));}
    private static void put(JsonValue v,String key,int value){v.addChild(key,new JsonValue(value));}
    /** JsonReader accepts JavaScript-like syntax; require actual RFC JSON before decoding. */
    static final class Grammar {
        final String text; int at;
        Grammar(String text){this.text=text;}
        void check(){value();space();require(at==text.length(),"JSON_TRAILING_CONTENT");}
        void space(){while(at<text.length() && " \t\r\n".indexOf(text.charAt(at))>=0)at++;}
        char peek(){space();return at<text.length()?text.charAt(at):'\0';}
        boolean take(char c){if(peek()==c){at++;return true;}return false;}
        void expect(char c){require(take(c),"JSON_SYNTAX_INVALID");}
        void value(){
            char c=peek();
            if(c=='{'){at++;if(take('}'))return;do{string();expect(':');value();}while(take(','));expect('}');}
            else if(c=='['){at++;if(take(']'))return;do{value();}while(take(','));expect(']');}
            else if(c=='"')string();
            else if(c=='t')literal("true");else if(c=='f')literal("false");else if(c=='n')literal("null");
            else number();
        }
        void literal(String word){require(text.startsWith(word,at),"JSON_LITERAL_INVALID");at+=word.length();}
        void string(){
            expect('"');boolean ended=false;
            while(at<text.length()){
                char c=text.charAt(at++);if(c=='"'){ended=true;break;}
                require(c>=32,"JSON_CONTROL_INVALID");
                if(c=='\\'){
                    require(at<text.length(),"JSON_ESCAPE_INVALID");char escaped=text.charAt(at++);
                    if(escaped=='u'){for(int n=0;n<4;n++){require(at<text.length()&&Character.digit(text.charAt(at++),16)>=0,"JSON_ESCAPE_INVALID");}}
                    else require("\"\\/bfnrt".indexOf(escaped)>=0,"JSON_ESCAPE_INVALID");
                }
            }
            require(ended,"JSON_STRING_UNCLOSED");
        }
        void number(){
            space();int start=at;if(at<text.length()&&text.charAt(at)=='-')at++;
            require(at<text.length(),"JSON_NUMBER_INVALID");
            if(text.charAt(at)=='0')at++;
            else{require(text.charAt(at)>='1'&&text.charAt(at)<='9',"JSON_NUMBER_INVALID");digits();}
            if(at<text.length()&&text.charAt(at)=='.'){at++;int begin=at;digits();require(at>begin,"JSON_NUMBER_INVALID");}
            if(at<text.length()&&(text.charAt(at)=='e'||text.charAt(at)=='E')){at++;if(at<text.length()&&(text.charAt(at)=='+'||text.charAt(at)=='-'))at++;int begin=at;digits();require(at>begin,"JSON_NUMBER_INVALID");}
            require(at>start,"JSON_NUMBER_INVALID");
        }
        void digits(){while(at<text.length()&&text.charAt(at)>='0'&&text.charAt(at)<='9')at++;}
    }

}
