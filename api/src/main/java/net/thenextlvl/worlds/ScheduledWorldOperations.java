package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.thenextlvl.worlds.event.WorldActionScheduledEvent;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;

import java.util.stream.Stream;

public sealed interface ScheduledWorldOperations permits SimpleScheduledWorldOperations {
    @Contract(pure = true)
    Stream<Operation> operations();

    @Contract(pure = true)
    Stream<Operation> operations(Key world);

    @Contract(pure = true)
    Stream<Operation> operations(World world);

    @Contract(pure = true)
    Stream<Operation> operations(WorldActionScheduledEvent.ActionType actionType);

    boolean scheduleDeletion(World world);

    boolean scheduleRegeneration(World world);

    boolean scheduleBackupRestoration(final World world, final Backup backup);

    @Contract(mutates = "this")
    boolean cancel(Operation operation);

    sealed interface Operation extends Keyed, Runnable permits SimpleScheduledWorldOperations.Operation {
        @Contract(pure = true)
        WorldActionScheduledEvent.ActionType type();
    }
}
