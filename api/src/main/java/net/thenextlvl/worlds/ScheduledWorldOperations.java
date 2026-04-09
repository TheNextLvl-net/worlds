package net.thenextlvl.worlds;

import org.bukkit.World;
import org.jetbrains.annotations.Contract;

public interface ScheduledWorldOperations extends WorldOperations {
    @Contract(mutates = "this")
    boolean cancelDeletion(World world);

    @Contract(pure = true)
    boolean isDeletionScheduled(World world);

    @Contract(mutates = "this")
    boolean cancelRegeneration(World world);

    @Contract(pure = true)
    boolean isRegenerationScheduled(World world);
}
