package com.khuongnd.dexkids.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PassengerPoseBoundsTest {
    private String fixture() {
        StringBuilder out=new StringBuilder();
        for(int i=0;i<4;i++)out.append("sau\t").append(i).append("\t1024\t1536\t3\t17\t500\t740\n");
        return out.toString();
    }
    @Test public void preserves_cell_relative_alpha_crop() {
        var bounds=PassengerPoseBounds.parse(fixture(),"sau",1024,1536);
        for(int[] b:bounds)assertArrayEquals(new int[]{3,17,500,740},b);
    }
    @Test public void rejects_mismatched_dimensions_duplicate_missing_and_overflow() {
        for(String bad:new String[]{fixture().replace("1024","2048"),fixture()+fixture(),
                fixture().replace("sau\t3","ong\t3"),fixture().replace("500","2147483647"),
                fixture().replace("\t3\t17\t","\t-1\t17\t")}) {
            assertThrows(IllegalArgumentException.class,()->PassengerPoseBounds.parse(bad,"sau",1024,1536));
        }
    }
}
