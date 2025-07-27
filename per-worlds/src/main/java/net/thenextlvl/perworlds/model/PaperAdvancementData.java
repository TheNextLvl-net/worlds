package net.thenextlvl.perworlds.model;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.thenextlvl.perworlds.data.AdvancementData;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NullMarked
public class PaperAdvancementData implements AdvancementData {
    private final Advancement advancement;
    private final Set<String> remainingCriteria = new HashSet<>();
    private final Map<String, @Nullable Instant> awardedCriteria = new HashMap<>();

    public PaperAdvancementData(AdvancementHolder holder, AdvancementProgress progress) {
        this.advancement = holder.toBukkit();
        progress.getCompletedCriteria().forEach(criteria -> {
            var criterion = progress.getCriterion(criteria);
            awardedCriteria.put(criteria, criterion != null ? criterion.getObtained() : null);
        });
        progress.getRemainingCriteria().forEach(this.remainingCriteria::add);
    }

    public PaperAdvancementData(Advancement advancement, Map<String, Instant> awardedCriteria, Set<String> remainingCriteria) {
        this.advancement = advancement;
        this.awardedCriteria.putAll(awardedCriteria);
        this.remainingCriteria.addAll(remainingCriteria);
    }

    public @Unmodifiable Map<String, @Nullable Instant> awardedCriteria() {
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
        return remainingCriteria.remove(criteria) && awardedCriteria.putIfAbsent(criteria, Instant.now()) == null;
    }

    @Override
    public boolean revokeCriteria(String criteria) {
        return remainingCriteria.add(criteria) && awardedCriteria.remove(criteria) != null;
    }

    @Override
    public @Nullable Instant getTimeAwarded(String criteria) {
        return awardedCriteria.get(criteria);
    }

    @Override
    public boolean setTimeAwarded(String criteria, Instant instant) {
        return awardedCriteria.containsKey(criteria) && awardedCriteria.put(criteria, instant) != null;
    }

    @Override
    public boolean shouldSerialize() {
        return !awardedCriteria.isEmpty() && isEnabled();
    }

    private boolean isEnabled() {
        var disabled = SpigotConfig.disabledAdvancements;
        if (disabled == null || disabled.isEmpty()) return true;
        if (disabled.contains("*")) return false;
        if (disabled.contains(advancement.getKey().asString())) return false;
        return !disabled.contains(advancement.getKey().getNamespace());
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
