package net.thenextlvl.worlds;

import org.bukkit.World;
import org.jetbrains.annotations.Contract;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface WorldOperations {
    @Contract(mutates = "param1")
    CompletableFuture<ActionResult<Void>> delete(World world);

    @Contract(mutates = "param1")
    CompletableFuture<ActionResult<World>> regenerate(World world);

    @Contract(mutates = "param1")
    CompletableFuture<ActionResult<World>> regenerate(World world, Consumer<Level.Builder> builder);
}
