package net.thenextlvl.perworlds;

import net.kyori.adventure.util.TriState;
import net.thenextlvl.perworlds.data.WorldBorderData;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface GroupData {
    /**
     * Iterates over each {@link GameRule} associated with the group and applies the given action.
     *
     * @param action a {@link BiConsumer} to accept each {@link GameRule} and its associated value.
     */
    void forEachGameRule(BiConsumer<GameRule<Object>, Object> action);

    /**
     * Get the current value for a given {@link GameRule}.
     *
     * @param rule the GameRule to check
     * @param <T>  the GameRule's type
     * @return the current value
     * @see World#getGameRuleValue(GameRule)
     * @deprecated use {@link #getGameRule(GameRule)}
     */
    @Nullable
    @Deprecated(forRemoval = true, since = "0.2.2")
    default <T> T gameRule(@NonNull GameRule<T> rule) {
        return getGameRule(rule).orElse(null);
    }

    /**
     * Get the current value for a given {@link GameRule}.
     *
     * @param rule the GameRule to check
     * @param <T>  the GameRule's type
     * @return an {@link Optional} containing the current gamerule value
     * @see World#getGameRuleValue(GameRule)
     */
    @NonNull
    <T> Optional<T> getGameRule(@NonNull GameRule<T> rule);

    /**
     * Set the given {@link GameRule}'s new value.
     *
     * @param rule the GameRule to update
     * @param <T>  the value type of the GameRule
     * @return true if the value was successfully set
     * @see World#setGameRule(GameRule, Object)
     * @deprecated use {@link #setGameRule(GameRule, Object)}
     */
    @Deprecated(forRemoval = true, since = "0.2.2")
    default <T> boolean gameRule(@NonNull GameRule<T> rule, @Nullable T value) {
        return setGameRule(rule, value);
    }

    /**
     * Set the given {@link GameRule}'s new value.
     *
     * @param rule the GameRule to update
     * @param <T>  the value type of the GameRule
     * @return true if the value was successfully set
     * @see World#setGameRule(GameRule, Object)
     */
    <T> boolean setGameRule(@NonNull GameRule<T> rule, @Nullable T value);

    /**
     * Retrieves the current difficulty level applied to the group.
     *
     * @return the current {@link Difficulty} of the group
     * @see World#getDifficulty()
     * @deprecated use {@link #getDifficulty}
     */
    @NonNull
    @Deprecated(forRemoval = true, since = "0.2.2")
    default Difficulty difficulty() {
        return getDifficulty();
    }

    /**
     * Retrieves the current difficulty level applied to the group.
     *
     * @return the current {@link Difficulty} of the group
     * @see World#getDifficulty()
     */
    @NonNull
    Difficulty getDifficulty();

    /**
     * Sets the difficulty level for the group.
     * The difficulty level dictates the gameplay challenges, such as
     * mob behavior and damage levels, associated with the group.
     *
     * @param difficulty the new {@link Difficulty} to be set
     * @see World#setDifficulty(Difficulty)
     * @deprecated use {@link #setDifficulty(Difficulty)}
     */
    @Deprecated(forRemoval = true, since = "0.2.2")
    default void difficulty(@NonNull Difficulty difficulty) {
        setDifficulty(difficulty);
    }

    /**
     * Sets the difficulty level for the group.
     * The difficulty level dictates the gameplay challenges, such as
     * mob behavior and damage levels, associated with the group.
     *
     * @param difficulty the new {@link Difficulty} to be set
     * @see World#setDifficulty(Difficulty)
     */
    void setDifficulty(@NonNull Difficulty difficulty);

    /**
     * Retrieves the default {@link GameMode} for the group.
     *
     * @return the default {@link GameMode} for the group, never null
     * @see #setDefaultGameMode(GameMode)
     * @deprecated use {@link #getDefaultGameMode()}
     */
    @NonNull
    @Deprecated(forRemoval = true, since = "0.2.2")
    default GameMode defaultGameMode() {
        return getDefaultGameMode().orElse(getGroupProvider().getServer().getDefaultGameMode());
    }

    /**
     * Retrieves the default {@link GameMode} for the group.
     *
     * @return an {@link Optional} containing the default {@link GameMode} for the group
     * @see #setDefaultGameMode(GameMode)
     */
    Optional<GameMode> getDefaultGameMode();

    /**
     * Sets the default {@link GameMode} for the group.
     *
     * @param gameMode the {@link GameMode} to be set as the default for the group
     * @see #getDefaultGameMode()
     */
    @Deprecated(forRemoval = true, since = "0.2.2")
    default void defaultGameMode(@Nullable GameMode gameMode) {
        setDefaultGameMode(gameMode);
    }

    /**
     * Sets the default {@link GameMode} for the group.
     *
     * @param gameMode the {@link GameMode} to be set as the default for the group,
     *                 {@code null} to delegate to {@link Server#getDefaultGameMode()}
     * @see #getDefaultGameMode()
     */
    void setDefaultGameMode(@Nullable GameMode gameMode);

    /**
     * Retrieves the {@link WorldBorderData} associated with the group.
     *
     * @return the {@link WorldBorderData} instance representing the world's border configuration
     * @see World#getWorldBorder()
     * @deprecated use {@link #getWorldBorder()}
     */
    @NonNull
    @Deprecated(forRemoval = true, since = "0.2.2")
    default WorldBorderData worldBorder() {
        return getWorldBorder();
    }

    /**
     * Retrieves the {@link WorldBorderData} associated with the group.
     *
     * @return the {@link WorldBorderData} instance representing the world's border configuration
     * @see World#getWorldBorder()
     */
    @NonNull
    WorldBorderData getWorldBorder();

    /**
     * Sets the {@link WorldBorderData} configuration for the group.
     * The world border defines boundaries and related settings such as size,
     * center, and warning distances for the game world.
     *
     * @param worldBorder the {@link WorldBorderData} instance to set, or {@code null}
     *                    to remove any existing world border configuration
     * @see World#getWorldBorder()
     * @deprecated use {@link #getWorldBorder()}
     */
    @Deprecated(forRemoval = true, since = "0.2.2")
    default void worldBorder(@Nullable WorldBorderData worldBorder) {
        if (worldBorder != null) getWorldBorder()
                .centerX(worldBorder.centerX())
                .centerZ(worldBorder.centerZ())
                .size(worldBorder.size())
                .damageAmount(worldBorder.damageAmount())
                .damageBuffer(worldBorder.damageBuffer())
                .warningDistance(worldBorder.warningDistance())
                .warningTime(worldBorder.warningTime())
                .duration(worldBorder.duration());
        else getWorldBorder().reset();
    }

    /**
     * Retrieves the spawn location associated with the group.
     *
     * @return the {@link Location} representing the group's spawn location,
     * or {@code null} if no spawn location is defined
     * @see #setSpawnLocation(Location)
     * @deprecated use {@link #getSpawnLocation()}
     */
    @Nullable
    @Deprecated(forRemoval = true, since = "0.2.2")
    default Location spawnLocation() {
        return getSpawnLocation().orElse(null);
    }

    /**
     * Retrieves the spawn location associated with the group.
     *
     * @return the {@link Location} representing the group's spawn location,
     * or {@code null} if no spawn location is defined
     * @see #setSpawnLocation(Location)
     */
    @NonNull
    Optional<Location> getSpawnLocation();

    /**
     * Sets the spawn location for the group.
     * The spawn location is typically the default location where players appear
     * when spawning within the group.
     *
     * @param location the {@link Location} to set as the group's spawn location.
     *                 Can be {@code null} to unset or clear the spawn location.
     * @see #getSpawnLocation()
     * @deprecated use {@link #setSpawnLocation(Location)}
     */
    @Deprecated(forRemoval = true, since = "0.2.2")
    default void spawnLocation(@Nullable Location location) {
        setSpawnLocation(location);
    }

    /**
     * Sets the spawn location for the group.
     * The spawn location is typically the default location where players appear
     * when spawning within the group.
     *
     * @param location the {@link Location} to set as the group's spawn location.
     *                 Can be {@code null} to unset or clear the spawn location.
     * @see #getSpawnLocation()
     */
    void setSpawnLocation(@Nullable Location location);

    /**
     * Checks whether the group is in hardcore mode.
     *
     * @return true if the group is in hardcore mode, false otherwise
     * @see World#isHardcore()
     * @deprecated use {@link #getHardcore()}
     */
    @Deprecated(forRemoval = true, since = "0.2.2")
    default boolean hardcore() {
        return getHardcore().toBooleanOrElse(getGroupProvider().getServer().isHardcore());
    }

    /**
     * Checks whether the group is in hardcore mode.
     *
     * @return a {@link TriState} indicating whether the group is in hardcore mode
     * @see World#isHardcore()
     */
    TriState getHardcore();

    /**
     * Sets the hardcore mode for the group.
     *
     * @param hardcore true to enable hardcore mode, false to disable it
     * @see World#setHardcore(boolean)
     * @deprecated use {@link #setHardcore(TriState)}
     */
    @Deprecated(forRemoval = true, since = "0.2.2")
    default void hardcore(boolean hardcore) {
        setHardcore(TriState.byBoolean(hardcore));
    }

    /**
     * Sets the hardcore mode for the group.
     *
     * @param hardcore {@link TriState#TRUE} to enable hardcore mode, {@link TriState#FALSE} to disable it,
     *                 and {@link TriState#NOT_SET} to delegate to {@link Server#isHardcore()}
     * @see World#setHardcore(boolean)
     */
    void setHardcore(TriState hardcore);

    /**
     * Retrieves the current rain state for the group.
     * Indicates whether it is currently raining within the group's environment.
     *
     * @return true if it is raining, false otherwise
     * @see World#setStorm(boolean)
     */
    boolean raining();

    /**
     * Sets the rain state for the group.
     * Determines whether it should start or stop raining within the group's environment.
     *
     * @param raining true to enable rain, false to disable it
     * @see World#setStorm(boolean)
     */
    void raining(boolean raining);

    /**
     * Checks whether it is currently thundering within the group's environment.
     *
     * @return true if it is thundering, false otherwise
     * @see World#setThundering(boolean)
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
     * @see World#getFullTime()
     */
    long time();

    /**
     * Sets the current time value for the group.
     * The time value typically represents the in-game time within the group's environment.
     *
     * @param time the time value to be set, represented as a long
     * @see World#setFullTime(long)
     */
    void time(long time);

    /**
     * Retrieves the current instance of the GroupProvider.
     *
     * @return the GroupProvider associated with this object
     */
    GroupProvider getGroupProvider();

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
