package net.thenextlvl.perworlds.model;

import net.thenextlvl.perworlds.data.WardenSpawnTracker;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperWardenSpawnTracker implements WardenSpawnTracker {
    private int cooldownTicks = 0;
    private int ticksSinceLastWarning = 0;
    private int warningLevel = 0;

    @Override
    public PaperWardenSpawnTracker cooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
        return this;
    }

    @Override
    public PaperWardenSpawnTracker ticksSinceLastWarning(int ticksSinceLastWarning) {
        this.ticksSinceLastWarning = ticksSinceLastWarning;
        return this;
    }

    @Override
    public PaperWardenSpawnTracker warningLevel(int warningLevel) {
        this.warningLevel = warningLevel;
        return this;
    }

    @Override
    public int cooldownTicks() {
        return cooldownTicks;
    }

    @Override
    public int ticksSinceLastWarning() {
        return ticksSinceLastWarning;
    }

    @Override
    public int warningLevel() {
        return warningLevel;
    }

    public static PaperWardenSpawnTracker of(Player player) {
        return new PaperWardenSpawnTracker()
                .cooldownTicks(player.getWardenWarningCooldown())
                .ticksSinceLastWarning(player.getWardenTimeSinceLastWarning())
                .warningLevel(player.getWardenWarningLevel());
    }
}
