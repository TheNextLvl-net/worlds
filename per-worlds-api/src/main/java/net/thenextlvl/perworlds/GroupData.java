package net.thenextlvl.perworlds;

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
     */
    @Nullable
    <T> T getGameRule(@NonNull GameRule<T> rule);

    /**
     * Set the given {@link GameRule}'s new value.
     *
     * @param rule the GameRule to update
     * @param newValue the new value
     * @param <T> the value type of the GameRule
     * @return true if the value was successfully set
     */
    <T> boolean setGameRule(@NonNull GameRule<T> rule, @NonNull T value);

    @NonNull
    Difficulty difficulty();

    boolean difficulty(@NonNull Difficulty difficulty);

    @NonNull
    GameMode defaultGameMode();

    boolean defaultGameMode(@NonNull GameMode gameMode);

    @Nullable
    Location spawnLocation();

    boolean spawnLocation(@Nullable Location location);

    boolean rain();

    boolean rain(boolean rain);

    boolean thunder();

    boolean thunder(boolean thunder);

    int time();

    boolean time(int time);
}
