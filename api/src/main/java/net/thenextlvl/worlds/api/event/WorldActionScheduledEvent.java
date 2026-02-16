package net.thenextlvl.worlds.api.event;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Represents an event triggered when an action is scheduled to be performed on a {@link World}.
 * This event allows developers to listen to such actions and optionally cancel or modify them.
 * It supports adding a custom action to be executed during the shutdown process of the scheduled action.
 * <p>
 * This event provides details about the type of action being scheduled and allows modification
 * of the event's behavior through implementing the {@link Cancellable} interface.
 * <p>
 * The {@link ActionType} enum defines the possible actions that can be scheduled,
 * such as deleting a world or regenerating it.
 *
 * @since 3.0.0
 */
@NullMarked
public final class WorldActionScheduledEvent extends WorldEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private @Nullable Consumer<Path> action = null;
    private boolean cancelled = false;
    private final ActionType actionType;

    @ApiStatus.Internal
    public WorldActionScheduledEvent(final World world, final ActionType actionType) {
        super(world, false);
        this.actionType = actionType;
    }

    /**
     * Retrieves the type of action scheduled to be performed on a world.
     *
     * @return the scheduled action type
     */
    @Contract(pure = true)
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Adds a custom action to be executed during the shutdown process of the scheduled world action.
     *
     * @param action the action to be performed
     */
    @Contract(mutates = "this")
    public void addAction(final Consumer<Path> action) {
        this.action = this.action != null ? this.action.andThen(action) : action;
    }

    @ApiStatus.Internal
    public @Nullable Consumer<Path> getAction() {
        return action;
    }

    @Override
    @Contract(pure = true)
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    @Contract(mutates = "this")
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Represents the type of action scheduled to be performed on a {@link World}.
     */
    public enum ActionType {
        /**
         * This action is used to schedule the removal of a {@link World}.
         *
         * @see WorldDeleteEvent
         */
        DELETE,
        /**
         * This action is used to schedule the regeneration of a {@link World}.
         *
         * @see WorldRegenerateEvent
         */
        REGENERATE,
        /**
         * This action is used to schedule the restoration of a {@link World} backup.
         *
         * @see WorldBackupRestoreEvent
         * @since 3.7.0
         */
        RESTORE_BACKUP
    }
}
