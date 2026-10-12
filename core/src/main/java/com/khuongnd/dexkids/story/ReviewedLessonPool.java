package com.khuongnd.dexkids.story;

import java.util.*;

/** Worker-cycle-only staging. No staged item is a playable pack. */
public final class ReviewedLessonPool {
    public record Candidate(String id, CreativeLessonPack.Lesson lesson,
                            String generationModel, String reviewModel) {
        public Candidate(String id, CreativeLessonPack.Lesson lesson) {
            this(id,lesson,"fixture","fixture");
        }
    }
    public record Verdict(String id, boolean accepted) {}
    private final List<Candidate> accepted = new ArrayList<>();
    public List<Candidate> accepted() { return List.copyOf(accepted); }

    /** Validate the entire response before mutating staging, including rejected IDs. */
    public void merge(List<Candidate> batch, List<Verdict> verdicts,
                      EntertainmentDirector.Audience audience, int sauAge, int ongAge,
                      Collection<String> recent) {
        Map<String, Candidate> ids = new LinkedHashMap<>();
        for (Candidate c : batch) {
            if (c.id()==null || c.id().isBlank() || ids.put(c.id(),c)!=null)
                throw new IllegalArgumentException("Duplicate candidate ID");
        }
        Set<String> seen = new HashSet<>();
        for (Verdict v : verdicts)
            if (!ids.containsKey(v.id()) || !seen.add(v.id()))
                throw new IllegalArgumentException("Invalid review ID");
        if (seen.size()!=ids.size()) throw new IllegalArgumentException("Incomplete review");
        Set<String> hashes = new HashSet<>(recent);
        for (Candidate c : accepted) hashes.add(CreativeLessonPack.fingerprint(c.lesson().prompt()));
        for (Verdict v : verdicts) {
            Candidate c=ids.get(v.id());
            var x=c.lesson();
            if (v.accepted() && (audience==EntertainmentDirector.Audience.BOTH || x.focus()==audience)
                && x.targetAge()==CreativeLessonPack.ageForFocus(x.focus(),sauAge,ongAge)
                && hashes.add(CreativeLessonPack.fingerprint(x.prompt()))) accepted.add(c);
        }
    }

    /** Prefer missing genres/topics, then restore the parent-owned turn schedule. */
    public CreativeLessonPack complete(EntertainmentDirector.Audience audience,
            int sauAge, int ongAge, Collection<String> recent) {
        List<CreativeLessonPack.Lesson> remaining=new ArrayList<>();
        for (Candidate c:accepted) remaining.add(c.lesson());
        List<CreativeLessonPack.Lesson> selected=new ArrayList<>();
        Set<String> topics=new HashSet<>();
        Set<CreativeLessonPack.Genre> genres=new HashSet<>();
        int stories=0;
        for(int i=0;i<20;i++) {
            var focus=CreativeLessonPack.focusAt(audience,i);
            CreativeLessonPack.Lesson best=null; int bestScore=-1;
            for(var x:remaining) {
                if(x.focus()!=focus)continue;
                int score=(topics.contains(x.topic())?0:2)+(genres.contains(x.genre())?0:4)
                    +(stories<2 && x.genre()==CreativeLessonPack.Genre.STORY?8:0);
                if(score>bestScore){best=x;bestScore=score;}
            }
            if(best==null)break;
            remaining.remove(best);selected.add(best);topics.add(best.topic());genres.add(best.genre());
            if(best.genre()==CreativeLessonPack.Genre.STORY)stories++;
        }
        if(selected.size()<16)throw new IllegalArgumentException("CREATIVE_REFILL_REQUIRED");
        var sources=new ArrayList<CreativeLessonPack.ReviewSource>();
        for(var lesson:selected) {
            var c=accepted.stream().filter(x->x.lesson()==lesson).findFirst().orElseThrow();
            sources.add(new CreativeLessonPack.ReviewSource(c.id(),c.generationModel(),c.reviewModel()));
        }
        return CreativeLessonPack.validate(selected,audience,sauAge,ongAge,recent).withReviewSources(sources);
    }
}
