package net.thenextlvl.worlds.api.event;

import com.google.common.base.Preconditions;
import net.thenextlvl.worlds.api.level.Level;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;

/**
 * Represents an event triggered when a {@link World} is cloned.
 * This event allows developers to listen to and modify the cloning process.
 * It provides information about whether the entire
 * world, including all data and entities, is being cloned, or if only the
 * {@code level.dat} is copied for generation.
 */
@NullMarked
public class WorldCloneEvent extends WorldEvent {
    private static final HandlerList handlerList = new HandlerList();

    private @Nullable BiPredicate<Path, BasicFileAttributes> fileFilter = null;
    private final Level clone;
    private final boolean full;

    @ApiStatus.Internal
    public WorldCloneEvent(World world, Level clone, boolean full) {
        super(world, false);
        this.clone = clone;
        this.full = full;
    }

    @ApiStatus.Internal
    @Contract(pure = true)
    public @Nullable BiPredicate<Path, BasicFileAttributes> getFileFilter() {
        return fileFilter;
    }
    
    /**
     * Retrieves the cloned {@link Level} associated with this event.
     *
     * @return the clone of the level involved in the cloning process
     */
    @Contract(pure = true)
    public Level getClone() {
        return clone;
    }

    /**
     * Adds a predicate to filter files during the cloning process.
     * The filter determines whether a specific file should be included in the cloning
     * based on its path and attributes.
     * <p>
     * If the filter returns false, the corresponding file
     * or directory will not be included in the cloning process.
     * Returning false for a directory skips the entire directory.
     * <p>
     * If multiple filters are added, they are combined logically such that filters
     * added later only evaluate if no earlier filter has disqualified the file for cloning.
     *
     * @param filter a {@link BiPredicate} that takes a {@link Path} and {@link BasicFileAttributes}
     *               and returns {@code true} if the file should be included in the cloning process,
     *               {@code false} otherwise
     * @throws IllegalStateException if the event represents a non-{@link #isFullClone() full} clone operation
     */
    @Contract(mutates = "this")
    public void addFileFilter(BiPredicate<Path, BasicFileAttributes> filter) throws IllegalStateException {
        Preconditions.checkState(full, "Cannot add file filter to non-full clone event");
        this.fileFilter = this.fileFilter != null ? this.fileFilter.and(filter) : filter;
    }

    /**
     * Indicates whether the entire world, including entities, data, regions, etc. will be cloned
     * or only the {@code level.dat} file, so that the world is just generated with the same config.
     *
     * @return true if the entire world will be cloned, false, if only the {@code level.dat} file will be cloned
     */
    @Contract(pure = true)
    public boolean isFullClone() {
        return full;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
