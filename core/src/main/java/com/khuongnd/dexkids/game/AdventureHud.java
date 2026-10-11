package com.khuongnd.dexkids.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.text.Normalizer;

/** Informational game HUD, not touch controls. POI content is labeled as estimated. */
final class AdventureHud {
    record Prompt(String topic, String text, StoryCompanionPose pose, String focus, String beatId) {
        static Prompt fromBeat(String id, String sentence) {
            String t = id.contains("bird") || id.contains("cloud") ? "BAU TROI" :
                    id.contains("bus") || id.contains("truck") || id.contains("stop") ? "GIAO THONG" :
                    id.contains("cat") || id.contains("rabbit") || id.contains("panda") ? "DONG VAT" :
                    id.contains("color") || id.contains("red") ? "MAU SAC" : "KHAM PHA";
            return new Prompt(t, ascii(sentence), StoryCompanionPose.fromBeat(id),
                id.startsWith("sau-") ? "SAU" : id.startsWith("ong-") ? "ONG" : "BOTH", id);
        }
    }
    static final Prompt WELCOME = Prompt.fromBeat("welcome",
            "Chao cac ban! Chung minh cung kham pha the gioi nhe!");
    private static final Color DARK = new Color(.08f,.21f,.32f,.85f);
    private static final Color PAPER = new Color(.97f,.98f,1f,.92f);
    private static final Color YELLOW = new Color(1f,.83f,.4f,1f);
    private static final Color GREEN = new Color(.28f,.68f,.42f,1f);
    private static final Color PINK = new Color(.94f,.52f,.7f,1f);
    private Prompt cachedPrompt;
    private String cachedPromptTitle;
    private String[] cachedPromptLines = {"","",""};
    private String cachedPoi;
    private String[] cachedPoiLines = {"",""};
    private String cachedPoiShort = "";
    private String cachedMetrics;
    private String[] cachedMetricLines = {"",""};

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
        if (prompt != cachedPrompt) {
            cachedPrompt = prompt;
            cachedPromptTitle = "CAPYBARA: " + prompt.topic();
            cachedPromptLines = wrap(prompt.text(), 43, 3);
        }
        String nextPoi = poi == null || poi.isBlank() ? "Dang kham pha the gioi hoat hinh" : poi;
        if (!nextPoi.equals(cachedPoi)) {
            cachedPoi = nextPoi;
            cachedPoiLines = wrap(ascii(nextPoi), 69, 2);
            cachedPoiShort = cachedPoiLines[0].substring(0, Math.min(43, cachedPoiLines[0].length()));
        }
        String nextMetrics = distance == null ? "" : distance;
        if (!nextMetrics.equals(cachedMetrics)) {
            cachedMetrics = nextMetrics;
            cachedMetricLines = wrap(ascii(nextMetrics), 28, 2);
        }
        String mode = "SAU".equals(audience) ? "SAU - 4 TUOI" :
                "ONG".equals(audience) ? "ONG - 3 TUOI" : "SAU & ONG CUNG CHOI";
        Color accent = "SAU".equals(audience) ? GREEN : "ONG".equals(audience) ? PINK : YELLOW;
        p.hudRect(b,DARK,18,918,760,142);
        p.hudRect(b,accent,18,918,5,142);
        text(b,f,"DeX KIDS ADVENTURE  |  " + mode,32,1042,1.15f,YELLOW);
        text(b,f,source,32,1019,1.0f,Color.WHITE);
        text(b,f,"DO VUI / KHAM PHA: " + prompt.topic(),32,995,1.05f,Color.WHITE);
        text(b,f,cachedPromptLines[0],32,971,1.05f,Color.WHITE);
        text(b,f,cachedPromptLines[1],32,947,1.05f,Color.WHITE);
        p.hudRect(b,DARK,1470,25,420,135);
        text(b,f,"NHAT KY CHUYEN DI",1484,139,1.05f,YELLOW);
        text(b,f,cachedMetricLines[0],1484,115,1f,Color.WHITE);
        text(b,f,cachedMetricLines[1],1484,93,1f,Color.WHITE);
        text(b,f,cachedPoiShort,1484,68,.9f,Color.WHITE);
        text(b,f,"GPS / GPX / DEMO - UOC TINH",1484,44,.9f,YELLOW);
        f.getData().setScale(1.15f);
        f.setColor(Color.WHITE);
    }
    private void tile(SpriteBatch b,WorldPainter p,BitmapFont f,float x,float y,String title,String caption) {
        p.hudRect(b,DARK,x,y,267,82);
        text(b,f,title,x+15,y+58,1.65f,YELLOW);
        text(b,f,caption,x+15,y+23,1.05f,Color.WHITE);
    }
}
