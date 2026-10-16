package com.khuongnd.dexkids.story.adventure;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Local authoring helper: never connects to a server. */
public final class StoryPackTool {
    public static void main(String[] args) throws Exception {
        if(args.length==2 && (args[0].equals("--sample") || args[0].equals("--expanded-sample"))) {
            var stories=Arrays.stream(StoryEpisode.Audience.values()).map(a->StoryEpisodeCodec.toJson(args[0].equals("--expanded-sample")?StoryFixtures.expanded(a):StoryFixtures.educational(a))).toList();
            String pack="{\"schemaVersion\":1,\"stories\":["+String.join(",",stories)+"]}\n";
            StoryPackCodec.parse(pack.getBytes(StandardCharsets.UTF_8));Files.writeString(Path.of(args[1]),new com.badlogic.gdx.utils.JsonReader().parse(pack).prettyPrint(com.badlogic.gdx.utils.JsonWriter.OutputType.json,80)+"\n",StandardCharsets.UTF_8);
            System.out.println("Wrote three authored example stories: "+args[1]);return;
        }
        if(args.length!=1)throw new IllegalArgumentException("Usage: StoryPackTool <pack.json> | --sample <output.json>");
        var path=Path.of(args[0]);
        if(Files.size(path)>StoryPackCodec.MAX_BYTES)throw new IllegalArgumentException("PACK_SIZE_INVALID");
        System.out.println("Valid pack: "+StoryPackCodec.parse(Files.readAllBytes(path)).size()+" stories");
    }
}
