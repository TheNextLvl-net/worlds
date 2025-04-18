package net.thenextlvl.perworlds;

import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents a provider responsible for managing and interacting with {@link WorldGroup world groups}.
 * A group contains multiple worlds and is defined alongside its settings and associated data.
 * This interface provides various functionalities to create, retrieve, verify, and remove groups.
 */
@NullMarked
public interface GroupProvider {
    /**
     * Retrieves the data folder associated with the group provider.
     * The data folder is used for storing persistent data relevant to groups.
     *
     * @return the file object pointing to the data folder used by the group provider
     */
    File getDataFolder();

    /**
     * Retrieves an unmodifiable list of all world groups managed by this provider.
     *
     * @return an unmodifiable list of {@link WorldGroup} instances managed by this provider
     */
    @Unmodifiable
    List<WorldGroup> getGroups();

    /**
     * Retrieves a {@link WorldGroup} by its name.
     *
     * @param name the name of the world group to retrieve
     * @return an {@link Optional} containing the {@link WorldGroup} if found, otherwise {@link Optional#empty()}
     */
    Optional<WorldGroup> getGroup(String name);

    /**
     * Retrieves the {@link WorldGroup} to which the specified {@link World} belongs.
     *
     * @param world the {@link World} for which the corresponding group is to be retrieved
     * @return an {@link Optional} containing the {@link WorldGroup} if the specified world is
     *         part of a group, or {@link Optional#empty()} if the world does not belong to any group
     */
    Optional<WorldGroup> getGroup(World world);

    /**
     * Retrieves the unowned world group.
     * <p>
     * The {@link UnownedWorldGroup} represents worlds that do not belong
     * to any explicitly defined {@link WorldGroup}.
     *
     * @return the {@link UnownedWorldGroup} instance representing unassociated worlds
     */
    UnownedWorldGroup getUnownedWorldGroup();

    /**
     * Creates a new {@link WorldGroup} with the specified name, data, settings, and a collection of worlds.
     * The group must have a unique name and cannot conflict with already existing groups.
     *
     * @param name     the name of the group to be created.
     * @param data     a {@link Consumer} to configure the {@link GroupData} for the new group.
     * @param settings a {@link Consumer} to configure the {@link GroupSettings} for the new group.
     * @param worlds   a collection of {@link World} instances that will be part of the group.
     * @return the created {@link WorldGroup} instance.
     * @throws IllegalStateException if a group with the specified name already exists,
     * or if a given world is already part of another group.
     */
    WorldGroup createGroup(String name, Consumer<GroupData> data, Consumer<GroupSettings> settings, Collection<World> worlds) throws IllegalStateException;

    /**
     * Creates a new {@link WorldGroup} with the specified name and a collection of worlds.
     * The group must have a unique name and cannot conflict with already existing groups.
     *
     * @param name     the name of the group to be created.
     * @param worlds   a collection of {@link World} instances that will be part of the group.
     * @return the created {@link WorldGroup} instance.
     * @throws IllegalStateException if a group with the specified name already exists,
     * or if a given world is already part of another group.
     */
    WorldGroup createGroup(String name, Collection<World> worlds) throws IllegalStateException;

    /**
     * Creates a new {@link WorldGroup} with the specified name, data, settings, and a collection of worlds.
     * The group must have a unique name and cannot conflict with already existing groups.
     *
     * @param name     the name of the group to be created.
     * @param data     a {@link Consumer} to configure the {@link GroupData} for the new group.
     * @param settings a {@link Consumer} to configure the {@link GroupSettings} for the new group.
     * @param worlds   an array of {@link World} instances that will be part of the group.
     * @return the created {@link WorldGroup} instance.
     * @throws IllegalStateException if a group with the specified name already exists,
     * or if a given world is already part of another group.
     */
    WorldGroup createGroup(String name, Consumer<GroupData> data, Consumer<GroupSettings> settings, World... worlds) throws IllegalStateException;

    /**
     * Creates a new {@link WorldGroup} with the specified name and a collection of worlds.
     * The group must have a unique name and cannot conflict with already existing groups.
     *
     * @param name     the name of the group to be created.
     * @param worlds   an array of {@link World} instances that will be part of the group.
     * @return the created {@link WorldGroup} instance.
     * @throws IllegalStateException if a group with the specified name already exists,
     * or if a given world is already part of another group.
     */
    WorldGroup createGroup(String name, World... worlds) throws IllegalStateException;

    /**
     * Checks if a group with the specified name exists.
     *
     * @param name the name of the group to check for existence
     * @return {@code true} if a group with the specified name exists, {@code false} otherwise
     */
    boolean hasGroup(String name);

    /**
     * Checks if the specified {@link World} is part of any group managed by this provider.
     *
     * @param world the {@link World} to check for group membership
     * @return {@code true} if the specified world is part of a group, {@code false} otherwise
     */
    boolean hasGroup(World world);

    /**
     * Checks if the specified {@link WorldGroup} is managed by this provider.
     *
     * @param group the {@link WorldGroup} to check for management
     * @return {@code true} if this provider manages the specified group, {@code false} otherwise
     */
    boolean hasGroup(WorldGroup group);

    /**
     * Unregisters a group with the specified name from the provider.
     *
     * @param name the name of the group to be removed
     * @return {@code true} if the group was successfully removed, {@code false} otherwise
     * @see #removeGroup(WorldGroup)
     */
    boolean removeGroup(String name);

    /**
     * Unregisters the specified {@link WorldGroup} from the provider.
     * This operation does not delete any data associated with the group;
     * it only causes the group to be unloaded.
     *
     * @param group the {@link WorldGroup} to be removed
     * @return whether the group was successfully removed from the provider
     */
    boolean removeGroup(WorldGroup group);
}
