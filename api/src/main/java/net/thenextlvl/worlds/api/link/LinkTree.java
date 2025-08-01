package net.thenextlvl.worlds.api.link;

import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A LinkTree is a mapping between one Overworld, one Nether, and one End.
 * Links restore vanilla portal behavior.
 */
@NullMarked
public interface LinkTree {
    /**
     * Retrieves the {@code Overworld} associated with this {@link LinkTree}, if available.
     *
     * @return the {@code World} associated with this {@code LinkTree}
     */
    @Contract(pure = true)
    World getOverworld();

    /**
     * Retrieves the {@code Nether} world associated with this {@link LinkTree}, if available.
     *
     * @return an Optional containing the world, or empty if undefined or not loaded
     */
    @Contract(pure = true)
    Optional<World> getNether();

    /**
     * Retrieves the persisted key associated with the {@code Nether} world, if available.
     *
     * @return an Optional containing the key of the world, or empty if undefined
     */
    @Contract(pure = true)
    Optional<Key> getPersistedNether();

    /**
     * Sets the {@code Nether} world for this {@link LinkTree}.
     *
     * @param world the {@link World} to be set, or null to clear the current world mapping
     * @return true if the world mapping was successfully set or cleared, false otherwise
     */
    @Contract(mutates = "this")
    boolean setNether(@Nullable World world);

    /**
     * Retrieves the {@code End} world associated with this {@link LinkTree}, if available.
     *
     * @return an Optional containing the world, or empty if undefined or not loaded
     */
    @Contract(pure = true)
    Optional<World> getEnd();

    /**
     * Retrieves the persisted key associated with the {@code End} world, if available.
     *
     * @return an Optional containing the key of the world, or empty if undefined
     */
    @Contract(pure = true)
    Optional<Key> getPersistedEnd();

    /**
     * Sets the {@code End} world for this {@link LinkTree}.
     *
     * @param world the {@link World} to be set, or null to clear the current world mapping
     * @return true if the world mapping was successfully set or cleared, false otherwise
     */
    @Contract(mutates = "this")
    boolean setEnd(@Nullable World world);

    /**
     * Determines whether the link tree is empty, signifying that no worlds are associated with it.
     *
     * @return true if the link tree contains no associations, false otherwise
     */
    @Contract(pure = true)
    boolean isEmpty();

    /**
     * Checks if the specified key is contained within the link tree.
     *
     * @param key the key to check for containment within the link tree
     * @return true if the specified key is present in the link tree, false otherwise
     */
    @Contract(pure = true)
    boolean contains(Key key);

    /**
     * Checks if the provided world is contained within the link tree.
     *
     * @param world the world to check for containment within the link tree
     * @return true if the specified world is present in the link tree, false otherwise
     */
    @Contract(pure = true)
    boolean contains(World world);

    /**
     * Removes the specified key from the link tree if it exists.
     *
     * @param key the key associated with the world to be removed from the link tree
     * @return true if the key was successfully removed, false otherwise
     */
    @Contract(mutates = "this")
    boolean remove(Key key);

    /**
     * Removes the specified world from the link tree if it exists.
     *
     * @param world the world to be removed from the link tree
     * @return true if the world was successfully removed, false otherwise
     */
    @Contract(mutates = "this")
    boolean remove(World world);

    /**
     * Retrieves the world mapped to the specified environment, if available.
     *
     * @param environment the environment for which the associated world is to be retrieved
     * @return an Optional containing the associated world, or an empty Optional if none exists
     */
    @Contract(pure = true)
    Optional<World> getWorld(Environment environment);

    /**
     * Retrieves the {@link LinkProvider} associated with this {@link LinkTree}.
     *
     * @return the {@link LinkProvider} managing associations and interactions for this LinkTree
     */
    @Contract(pure = true)
    LinkProvider getLinkProvider();
}
