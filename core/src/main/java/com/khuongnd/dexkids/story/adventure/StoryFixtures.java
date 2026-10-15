package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.story.VisualCue;
import java.util.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisode.*;

/** Locally authored fictional sample; not AI-reviewed or automatically approved. */
public final class StoryFixtures {
    private StoryFixtures() {}
    public static StoryEpisode educational(Audience audience) {
        Actor child=audience==Audience.ONG?Actor.ONG:Actor.SAU;
        String name=child==Actor.SAU?"Sâu":"Ong";
        List<Scene> scenes=new ArrayList<>();
        for(int i=0;i<4;i++) {
            List<Beat> beats=new ArrayList<>();
            String[] narration={"Trong câu chuyện tưởng tượng, các bạn cùng chào khu vườn.","Capybara ngước nhìn và nói: Có bạn chim trên cành cây!","Capybara nói: Bạn thỏ đang chờ được vuốt ve thật nhẹ nhàng.","Cuối ngày, các bạn vẫy tay chào khu vườn và cảm ơn nhau."};
            VisualCue.Action action=i==1?VisualCue.Action.LOOK_UP:i==2?VisualCue.Action.PET:VisualCue.Action.WAVE;
            VisualCue.Prop prop=i==1?VisualCue.Prop.BIRD:i==2?VisualCue.Prop.RABBIT:VisualCue.Prop.TREE;
            beats.add(new Beat(Actor.CAPYBARA,narration[i],Emotion.CALM,action,prop));
            String[] lines={" vẫy tay chào các bạn trong vườn."," ngước nhìn bạn chim và mỉm cười."," vuốt ve bạn thỏ thật nhẹ nhàng."," vẫy tay và nói: Cảm ơn các bạn đã cùng giúp đỡ nhau!"};
            beats.add(new Beat(child,name+lines[i],Emotion.HAPPY,action,prop));
            if(audience==Audience.BOTH)beats.add(new Beat(Actor.ONG,"Ong"+lines[i],Emotion.CURIOUS,action,prop));
            Audience target=audience==Audience.BOTH?(i==0?Audience.SAU:i==1?Audience.ONG:Audience.BOTH):audience;
            String[] prompts={"Mình chào các bạn trong vườn thế nào?","Bạn chim đang ở đâu?","Mình vuốt ve bạn thỏ thế nào?"};
            String[] answers={"vẫy tay","trên cây","nhẹ nhàng"};
            String[] hints={"Mình cùng giơ tay lên chào nhé.","Mình ngước nhìn cành cây nhé.","Mình cần thật nhẹ nhàng với bạn thỏ."};
            Question question=i<3?new Question(target,prompts[i],List.of(answers[i]),hints[i],"Cảm ơn con đã cùng quan sát và giúp bạn.",hints[i],12):null;
            scenes.add(new Scene("garden-"+(i+1),VisualCue.Theme.NATURE,beats,question));
        }
        StoryEpisode e=new StoryEpisode("garden-"+audience.name().toLowerCase(Locale.ROOT),1,1,"Khu vườn của những người bạn",audience,5,4,"Cùng giúp đỡ và chăm sóc cây","Một câu chuyện tưởng tượng về các bạn nhỏ cùng chăm khu vườn.",scenes);
        StoryEpisodeValidator.validate(e);return e;
    }
}
