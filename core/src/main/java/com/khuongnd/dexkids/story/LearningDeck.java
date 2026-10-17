package com.khuongnd.dexkids.story;

import java.util.*;

/** Session-local shuffled cards. One shared history prevents repeats across age/topic changes. */
public final class LearningDeck {
    private final OfflineLearningCatalog catalog;
    private final Random random;
    private final Set<String> used = new LinkedHashSet<>();
    private final Map<OfflineLearningCatalog.Kind, String> last = new EnumMap<>(OfflineLearningCatalog.Kind.class);
    public LearningDeck(OfflineLearningCatalog catalog) { this(catalog, new Random()); }
    public LearningDeck(OfflineLearningCatalog catalog, Random random) {
        this.catalog = Objects.requireNonNull(catalog);
        this.random = Objects.requireNonNull(random);
    }
    public List<String> history() { return List.copyOf(used); }
    public void restoreHistory(List<String> ids) {
        used.clear(); last.clear();
        for (String id : ids) catalog.cards().stream().filter(c -> c.id().equals(id)).findFirst()
            .ifPresent(c -> { used.add(id); last.put(c.kind(), id); });
    }
    public OfflineLearningCatalog.Card next(OfflineLearningCatalog.Kind kind, int age, String topic) {
        var eligible = catalog.cards().stream().filter(c -> c.kind() == kind && c.forAge(age)).toList();
        if (eligible.isEmpty()) return null;
        var remaining = eligible.stream().filter(c -> !used.contains(c.id())).toList();
        if (remaining.isEmpty()) {
            eligible.forEach(c -> used.remove(c.id()));
            remaining = eligible.stream().filter(c -> eligible.size() == 1 || !c.id().equals(last.get(kind))).toList();
        }
        // Prefer a contextual unused card, but exhaust other topics before recycling.
        var contextual = remaining.stream().filter(c -> c.topic().equals(topic)).toList();
        var pool = contextual.isEmpty() ? remaining : contextual;
        var selected = pool.get(random.nextInt(pool.size()));
        used.add(selected.id()); last.put(kind, selected.id());
        return selected;
    }
}
