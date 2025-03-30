package net.thenextlvl.perworlds.data;

import org.bukkit.advancement.AdvancementProgress;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Date;
import java.util.Set;

@NullMarked
public interface AdvancementData extends AdvancementProgress {
    @Override
    @Unmodifiable
    Set<String> getAwardedCriteria();

    @Override
    @Unmodifiable
    Set<String> getRemainingCriteria();

    boolean setDateAwarded(String criteria, Date date);
}
