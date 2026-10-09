package com.khuongnd.dexkids.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChildAnswerInterpreterTest {
    @Test void respondsContextuallyToVietnameseAnswers() {
        assertEquals(ChildAnswerInterpreter.Kind.YES,
                ChildAnswerInterpreter.quiz("Dạ có ạ","Có, nhiều tảng đá rất lớn!").kind());
        assertEquals(ChildAnswerInterpreter.Kind.RELEVANT,
                ChildAnswerInterpreter.quiz("Nước từ trên xuống","Từ cao xuống thấp đấy!").kind());
        assertEquals(ChildAnswerInterpreter.Kind.UNSURE,
                ChildAnswerInterpreter.quiz("Con không biết","Bằng gạch đấy!").kind());
        assertTrue(ChildAnswerInterpreter.quiz("không","Có, nhiều tảng đá rất lớn!")
                .textVi().contains("Đáp án"));
        assertEquals(ChildAnswerInterpreter.Kind.RELEVANT,
                ChildAnswerInterpreter.chat("Con thích biển xanh").kind());
        assertEquals(ChildAnswerInterpreter.Kind.RELEVANT,
                ChildAnswerInterpreter.chat("Con thích hoa").kind());
        assertEquals(ChildAnswerInterpreter.Kind.UNKNOWN,
                ChildAnswerInterpreter.chat("").kind());
    }
    @Test void noRawChildPersonalDataIsEchoedOrSaved() {
        String privateWords = "Tên con là abcxyz 123456789";
        var a = ChildAnswerInterpreter.chat(privateWords);
        var b = ChildAnswerInterpreter.quiz(privateWords, "Bằng gạch đấy!");
        assertFalse(a.textVi().contains("abcxyz"));
        assertFalse(b.textVi().contains("123456789"));
        assertTrue(ChildAnswerInterpreter.norm("Đá ở trên").contains("da o tren"));
        assertTrue(ChildAnswerInterpreter.norm("A".repeat(600)).length() <= 160);
        assertEquals(ChildAnswerInterpreter.Kind.UNKNOWN,
                ChildAnswerInterpreter.quiz(null,"Có").kind());
    }
}
