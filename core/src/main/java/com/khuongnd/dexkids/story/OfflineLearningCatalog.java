package com.khuongnd.dexkids.story;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Immutable local learning cards: no network, GPS, microphone or personal data. */
public final class OfflineLearningCatalog {
    public enum Kind { QUIZ, WORD }
    public record Card(String id,Kind kind,String topic,int minAge,int maxAge,
                       String promptVi,String answerVi,String englishWord) {
        public Card {
            if(id==null || !id.matches("[a-z][a-z0-9-]{3,60}") || kind==null ||
                !Set.of("sea","city","road","nature","weather","animal","garden","safety").contains(topic) ||
                minAge<3 || maxAge>6 || maxAge<minAge || !safe(promptVi,10,230) ||
                !safe(answerVi,6,230) || englishWord==null ||
                (kind==Kind.WORD && !englishWord.matches("[a-z][a-z -]{1,29}")) ||
                (kind==Kind.QUIZ && !englishWord.isBlank()))
                throw new IllegalArgumentException("Unsafe learning card");
            String words=(promptVi+" "+answerVi).toLowerCase(Locale.ROOT);
            if(words.contains("đang đi qua") || words.contains("chúng ta đang ở") ||
                    words.contains("phía trước xe") || words.contains("vừa đến"))
                throw new IllegalArgumentException("Unverified location claim");
        }
        private static boolean safe(String s,int min,int max){
            return s!=null && s.length()>=min && s.length()<=max &&
                s.codePoints().noneMatch(ch->ch<32||ch==127);
        }
        public boolean forAge(int age){return age>=minAge&&age<=maxAge;}
    }
    private final List<Card> cards;
    private OfflineLearningCatalog(List<Card> cards){this.cards=List.copyOf(cards);}
    public static OfflineLearningCatalog empty(){return new OfflineLearningCatalog(List.of());}
    /** Curated catalog is immutable; expose IDs for the AI selection validator only. */
    public List<Card> cards(){return cards;}
    public int size(){return cards.size();}
    public long count(Kind type){return cards.stream().filter(c->c.kind()==type).count();}
    public static String topicForBackdrop(String scene){
        if(scene==null)return "";
        return switch(scene){
            case "vungtau_beach","nha_trang_beach","vungtau_lighthouse","ke_ga",
                 "rock_coast","vungtau_christ" -> "sea";
            case "dalat_lake","waterfall","hilltop_mountain","rock_stacks" -> "nature";
            case "tea_hills","flower_garden","city_park" -> "garden";
            case "urban_roundabout" -> "road";
            case "hcm_palace","saigon_post","war_museum","heritage_pagoda",
                 "gothic_church","cham_tower","lam_vien","bien_hoa_city" -> "city";
            default -> "";
        };
    }
    /** Context is a hint derived from a sourced POI, not a claim of actual passage. */
    public Card select(Kind kind,int age,String topic,int turn){
        if(kind==null||age<3||age>6||turn<0)return null;
        String wanted=topic==null?"":topic;
        List<Card> contextual=cards.stream().filter(c->c.kind()==kind&&c.forAge(age)&&
                c.topic().equals(wanted)).toList();
        List<Card> pool=contextual.isEmpty()
            ?cards.stream().filter(c->c.kind()==kind&&c.forAge(age)).toList():contextual;
        return pool.isEmpty()?null:pool.get(Math.floorMod(turn*7+turn/Math.max(1,pool.size()),pool.size()));
    }
    public static OfflineLearningCatalog parse(InputStream in){
        Objects.requireNonNull(in);
        try{
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buffer=new byte[4096];int n;
            while((n=in.read(buffer))!=-1){
                if(out.size()+n>131072)throw new IllegalArgumentException("Learning pack oversized");
                out.write(buffer,0,n);
            }
            String text=new String(out.toByteArray(),StandardCharsets.UTF_8);
            if(text.indexOf('\uFFFD')>=0||text.indexOf('\0')>=0)
                throw new IllegalArgumentException("Invalid encoding");
            String[] lines=text.split("\\R",-1);
            if(lines.length<2||!lines[0].equals("# dexkids-offline-learning-v1")||
                !lines[1].equals("id\tkind\ttopic\tmin_age\tmax_age\tprompt_vi\tanswer_vi\tenglish"))
                throw new IllegalArgumentException("Invalid learning schema");
            ArrayList<Card> cards=new ArrayList<>();Set<String> ids=new HashSet<>();
            for(int i=2;i<lines.length;i++){
                if(lines[i].isBlank())continue;
                if(cards.size()>=300)throw new IllegalArgumentException("Too many cards");
                String[] row=lines[i].split("\\t",-1);
                if(row.length!=8)throw new IllegalArgumentException("Invalid row "+i);
                Card card=new Card(row[0],Kind.valueOf(row[1]),row[2],Integer.parseInt(row[3]),
                    Integer.parseInt(row[4]),row[5],row[6],row[7]);
                if(!ids.add(card.id()))throw new IllegalArgumentException("Duplicate card");
                cards.add(card);
            }
            return new OfflineLearningCatalog(cards);
        }catch(IOException|RuntimeException e){
            throw new IllegalArgumentException("Offline learning data rejected",e);
        }
    }
}
