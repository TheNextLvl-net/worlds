package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.data.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
     * @return the file object representing the configuration file for the current group
     */
    File getFile();


    GroupData getGroupData();

    /**
     * Retrieves the settings for the group.
     * These settings include various configurations and toggles that dictate
     * the behavior and properties specific to the group.
     *
     * @return the {@code GroupSettings} object containing the configurations for the group
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
     * Reads and retrieves the data associated with the specified offline player.
     *
     * @param player the offline player whose data is to be read
     * @return an {@code Optional} containing the player's data if it exists, or an empty {@code Optional} otherwise
     */
    Optional<PlayerData> readPlayerData(OfflinePlayer player);

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
     * Checks if the specified {@code World} is part of this world group.
     *
     * @param world the {@code World} to check for membership in the group
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
     * Loads the saved data for the specified player and optionally restores their position, motion, and fall distance.
     *
     * @param player the player whose data is to be loaded
     * @param position indicates whether to teleport the player to the saved position, apply motion, and restore fall distance
     */
    void loadPlayerData(Player player, boolean position);

    /**
     * Persists the settings and worlds of this group to the configuration.
     */
    void persist();

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
