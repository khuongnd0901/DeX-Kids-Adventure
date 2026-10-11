package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TripQuestionPackTest {
    private OfflineLearningCatalog catalog() {
        StringBuilder csv = new StringBuilder("# dexkids-offline-learning-v1\nid\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish\n");
        String[] topics = {"sea", "city", "road", "nature", "weather", "animal", "garden", "safety"};
        for (int i=0; i<24; i++) {
            String topic=topics[i%topics.length];
            csv.append("quiz-").append(topic).append("-").append(i).append("\tQUIZ\t").append(topic)
               .append("\t4\t6\tCon thử nói điều số ").append(i).append(" có đúng không?\tMột câu trả lời được biên tập số ").append(i).append(".\t\n");
            csv.append("word-").append(topic).append("-").append(i).append("\tWORD\t").append(topic)
               .append("\t4\t6\tMình cùng học một từ tiếng Anh nhé!\tTừ tiếng Anh dành cho trẻ nhỏ nhé!\tword\n");
        }
        return OfflineLearningCatalog.parse(new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.UTF_8)));
    }
    private List<TripQuestionPack.Item> pack(int count, EntertainmentDirector.Audience mode) {
        String[] topics={"sea","city","road","nature","weather","animal","garden","safety"};
        List<TripQuestionPack.Item> out=new ArrayList<>();
        for(int i=0;i<count;i++){
            String id=topics[i%topics.length]+"-"+i;
            var focus=mode==EntertainmentDirector.Audience.BOTH ?
                switch(i%3){case 0 -> EntertainmentDirector.Audience.SAU;
                            case 1 -> EntertainmentDirector.Audience.ONG;
                            default -> EntertainmentDirector.Audience.BOTH;} : mode;
            out.add(new TripQuestionPack.Item("quiz-"+id,"word-"+id,focus));
        }
        return out;
    }
    @Test void acceptsAllAgeModesAndBounds() {
        for (var mode:EntertainmentDirector.Audience.values()) {
            assertEquals(15,TripQuestionPack.validate(pack(15,mode),catalog(),mode).items().size());
            assertEquals(20,TripQuestionPack.validate(pack(20,mode),catalog(),mode).items().size());
        }
    }
    @Test void rejectsBadSizeDuplicateFocusOrWord() {
        var cat=catalog();
        assertThrows(IllegalArgumentException.class,()->TripQuestionPack.validate(pack(14,EntertainmentDirector.Audience.ONG),cat,EntertainmentDirector.Audience.ONG));
        assertThrows(IllegalArgumentException.class,()->TripQuestionPack.validate(pack(21,EntertainmentDirector.Audience.ONG),cat,EntertainmentDirector.Audience.ONG));
        var repeated=pack(18,EntertainmentDirector.Audience.SAU);
        repeated.set(3,repeated.get(0));
        assertThrows(IllegalArgumentException.class,()->TripQuestionPack.validate(repeated,cat,EntertainmentDirector.Audience.SAU));
        var wrong=pack(18,EntertainmentDirector.Audience.BOTH);
        wrong.set(1,new TripQuestionPack.Item(wrong.get(1).quizId(),wrong.get(1).wordId(),EntertainmentDirector.Audience.SAU));
        assertThrows(IllegalArgumentException.class,()->TripQuestionPack.validate(wrong,cat,EntertainmentDirector.Audience.BOTH));
        var forged=pack(18,EntertainmentDirector.Audience.ONG);
        forged.set(0,new TripQuestionPack.Item("quiz-sea-999","word-sea-0",EntertainmentDirector.Audience.ONG));
        assertThrows(IllegalArgumentException.class,()->TripQuestionPack.validate(forged,cat,EntertainmentDirector.Audience.ONG));
    }
}
