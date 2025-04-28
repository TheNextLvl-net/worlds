package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Represents a group of worlds, providing functionality to manage and interact
 * with world-specific and player-specific data, as well as group settings.
 */
@NullMarked
public interface WorldGroup {
    /**
     * Retrieves the data folder of the specific group, used to store persistent data.
     *
     * @return the file object pointing to the data folder for the current group
     */
    File getDataFolder();

    /**
     * Retrieves the configuration file associated with this group.
     * The configuration file contains information such as settings, group name, and associated worlds.
     *
     * @return the configuration file.
     */
    File getConfigFile();

    /**
     * Retrieves the backup file for the configuration settings.
     *
     * @return the backup configuration file.
     */
    File getConfigFileBackup();

    /**
     * Retrieves the group data associated with this group.
     * The group data encapsulates various data attributes and metadata specific to the group.
     *
     * @return the {@link GroupData} object representing the data of this world group
     */
    GroupData getGroupData();

    /**
     * Retrieves the GroupProvider instance associated with this context.
     *
     * @return the GroupProvider instance
     */
    GroupProvider getGroupProvider();

    /**
     * Retrieves the settings for the group.
     * These settings include various configurations and toggles that dictate
     * the behavior and properties specific to the group.
     *
     * @return the {@link GroupSettings} object containing the configurations for the group
     */
    GroupSettings getSettings();

    /**
     * Retrieves a list of all players within the current world group.
     * The returned list is unmodifiable and contains only players that are not NPCs.
     * This method involves a potentially expensive operation and should not be called frequently.
     *
     * @return an unmodifiable list of players within the group
     */
    @Unmodifiable
    List<Player> getPlayers();

    /**
     * Retrieves the spawn location associated with the given offline player.
     * This method determines the appropriate spawn location for the player
     * within the context of the current world group.
     *
     * @param player the offline player for whom the spawn location is being determined
     * @return an {@link Optional} containing the {@link Location} where the player should spawn
     */
    Optional<Location> getSpawnLocation(OfflinePlayer player);

    /**
     * Retrieves the spawn location associated with the provided player data.
     * This method determines the appropriate spawn location based on the data
     * provided for a specific player in the context of the world group.
     *
     * @param data the {@link PlayerData} object containing the player's information
     *             used to determine the spawn location
     * @return an {@link Optional} containing the {@link Location} where the player should spawn
     */
    Optional<Location> getSpawnLocation(PlayerData data);

    /**
     * Retrieves the general spawn location for this world group.
     *
     * @return an {@link Optional} containing the spawn {@link Location}
     */
    Optional<Location> getSpawnLocation();

    /**
     * Retrieves the default spawn world for this world group.
     * The spawn world is the primary world associated with this group
     * where players or entities are typically spawned.
     *
     * @return an {@link Optional} containing the spawn {@link World} if it exists,
     *         or an empty {@link Optional} if no spawn world is defined for the group
     */
    Optional<World> getSpawnWorld();

    /**
     * Reads and retrieves the data associated with the specified offline player.
     *
     * @param player the offline player whose data is to be read
     * @return an {@link Optional} containing the player's data if it exists, or an empty {@link Optional} otherwise
     */
    Optional<? extends PlayerData> readPlayerData(OfflinePlayer player);

    /**
     * Retrieves all persisted keys of the worlds that are members of this group.
     * This method supplements {@link #getWorlds} to account for worlds that are
     * members of the group but may not currently be loaded.
     *
     * @return an unmodifiable set of keys representing the persisted worlds in the group
     */
    @Unmodifiable
    Set<Key> getPersistedWorlds();

    /**
     * Retrieves all loaded worlds that are members of this group.
     * This method involves a potentially expensive operation and should not be called frequently.
     *
     * @return an unmodifiable set containing all loaded worlds associated with this group
     */
    @Unmodifiable
    Set<World> getWorlds();

    /**
     * Retrieves the name of the world group.
     *
     * @return the name of the group as a string
     */
    String getName();

    /**
     * Adds a world to the current world group.
     * This method associates the specified world with the group,
     * enabling shared properties and features as defined by the group settings.
     *
     * @param world the world to be added to the group.
     * @return {@code true} if the world was successfully added to the group,
     *         {@code false} if the world is already part of a group
     */
    boolean addWorld(World world);

    /**
     * Checks if the specified {@link World} is part of this world group.
     *
     * @param world the {@link World} to check for membership in the group
     * @return whether the world is a member of this group
     */
    boolean containsWorld(World world);

    /**
     * Deletes and {@link GroupProvider#removeGroup(WorldGroup) unregisters}
     * the current world group, removing its associated data.
     * This method may involve the deletion of persistent data linked to the group.
     *
     * @return {@code true} if the group was successfully deleted, {@code false} if nothing was changed
     */
    boolean delete();

    /**
     * Checks if the specified offline player has associated persistent data within the group.
     *
     * @param player the offline player to check for associated data
     * @return {@code true} if the player has persistent data in the group, {@code false} otherwise
     */
    boolean hasPlayerData(OfflinePlayer player);

    /**
     * Removes the specified world from this world group.
     * <p>
     * If the world is successfully removed, it will no longer
     * be associated with the group and its settings and data.
     *
     * @param world the world to be removed from the group
     * @return {@code true} if the world was successfully removed from the group,
     *         {@code false} if the world is not part of this group
     */
    boolean removeWorld(World world);

    /**
     * Writes the provided player data for the specified offline player to persistent storage.
     *
     * @param player the offline player whose data is to be written
     * @param data the player data to be stored
     * @return {@code true} if the player's data was successfully written, {@code false} otherwise
     */
    boolean writePlayerData(OfflinePlayer player, PlayerData data);

    /**
     * Loads player data for a specific player.
     * This method only functions if the player is not currently in the process of loading data.
     * <p>
     * This method is the equivalent of calling {@link #loadPlayerData(Player, boolean) loadPlayerData(player, false)}.
     *
     * @param player   the player for whom data is to be loaded
     * @see #loadPlayerData(Player, boolean)
     */
    CompletableFuture<Boolean> loadPlayerData(Player player);

    /**
     * Loads player data for a specific player.
     * This method only functions if the player is not currently in the process of loading data.
     * Optionally, it can also load the player's position data.
     *
     * @param player   the player for whom data is to be loaded
     * @param position whether to load the player's position data
     */
    CompletableFuture<Boolean> loadPlayerData(Player player, boolean position);

    /**
     * Propagates the {@link #getGroupData() group's data} onto the given world.
     *
     * @param world the world whose data is to be updated
     * @throws IllegalArgumentException thrown if the specified world is not part of this group
     */
    void updateWorldData(World world) throws IllegalArgumentException;

    /**
     * Updates the data of the specified world within the group with the given type.
     * <p>
     * The update applies changes according to the specified {@link GroupData.Type}.
     *
     * @param world the world whose data is to be updated
     * @param type the type of update to apply to the world's data
     * @throws IllegalArgumentException if the specified world is not part of this group
     */
    void updateWorldData(World world, GroupData.Type type) throws IllegalArgumentException;

    /**
     * Checks whether data for the specified player is currently being loaded.
     *
     * @param player the player for whom the loading status is to be checked
     * @return {@code true} if the player's data is currently being loaded, {@code false} otherwise
     */
    boolean isLoadingData(Player player);

    /**
     * Persists the settings and worlds of this group to the configuration.
     *
     * @return whether the group could be successfully saved
     */
    boolean persist();

    /**
     * Persists the data of all players within the current world group to persistent storage.
     * This method ensures that any unsaved player-specific data is written to disk,
     * helping to maintain the integrity and consistency of player data across sessions.
     */
    void persistPlayerData();

    /**
     * Persists the data of the specified player to persistent storage.
     *
     * @param player the player whose data is to be persisted
     */
    void persistPlayerData(Player player);

    /**
     * Persists and modifies the data of the specified player using the provided consumer.
     * The method fetches the player's data, allows manipulation through the consumer,
     * and ensures the updated data is saved to persistent storage.
     *
     * @param player the player whose data is to be persisted and modified
     * @param data a {@link Consumer} that manipulates the {@link PlayerData} object
     */
    void persistPlayerData(Player player, Consumer<PlayerData> data);
}
