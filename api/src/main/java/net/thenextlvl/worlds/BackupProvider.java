package net.thenextlvl.worlds;

import org.bukkit.World;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BackupProvider {
    @Contract(mutates = "io")
    CompletableFuture<Backup> backup(Level level);

    @Contract(mutates = "io")
    CompletableFuture<Backup> backup(World world);

    @Contract(mutates = "io")
    CompletableFuture<ActionResult<Level>> restore(Level level, Backup backup);

    @Contract(mutates = "io")
    CompletableFuture<ActionResult<World>> restore(World world, Backup backup);

    @Contract(pure = true)
    CompletableFuture<List<Backup>> listBackups(Level level);

    @Contract(pure = true)
    CompletableFuture<List<Backup>> listBackups(World world);

    @Contract(mutates = "io")
    CompletableFuture<Boolean> delete(Backup backup);
}
