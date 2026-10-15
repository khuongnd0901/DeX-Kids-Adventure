package com.khuongnd.dexkids.qa;

import android.os.Bundle;
import com.khuongnd.dexkids.story.StoryAuthor;
import com.khuongnd.dexkids.story.adventure.StoryEpisode;
import java.text.Normalizer;

/** Parent input is rejected before fake transport; never dispatches a provider request. */
final class StoryAuthorSafetyValidation {
    static void run(LifecycleValidationRunner runner, Bundle result) {
        final int[] dispatches={0};
        StoryAuthor author=new StoryAuthor(runner.getTargetContext(),(system,input,review)->{
            dispatches[0]++;throw new AssertionError("Private parent input reached author transport");
        });
        String[] privateInputs={
            "địa chỉ nhà tôi Nguyễn Huệ", "dia chi nha toi Nguyen Hue",
            "SỐ ĐIỆN THOẠI CỦA TÔI", "so dien thoai cua toi",
            "Tên thật của con", "ten that cua con",
            "Mật khẩu của tôi", "mat khau cua toi",
            "Trường con học ở đây", "truong con hoc o day",
            "Tên đầy đủ của bé", "ten day du cua be",
            "Họ tên của bé", "ho ten cua be",
            "GPS của chuyến đi", "gps cua chuyen di",
            "dia-chi nha toi", "dia   chi nha toi",
            Normalizer.normalize("ĐỊA CHỈ nhà tôi",Normalizer.Form.NFD),
            "Tên con là Nguyễn Văn Bé", "ten con la Nguyen Van Be"
        };
        int rejected=0;
        for(String value:privateInputs) {
            for(boolean asGoal:new boolean[]{false,true}) {
                boolean denied=false;
                try {author.generate(asGoal?"Giúp bạn":value,asGoal?value:"Biết chia sẻ",StoryEpisode.Audience.BOTH);}
                catch(IllegalArgumentException expected){denied=true;}
                LifecycleValidationRunner.require(denied,"Private parent input accepted: "+rejected);
                rejected++;
            }
        }
        LifecycleValidationRunner.require(dispatches[0]==0,"Private input dispatched AI");
        result.putString("t054_parent_input_privacy","PASS: "+rejected+" accented/ASCII/uppercase/decomposed/punctuation/whitespace private topic and goal cases rejected before transport; zero provider calls");
    }
}
