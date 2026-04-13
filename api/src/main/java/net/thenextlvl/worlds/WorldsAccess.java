package net.thenextlvl.worlds;

import net.thenextlvl.binder.StaticBinder;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

@ApiStatus.NonExtendable
public interface WorldsAccess extends Plugin {
    @Contract(pure = true)
    static WorldsAccess access() {
        final class Cache {
            private static final WorldsAccess INSTANCE = StaticBinder.getInstance(WorldsAccess.class.getClassLoader()).find(WorldsAccess.class);
        }
        return Cache.INSTANCE;
    }
    // todo: Stuff that has to go

    Optional<World> getTarget(World world, PortalType type);

    // todo end
    
    Path getWorldContainer();

    @Contract(pure = true)
    Level getLevel(World world);

    @Contract(pure = true)
    Optional<Level> getLevel(String name);

    @Contract(pure = true)
    Stream<Level> getLevels();

    @Contract(pure = true)
    Stream<Path> listLevels();

    @Contract(pure = true)
    boolean isLevel(Path path);

    @Contract(value = "_ -> new", pure = true)
    Optional<Level.Builder> read(Path directory);

    @Contract(value = "_ -> new", pure = true)
    CompletableFuture<ActionResult<World>> load(Path directory);

    CompletableFuture<World> create(Level level);

    @Contract(mutates = "param1")
    CompletableFuture<Boolean> unload(World world, boolean save);

    @Contract(mutates = "param1")
    CompletableFuture<Boolean> save(World world, boolean flush);

    @Contract(mutates = "param1")
    CompletableFuture<ActionResult<World>> clone(World world, boolean full);

    @Contract(mutates = "param1")
    CompletableFuture<ActionResult<Void>> delete(World world);

    @Contract(mutates = "param1")
    CompletableFuture<ActionResult<World>> regenerate(World world);

    @Contract(mutates = "param1")
    CompletableFuture<ActionResult<World>> regenerate(World world, Consumer<Level.Builder> builder);

    @Contract(pure = true)
    boolean isEnabled(World world);

    @Contract(mutates = "param1")
    void setEnabled(World world, boolean enabled);

    @Contract(pure = true)
    String getEntryPermission(World world);

    @Contract(pure = true)
    ScheduledWorldOperations getScheduler();

    @Contract(pure = true)
    BackupProvider getBackupProvider();

    @Contract(mutates = "this")
    void setBackupProvider(BackupProvider provider);
}
