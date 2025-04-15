package net.thenextlvl.perworlds;

import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

/**
 * Represents a group of worlds that are not explicitly covered by any other specific {@link WorldGroup}.
 * <p>
 * This is a special type of group that cannot have worlds added or removed from it,
 * and it serves as a container for unmanaged or unassociated worlds.
 */
@NullMarked
public interface UnownedWorldGroup extends WorldGroup {
    /**
     * Attempts to add the specified world to the group.
     * In this implementation, adding a world is not supported and will always return false.
     *
     * @param world the {@link World} to add
     * @return false, as the operation is not supported
     */
    @Override
    default boolean addWorld(World world) {
        return false;
    }

    /**
     * Attempts to remove the specified world from the group.
     * In this implementation, removing a world is not supported and will always return false.
     *
     * @param world the {@link World} to remove
     * @return false, as the operation is not supported
     */
    @Override
    default boolean removeWorld(World world) {
        return false;
    }
}
