package com.khuongnd.dexkids.story.adventure;

import com.khuongnd.dexkids.story.VisualCue.*;
import java.util.*;

/** Finite, extensible bundled vocabulary. Names and physical roles are not inferred at playback. */
public final class StoryWorldCatalog {
    public enum Kind { LARGE, PORTABLE, DRINK, FOOD, SKY, GROUND, ANIMAL }
    public record ObjectSpec(Prop prop,String labelVi,String topic,Kind kind) {
        public String region() {return prop.name().toLowerCase(Locale.ROOT);}
        public boolean portable(){return kind==Kind.PORTABLE || kind==Kind.DRINK || kind==Kind.FOOD;}
    }
    private static final Map<Prop,ObjectSpec> OBJECTS;
    static {
        EnumMap<Prop,ObjectSpec> map=new EnumMap<>(Prop.class);
        String rows="""
TABLE|Bàn|HOME|LARGE
CHAIR|Ghế|HOME|LARGE
BED|Giường|HOME|LARGE
PILLOW|Gối|HOME|PORTABLE
BLANKET|Chăn|HOME|PORTABLE
LAMP|Đèn bàn|HOME|PORTABLE
CUP|Cốc|PERSONAL|DRINK
BOWL|Bát|PERSONAL|PORTABLE
SPOON|Thìa|PERSONAL|PORTABLE
WATER_BOTTLE|Bình nước|PERSONAL|DRINK
TOOTHBRUSH|Bàn chải|PERSONAL|PORTABLE
TOWEL|Khăn|PERSONAL|PORTABLE
PENCIL|Bút chì|SCHOOL|PORTABLE
ERASER|Tẩy|SCHOOL|PORTABLE
RULER|Thước|SCHOOL|PORTABLE
NOTEBOOK|Vở|SCHOOL|PORTABLE
BACKPACK|Ba lô|SCHOOL|PORTABLE
CRAYONS|Bút sáp|SCHOOL|PORTABLE
BANANA|Chuối|FOOD|FOOD
ORANGE|Cam|FOOD|FOOD
CARROT|Cà rốt|FOOD|FOOD
BREAD|Bánh mì|FOOD|FOOD
RICE|Cơm|FOOD|FOOD
MILK|Sữa|FOOD|DRINK
TEDDY_BEAR|Gấu bông|TOYS|PORTABLE
BLOCKS|Khối xếp hình|TOYS|PORTABLE
PUZZLE|Tranh ghép|TOYS|PORTABLE
DOLL|Búp bê|TOYS|PORTABLE
TOY_TRAIN|Tàu đồ chơi|TOYS|PORTABLE
BUBBLES|Bong bóng xà phòng|TOYS|SKY
BICYCLE|Xe đạp|SPORT|LARGE
HELMET|Mũ bảo hiểm|SPORT|PORTABLE
JUMP_ROPE|Dây nhảy|SPORT|PORTABLE
SCOOTER|Xe scooter|SPORT|LARGE
HOOP|Vòng thể dục|SPORT|PORTABLE
GOAL|Khung thành|SPORT|LARGE
SUN|Mặt trời|NATURE|SKY
MOON|Mặt trăng|NATURE|SKY
RAIN|Mưa|NATURE|SKY
RAINBOW|Cầu vồng|NATURE|SKY
MUSHROOM|Nấm|NATURE|GROUND
ROCK|Đá|NATURE|PORTABLE
DOG|Chó|ANIMALS|ANIMAL
TURTLE|Rùa|ANIMALS|ANIMAL
ELEPHANT|Voi|ANIMALS|ANIMAL
MONKEY|Khỉ|ANIMALS|ANIMAL
FISH|Cá|ANIMALS|ANIMAL
SNAIL|Ốc sên|ANIMALS|ANIMAL
HOUSE|Ngôi nhà|PLACES|LARGE
SCHOOL|Trường học|PLACES|LARGE
BRIDGE|Cầu|PLACES|LARGE
BENCH|Ghế công viên|PLACES|LARGE
BIN|Thùng rác|PLACES|LARGE
RECYCLE_BIN|Thùng tái chế|PLACES|LARGE
CLOCK|Đồng hồ|SCIENCE|PORTABLE
COMPASS|La bàn|SCIENCE|PORTABLE
MAGNET|Nam châm|SCIENCE|PORTABLE
GLOBE|Quả địa cầu|SCIENCE|PORTABLE
TELESCOPE|Kính thiên văn|SCIENCE|LARGE
THERMOMETER|Nhiệt kế|SCIENCE|PORTABLE
            """;
        for(String row:rows.strip().split("\\n")) {
            String[] c=row.strip().split("\\|");
            Prop prop=Prop.valueOf(c[0]);
            if(map.put(prop,new ObjectSpec(prop,c[1],c[2],Kind.valueOf(c[3])))!=null)
                throw new IllegalStateException("Duplicate story world object");
        }
        OBJECTS=Collections.unmodifiableMap(map);
    }
    private StoryWorldCatalog(){}
    public static Collection<ObjectSpec> objects(){return OBJECTS.values();}
    public static ObjectSpec object(Prop prop){return OBJECTS.get(prop);}
    public static boolean isNew(Prop prop){return OBJECTS.containsKey(prop);}
    public static boolean portable(Prop prop) {
        var object=object(prop);if(object!=null)return object.portable();
        return Set.of(Prop.BOOK,Prop.BALL,Prop.GIFT,Prop.WATERING_CAN,Prop.APPLE,Prop.BASKET,
            Prop.STAR,Prop.LEAF,Prop.SHELL,Prop.UMBRELLA,Prop.MAGNIFYING_GLASS,Prop.FLOWER).contains(prop);
    }
    public static boolean newAction(Action a){return a.ordinal()>=Action.IDLE.ordinal();}
    public static boolean newTheme(Theme t){return t.ordinal()>=Theme.HOME.ordinal();}
    public static String promptCatalog() {
        return " World capabilities v3: emotions="+Arrays.toString(StoryEpisode.Emotion.values())+
            "; new portable props="+OBJECTS.values().stream().filter(ObjectSpec::portable).map(ObjectSpec::prop).toList()+". Episode schemaVersion=2 enables supportingProps: "+
            "optional array of at most five {prop,slot} objects. Slots BACK_LEFT,BACK_RIGHT,LEFT,RIGHT,FOREGROUND, "+
            "each used once. One primary prop plus up to five supports; no NONE in supports. "+
            "COUNT uses NONE and no supports: verbal counting only. PICK_UP,PUT_DOWN,CARRY require portable objects. "+
            "DRAW requires NOTEBOOK (pencil is supplied by renderer), EAT requires APPLE,BANANA,ORANGE,CARROT,BREAD,RICE, "+
            "DRINK requires CUP,WATER_BOTTLE,MILK; SIT requires CHAIR,BENCH or NONE. "+
            "READ,WATER,INSPECT keep their existing targets. IDLE,TALK,WALK,RUN,THINK,POINT,NOD,SHAKE_HEAD,CLAP "+
            "are character gestures. New HOME,CLASSROOM,PLAYGROUND,GARDEN,FOREST,BEACH,MARKET,LIBRARY backdrops are fictional. "+
            "Use schemaVersion=2 for any new vocabulary; v1 remains for legacy stories. All actions happen inside stories.";
    }
}
