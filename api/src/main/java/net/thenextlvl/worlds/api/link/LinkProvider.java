package net.thenextlvl.worlds.api.link;

import net.kyori.adventure.key.Key;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * LinkProvider defines an interface for managing and retrieving associations
 * between worlds and their respective {@link LinkTree} structures.
 * It allows querying linked worlds, establishing links, and managing portal relationships.
 *
 * @since 3.0.0
 */
@NullMarked
@ApiStatus.NonExtendable
public interface LinkProvider {
    /**
     * Retrieves all {@link LinkTree} instances managed by the provider.
     *
     * @return a stream of all {@link LinkTree} instances
     */
    @Unmodifiable
    @Contract(pure = true)
    Stream<LinkTree> getLinkTrees();

    /**
     * Retrieves the {@link LinkTree} associated with the specified key, if available.
     *
     * @param key the key for which the associated LinkTree is to be retrieved
     * @return an Optional containing the associated LinkTree
     */
    @Contract(pure = true)
    Optional<LinkTree> getLinkTree(Key key);

    /**
     * Retrieves the {@link LinkTree} associated with the provided world, if available.
     *
     * @param world the world for which the associated LinkTree is to be retrieved
     * @return an Optional containing the associated LinkTree
     */
    @Contract(pure = true)
    Optional<LinkTree> getLinkTree(World world);

    /**
     * Retrieves the target world associated with the specified source world, based on the given portal type.
     * This method determines the destination world depending on the portal relationship and the environment
     * of the source world.
     *
     * @param world the source world for which the target world is determined
     * @param type  the type of portal that dictates the relationship between worlds (e.g., NETHER, ENDER)
     * @return an Optional containing the target world if available
     */
    @Contract(pure = true)
    Optional<World> getTarget(World world, PortalType type);

    /**
     * Establishes a link between the specified source and destination world.
     * The link is created depending on the type of the destination world's environment.
     * <p>
     * The environment of the {@code source} world has to be {@link World.Environment#NORMAL NORMAL}<br>
     * The environment of the {@code target} world has to be either {@link World.Environment#NETHER NETHER}
     * or {@link World.Environment#THE_END THE_END}<br>
     * The {@code target} world may not be associated with a link tree
     *
     * @param source the source world from which the link is created
     * @param target the target world to which the link is made
     * @return true if the link was successfully created; false otherwise
     * @see #hasLinkTree(World)
     */
    boolean link(World source, World target);

    /**
     * Removes the link between the specified source and target if it exists.
     * The operation ensures that the link is only removed if it is present
     * in the associated link structure of the source world.
     *
     * @param source the key representing the source of the link
     * @param target the key representing the target of the link
     * @return true if the link was successfully removed; false otherwise
     */
    boolean unlink(Key source, Key target);

    /**
     * Removes the link between the specified source and target worlds if it exists.
     * The operation ensures that the link is only removed if it is present
     * in the associated link structure of the source world.
     *
     * @param source the source world from which the link is to be removed
     * @param target the target world for which the link is to be removed
     * @return true if the link was successfully removed; false otherwise
     */
    boolean unlink(World source, World target);

    /**
     * Checks if the specified world is associated with a {@link LinkTree}.
     *
     * @param key the key to check for an associated LinkTree
     * @return whether the specified key has an associated LinkTree
     */
    @Contract(pure = true)
    boolean hasLinkTree(Key key);

    /**
     * Checks if the specified world is associated with a LinkTree.
     *
     * @param world the world to check for an associated LinkTree
     * @return whether the specified world has an associated LinkTree
     */
    @Contract(pure = true)
    boolean hasLinkTree(World world);
}
