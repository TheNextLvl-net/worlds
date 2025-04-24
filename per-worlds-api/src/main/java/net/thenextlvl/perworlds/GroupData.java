package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.data.WorldBorderData;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface GroupData {
    /**
     * Get the current value for a given {@link GameRule}.
     *
     * @param rule the GameRule to check
     * @param <T> the GameRule's type
     * @return the current value
     * @see #gameRule(GameRule, Object)
     */
    @Nullable
    <T> T gameRule(@NonNull GameRule<T> rule);

    /**
     * Set the given {@link GameRule}'s new value.
     *
     * @param rule the GameRule to update
     * @param <T> the value type of the GameRule
     * @return true if the value was successfully set
     * @see #gameRule(GameRule)
     */
    <T> boolean gameRule(@NonNull GameRule<T> rule, @NonNull T value);

    /**
     * Retrieves the current difficulty level applied to the group.
     *
     * @return the current {@link Difficulty} of the group
     * @see #difficulty(Difficulty)
     */
    @NonNull
    Difficulty difficulty();

    /**
     * Sets the difficulty level for the group.
     * The difficulty level dictates the gameplay challenges, such as
     * mob behavior and damage levels, associated with the group.
     *
     * @param difficulty the new {@link Difficulty} to be set
     * @see #difficulty()
     */
    void difficulty(@NonNull Difficulty difficulty);

    /**
     * Retrieves the default {@link GameMode} for the group.
     *
     * @return the default {@link GameMode} for the group, never null
     * @see #defaultGameMode(GameMode)
     */
    @NonNull
    GameMode defaultGameMode();

    /**
     * Sets the default {@link GameMode} for the group.
     *
     * @param gameMode the {@link GameMode} to be set as the default for the group
     * @see #defaultGameMode()
     */
    void defaultGameMode(@NonNull GameMode gameMode);

    /**
     * Retrieves the {@link WorldBorderData} associated with the group.
     *
     * @return the {@link WorldBorderData} instance representing the world's border configuration
     * @see #worldBorder(WorldBorderData)
     */
    @Nullable
    WorldBorderData worldBorder();

    /**
     * Sets the {@link WorldBorderData} configuration for the group.
     * The world border defines boundaries and related settings such as size,
     * center, and warning distances for the game world.
     *
     * @param worldBorder the {@link WorldBorderData} instance to set, or {@code null}
     *                    to remove any existing world border configuration
     */
    void worldBorder(@Nullable WorldBorderData worldBorder);

    /**
     * Retrieves the spawn location associated with the group.
     *
     * @return the {@link Location} representing the group's spawn location,
     *         or {@code null} if no spawn location is defined
     * @see #spawnLocation(Location)
     */
    @Nullable
    Location spawnLocation();

    /**
     * Sets the spawn location for the group.
     * The spawn location is typically the default location where players appear
     * when spawning within the group.
     *
     * @param location the {@link Location} to set as the group's spawn location.
     *                 Can be {@code null} to unset or clear the spawn location.
     * @see #spawnLocation()
     */
    void spawnLocation(@Nullable Location location);

    /**
     * Checks whether the group is in hardcore mode.
     *
     * @return true if the group is in hardcore mode, false otherwise
     * @see #hardcore(boolean)
     */
    boolean hardcore();

    /**
     * Sets the hardcore mode for the group.
     *
     * @param hardcore true to enable hardcore mode, false to disable it
     * @see #hardcore()
     */
    void hardcore(boolean hardcore);

    /**
     * Retrieves the current rain state for the group.
     * Indicates whether it is currently raining within the group's environment.
     *
     * @return true if it is raining, false otherwise
     * @see #rain(boolean)
     */
    boolean rain();

    /**
     * Sets the rain state for the group.
     * Determines whether it should start or stop raining within the group's environment.
     *
     * @param rain true to enable rain, false to disable it
     * @see #rain()
     */
    void rain(boolean rain);

    /**
     * Checks whether it is currently thundering within the group's environment.
     *
     * @return true if it is thundering, false otherwise
     * @see #thunder(boolean)
     */
    boolean thunder();

    /**
     * Sets the thunder state for the group.
     * Determines whether it should start or stop thundering within the group's environment.
     *
     * @param thunder true to enable thundering, false to disable it
     * @see #thunder()
     */
    void thunder(boolean thunder);

    /**
     * Retrieves the current time value associated with the group.
     *
     * @return the current time as a long
     * @see #time(long)
     */
    long time();

    /**
     * Sets the current time value for the group.
     * The time value typically represents the in-game time within the group's environment.
     *
     * @param time the time value to be set, represented as a long
     * @see #time()
     */
    void time(long time);
}
