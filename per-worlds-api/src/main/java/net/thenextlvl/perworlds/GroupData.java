package net.thenextlvl.perworlds;

import net.thenextlvl.perworlds.data.WorldBorderData;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
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
    boolean raining();

    /**
     * Sets the rain state for the group.
     * Determines whether it should start or stop raining within the group's environment.
     *
     * @param rain true to enable rain, false to disable it
     * @see #rain()
     */
    void raining(boolean raining);

    /**
     * Checks whether it is currently thundering within the group's environment.
     *
     * @return true if it is thundering, false otherwise
     * @see #thunder(boolean)
     */
    boolean thundering();

    /**
     * Sets the thunder state for the group.
     * Determines whether it should start or stop thundering within the group's environment.
     *
     * @param thundering true to enable thundering, false to disable it
     * @see World#setThundering(boolean)
     */
    void thundering(boolean thundering);

    /**
     * Retrieves the duration for which the weather will remain clear in the group.
     * The duration is specified in ticks and indicates how long the clear weather will last.
     *
     * @return the remaining duration of clear weather in ticks
     * @see World#getClearWeatherDuration()
     */
    int clearWeatherDuration();

    /**
     * Sets the duration for clear weather in the group.
     * The duration specifies how long the clear weather should last
     * before transitioning to a different weather state.
     *
     * @param duration the number of ticks for the clear weather duration
     * @see World#setClearWeatherDuration(int)
     */
    void clearWeatherDuration(int duration);

    /**
     * Retrieves the duration for which it will continue thundering in the group.
     * The duration is specified in ticks and indicates how long the current thunderstorm will last.
     *
     * @return the remaining duration of the thunderstorm in ticks
     * @see World#getThunderDuration()
     */
    int thunderDuration();

    /**
     * Sets the duration for thundering in the group.
     * The duration specifies how long the thunder should last
     * before transitioning to a different weather state.
     *
     * @param duration the number of ticks for the thundering duration
     * @see World#setThunderDuration(int)
     */
    void thunderDuration(int duration);

    /**
     * Retrieves the duration for which it will continue raining in the group.
     * The duration is specified in ticks and indicates how long the current rain session will last.
     *
     * @return the remaining duration of the rain in ticks
     * @see World#getWeatherDuration()
     */
    int rainDuration();

    /**
     * Sets the duration for raining in the group.
     * The duration specifies how long the rain should last
     * before transitioning to a different weather state.
     *
     * @param duration the number of ticks for the raining duration
     * @see World#setWeatherDuration(int)
     */
    void rainDuration(int duration);

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

    /**
     * Represents various types of group data that can be manipulated within the game.
     * Each enum constant corresponds to a specific configurable or retrievable attribute within a {@link WorldGroup}.
     */
    enum Type {
        DEFAULT_GAME_MODE,
        DIFFICULTY,
        GAME_RULE,
        HARDCORE,
        SPAWN_LOCATION,
        TIME,
        WEATHER,
        WORLD_BORDER,
    }
}
