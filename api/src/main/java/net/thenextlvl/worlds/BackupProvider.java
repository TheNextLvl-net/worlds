package net.thenextlvl.worlds;

import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BackupProvider {
    @Contract(mutates = "io")
    CompletableFuture<Backup> backup(World world);

    @Contract(mutates = "param,io")
    CompletableFuture<ActionResult<World>> restore(World world, Backup backup);

    @ApiStatus.OverrideOnly
    @Contract(mutates = "io")
    ActionResult.Status restoreNow(Path path, Backup backup);

    @Contract(pure = true)
    CompletableFuture<@Unmodifiable List<Backup>> listBackups();

    @Contract(pure = true)
    default CompletableFuture<@Unmodifiable List<Backup>> listBackups(final World world) {
        return listBackups(world.key());
    }

    @Contract(pure = true)
    CompletableFuture<@Unmodifiable List<Backup>> listBackups(Key world);

    @Contract(mutates = "io")
    CompletableFuture<Boolean> delete(Backup backup);
}
