package com.khuongnd.dexkids.qa;

import android.os.Bundle;
import com.khuongnd.dexkids.ai.CreativeBatchReview;

/** Android's real JSON parser, no provider requests. */
final class CreativeReviewValidation {
    static void run(Bundle result) {
        String valid="{\"verdicts\":[{\"id\":\"b1q0\",\"verdict\":\"ACCEPT\",\"code\":\"OK\"}]}";
        LifecycleValidationRunner.require(CreativeBatchReview.parse(valid).get(0).accepted(),"Explicit accept missing");
        for(String invalid:new String[]{"not json","{}","{\"verdicts\":[]}",
                valid.replace("ACCEPT","MAYBE"),valid.replace("OK","FACT_ERROR"),
                valid.replace("ACCEPT","REJECT"),valid.replace(",\"code\":\"OK\"","")}) {
            boolean rejected=false;
            try { CreativeBatchReview.parse(invalid); } catch(Exception expected){rejected=true;}
            LifecycleValidationRunner.require(rejected,"Malformed review accepted");
        }
        result.putString("scope","T-046 strict Android JSON verdict parsing; zero HTTP");
    }
}
