package com.khuongnd.dexkids.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.text.Normalizer;

/** Informational game HUD, not touch controls. POI content is labeled as estimated. */
final class AdventureHud {
    record Prompt(String topic, String text) {
        static Prompt fromBeat(String id, String sentence) {
            String t = id.contains("bird") || id.contains("cloud") ? "BAU TROI" :
                    id.contains("bus") || id.contains("truck") || id.contains("stop") ? "GIAO THONG" :
                    id.contains("cat") || id.contains("rabbit") || id.contains("panda") ? "DONG VAT" :
                    id.contains("color") || id.contains("red") ? "MAU SAC" : "KHAM PHA";
            return new Prompt(t, ascii(sentence));
        }
    }
    static final Prompt WELCOME = Prompt.fromBeat("welcome",
            "Chao cac ban! Chung minh cung kham pha the gioi nhe!");
    private static final Color DARK = new Color(.08f,.21f,.32f,.85f);
    private static final Color PAPER = new Color(.97f,.98f,1f,.92f);
    private static final Color YELLOW = new Color(1f,.83f,.4f,1f);
    private static final Color GREEN = new Color(.28f,.68f,.42f,1f);
    private static final Color PINK = new Color(.94f,.52f,.7f,1f);

    static String ascii(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s.replace('đ','d').replace('Đ','D'), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "").replaceAll("[^a-zA-Z0-9 .,!?():/-]", " ").replaceAll(" +", " ").trim();
    }
    private static String[] wrap(String s, int max, int rows) {
        String[] out = new String[rows];
        java.util.Arrays.fill(out, "");
        int line = 0;
        for (String word : s.split(" ")) {
            if (word.isEmpty()) continue;
            if (!out[line].isEmpty() && out[line].length() + word.length() + 1 > max) {
                if (++line == rows) break;
            }
            out[line] += (out[line].isEmpty() ? "" : " ") + word;
        }
        return out;
    }
    private static void text(SpriteBatch b, BitmapFont f, String s, float x, float y, float size, Color c) {
        f.getData().setScale(size);
        f.setColor(c);
        f.draw(b,s,x,y);
    }
    void draw(SpriteBatch b, WorldPainter p, BitmapFont f, String audience,
              Prompt prompt, String poi, String distance, String source) {
        if (prompt == null) prompt = WELCOME;
        String mode = "SAU".equals(audience) ? "SAU - 4 TUOI" :
                "ONG".equals(audience) ? "ONG - 3 TUOI" : "SAU & ONG CUNG CHOI";
        Color accent = "SAU".equals(audience) ? GREEN : "ONG".equals(audience) ? PINK : YELLOW;
        p.hudRect(b,DARK,24,941,1872,115);
        p.hudRect(b,accent,30,943,10,110);
        text(b,f,"DeX KIDS ADVENTURE",57,1008,2.4f,YELLOW);
        text(b,f,"BAN DONG HANH TREN MOI CHANG DUONG",57,966,1.28f,Color.WHITE);
        p.hudRect(b,accent,867,965,460,67);
        text(b,f,mode,890,1008,1.8f,Color.BLACK);
        p.hudRect(b,DARK,1350,965,512,67);
        text(b,f,source,1366,1007,1.25f,Color.WHITE);

        tile(b,p,f,31,697,"KHAM PHA","NHIEN NHIEN / DIA DANH");
        tile(b,p,f,31,596,"DO VUI",prompt.topic());
        tile(b,p,f,31,495,"NGHE KE CHUYEN","CAPYBARA NOI CHUYEN");
        p.hudRect(b,PAPER,914,754,956,163);
        p.hudRect(b,accent,914,754,12,163);
        text(b,f,"CAPYBARA: " + prompt.topic(),943,887,1.5f,GREEN);
        String[] q=wrap(prompt.text(),43,3);
        text(b,f,q[0],940,846,1.7f,Color.DARK_GRAY);
        text(b,f,q[1],940,807,1.7f,Color.DARK_GRAY);
        text(b,f,q[2],940,769,1.7f,Color.DARK_GRAY);

        p.hudRect(b,DARK,25,18,1420,170);
        text(b,f,"GOC DIA DANH / KIEN THUC",53,150,1.7f,YELLOW);
        String[] place=wrap(ascii(poi == null || poi.isBlank()
                ? "Dang kham pha the gioi hoat hinh" : poi),69,2);
        text(b,f,place[0],53,108,1.58f,Color.WHITE);
        text(b,f,place[1],53,75,1.58f,Color.WHITE);
        text(b,f,"DIA DANH GPS CO THE CHUA TRUNG VOI TUYEN XE",53,38,1.1f,YELLOW);
        p.hudRect(b,DARK,1463,18,435,170);
        text(b,f,"NHAT KY CHUYEN DI",1487,150,1.68f,YELLOW);
        String[] metrics=wrap(ascii(distance),28,2);
        text(b,f,metrics[0],1487,107,1.4f,Color.WHITE);
        text(b,f,metrics[1],1487,73,1.4f,Color.WHITE);
        text(b,f,"GPS / GPX / DEMO",1487,38,1.15f,YELLOW);
        f.getData().setScale(2.5f);
        f.setColor(Color.WHITE);
    }
    private void tile(SpriteBatch b,WorldPainter p,BitmapFont f,float x,float y,String title,String caption) {
        p.hudRect(b,DARK,x,y,267,82);
        text(b,f,title,x+15,y+58,1.65f,YELLOW);
        text(b,f,caption,x+15,y+23,1.05f,Color.WHITE);
    }
}
