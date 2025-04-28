package net.thenextlvl.perworlds;

import org.jspecify.annotations.NullMarked;

/**
 * Represents a group of worlds that are not explicitly covered by any other specific {@link WorldGroup}.
 * <p>
 * This is a special type of group that cannot have worlds added or removed from it,
 * and it serves as a container for unmanaged or unassociated worlds.
 */
@NullMarked
public interface UnownedWorldGroup extends WorldGroup {
}
