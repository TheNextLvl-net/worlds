package net.thenextlvl.perworlds.model;

import net.thenextlvl.perworlds.data.AdvancementData;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NullMarked
public class PaperAdvancementData implements AdvancementData {
    private final Advancement advancement;
    private final Set<String> remainingCriteria = new HashSet<>();
    private final Map<String, Date> awardedCriteria = new HashMap<>();

    public PaperAdvancementData(AdvancementProgress progress) {
        this.advancement = progress.getAdvancement();
        progress.getAwardedCriteria().forEach(criteria -> {
            var date = progress.getDateAwarded(criteria);
            awardedCriteria.put(criteria, date != null ? date : new Date());
        });
        this.remainingCriteria.addAll(progress.getRemainingCriteria());
    }

    public PaperAdvancementData(Advancement advancement, Map<String, Date> awardedCriteria, Set<String> remainingCriteria) {
        this.advancement = advancement;
        this.awardedCriteria.putAll(awardedCriteria);
        this.remainingCriteria.addAll(remainingCriteria);
    }

    public @Unmodifiable Map<String, Date> awardedCriteria() {
        return Map.copyOf(awardedCriteria);
    }

    @Override
    public Advancement getAdvancement() {
        return advancement;
    }

    @Override
    public boolean isDone() {
        return remainingCriteria.isEmpty();
    }

    @Override
    public boolean awardCriteria(String criteria) {
        return remainingCriteria.remove(criteria) && awardedCriteria.putIfAbsent(criteria, new Date()) == null;
    }

    @Override
    public boolean revokeCriteria(String criteria) {
        return remainingCriteria.add(criteria) && awardedCriteria.remove(criteria) != null;
    }

    @Override
    public @Nullable Date getDateAwarded(String criteria) {
        return awardedCriteria.get(criteria);
    }

    @Override
    public boolean setDateAwarded(String criteria, Date date) {
        return awardedCriteria.containsKey(criteria) && awardedCriteria.put(criteria, date) != null;
    }

    @Override
    public boolean shouldSerialize() {
        return !awardedCriteria.isEmpty() && !remainingCriteria.isEmpty();
    }

    @Override
    public @Unmodifiable Set<String> getRemainingCriteria() {
        return Set.copyOf(remainingCriteria);
    }

    @Override
    public @Unmodifiable Set<String> getAwardedCriteria() {
        return Set.copyOf(awardedCriteria.keySet());
    }
}
