package net.thenextlvl.perworlds.group;

import net.thenextlvl.perworlds.GroupData;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@NullMarked
public class PaperGroupData implements GroupData {
    private final Map<GameRule<?>, Object> gameRules = new HashMap<>();
    private @Nullable Location spawnLocation = null;
    private Difficulty difficulty = Difficulty.NORMAL;
    private GameMode defaultGameMode = GameMode.SURVIVAL;
    private boolean rain = false;
    private boolean thunder = false;
    private int time = 0;

    @Override
    public <T> @Nullable T getGameRule(GameRule<T> rule) {
        return rule.getType().cast(gameRules.get(rule));
    }

    @Override
    public <T> boolean setGameRule(GameRule<T> rule, @Nullable T value) {
        if (value == null) return gameRules.remove(rule) != null;
        return !value.equals(gameRules.put(rule, value));
    }

    @Override
    public Difficulty difficulty() {
        return difficulty;
    }

    @Override
    public boolean difficulty(Difficulty difficulty) {
        if (this.difficulty.equals(difficulty)) return false;
        this.difficulty = difficulty;
        return true;
    }

    @Override
    public GameMode defaultGameMode() {
        return defaultGameMode;
    }

    @Override
    public boolean defaultGameMode(GameMode gameMode) {
        if (defaultGameMode.equals(gameMode)) return false;
        this.defaultGameMode = gameMode;
        return true;
    }

    @Override
    public @Nullable Location spawnLocation() {
        return spawnLocation;
    }

    @Override
    public boolean spawnLocation(@Nullable Location location) {
        if (Objects.equals(spawnLocation, location)) return false;
        this.spawnLocation = location;
        return true;
    }

    @Override
    public boolean rain() {
        return rain;
    }

    @Override
    public boolean rain(boolean rain) {
        if (this.rain == rain) return false;
        this.rain = rain;
        return true;
    }

    @Override
    public boolean thunder() {
        return thunder;
    }

    @Override
    public boolean thunder(boolean thunder) {
        if (this.thunder == thunder) return false;
        this.thunder = thunder;
        return true;
    }

    @Override
    public int time() {
        return time;
    }

    @Override
    public boolean time(int time) {
        if (this.time == time) return false;
        this.time = time;
        return true;
    }
}
