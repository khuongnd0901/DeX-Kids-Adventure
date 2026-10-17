package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.story.VisualCue.*;
import java.util.*;
import static com.khuongnd.dexkids.story.adventure.StoryEpisode.*;

/** Authored local capability showcase, kept separate from the parent's canonical source. */
public final class StoryWorldFixtures {
    public static Prop target(Action action,int index) {
        return switch(action) {
            case LOOK_UP -> Prop.CLOUD;case PET -> Prop.DOG;case READ -> Prop.BOOK;case WATER -> Prop.SEEDLING;case SHARE -> Prop.GIFT;
            case INSPECT -> Prop.LEAF;case COUNT -> Prop.NONE;case SIT -> Prop.CHAIR;
            case DRAW -> Prop.NOTEBOOK;case EAT -> Prop.BANANA;case DRINK -> Prop.CUP;
            case PICK_UP,PUT_DOWN,CARRY -> Prop.TEDDY_BEAR;
            default -> StoryWorldCatalog.objects().stream().toList().get(Math.floorMod(index,60)).prop();
        };
    }
    public static StoryEpisode showcase(Audience audience) {
        var objects=new ArrayList<>(StoryWorldCatalog.objects());var scenes=new ArrayList<Scene>();
        Theme[] backdrops={Theme.HOME,Theme.CLASSROOM,Theme.PLAYGROUND,Theme.GARDEN,Theme.FOREST,Theme.BEACH,Theme.MARKET,Theme.LIBRARY};
        String[] places={"ngôi nhà ấm áp","lớp học vui vẻ","sân chơi","khu vườn","khu rừng","bãi biển","khu chợ","thư viện"};
        String[] verbs={"vẫy tay chào bạn","ngước nhìn vật ở phía trên","đếm bằng lời","vuốt ve bạn thú","nhảy một nhịp trong truyện","nhún nhảy vui vẻ","cúi chào nhẹ nhàng","đọc sách","tưới cây non","mời bạn nhận một món quà","dùng kính lúp ngắm chiếc lá","đứng nghỉ","trò chuyện với bạn","đi bộ tại chỗ","chạy tại chỗ trong truyện","ngồi nghỉ trên ghế","suy nghĩ một chút","chỉ tay giới thiệu","gật đầu","lắc đầu nhẹ nhàng","vỗ tay","nhặt gấu bông","đặt gấu bông xuống","mang gấu bông","vẽ vào vở","ăn một miếng chuối","uống nước từ cốc"};
        for(int scene=0;scene<8;scene++) {
            var beats=new ArrayList<Beat>();
            for(int j=0;j<6;j++) {
                int i=scene*6+j;Action action=Action.values()[i%Action.values().length];
                Actor actor=audience==Audience.BOTH?Actor.values()[i%3]:i%2==0?Actor.CAPYBARA:audience==Audience.SAU?Actor.SAU:Actor.ONG;
                Prop prop=target(action,i);
                if(action==Action.LOOK_UP)prop=Prop.CLOUD;if(action==Action.PET)prop=Prop.DOG;
                List<SupportingProp> supports=new ArrayList<>();
                if(action!=Action.COUNT)for(int k=0;k<5;k++)supports.add(new SupportingProp(objects.get((i*5+k)%60).prop(),ObjectSlot.values()[k]));
                String text=i==0?"Trong câu chuyện tưởng tượng, các bạn cùng khám phá ngôi nhà ấm áp.":
                    (actor==Actor.CAPYBARA?"Capybara":actor==Actor.SAU?"Sâu":"Ong")+" "+verbs[action.ordinal()]+" ở "+places[scene]+".";
                beats.add(new Beat(actor,text,Emotion.values()[i%Emotion.values().length],action,prop,supports));
            }
            Question question=null;
            if(scene>=5) {
                Audience target=audience==Audience.BOTH?Audience.values()[scene-5]:audience;
                question=new Question(target,"Trong truyện, các bạn đã cùng khám phá nơi nào?",List.of(places[scene]),"Con nhớ lại nơi vừa được kể nhé.","Cảm ơn con đã chia sẻ điều con nhớ.","Mình có thể nghe lại câu chuyện cùng nhau nhé.",5);
            }
            scenes.add(new Scene("world-scene-"+scene,backdrops[scene],beats,question));
        }
        var episode=new StoryEpisode("world-showcase-"+audience.name().toLowerCase(Locale.ROOT),2,1,"Một ngày khám phá thế giới quanh em",audience,5,4,
            "Khám phá đồ vật, cảm xúc và hành động quen thuộc.","Truyện minh họa khả năng hoạt cảnh mới qua tám không gian tưởng tượng.",scenes);
        StoryEpisodeValidator.validate(episode);return episode;
    }
    private StoryWorldFixtures(){}
}
