package net.thenextlvl.perworlds.data;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WardenSpawnTracker {
    WardenSpawnTracker cooldownTicks(int cooldownTicks);

    WardenSpawnTracker ticksSinceLastWarning(int ticksSinceLastWarning);

    WardenSpawnTracker warningLevel(int warningLevel);

    int cooldownTicks();

    int ticksSinceLastWarning();

    int warningLevel();
}
