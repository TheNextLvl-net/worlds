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
    public <T> @Nullable T gameRule(GameRule<T> rule) {
        return rule.getType().cast(gameRules.get(rule));
    }

    @Override
    public <T> boolean gameRule(GameRule<T> rule, @Nullable T value) {
        if (value == null) return gameRules.remove(rule) != null;
        return !value.equals(gameRules.put(rule, value));
    }

    @Override
    public Difficulty difficulty() {
        return difficulty;
    }

    @Override
    public void difficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public GameMode defaultGameMode() {
        return defaultGameMode;
    }

    @Override
    public void defaultGameMode(GameMode gameMode) {
        this.defaultGameMode = gameMode;
    }

    @Override
    public @Nullable Location spawnLocation() {
        return spawnLocation;
    }

    @Override
    public void spawnLocation(@Nullable Location location) {
        this.spawnLocation = location;
    }

    @Override
    public boolean rain() {
        return rain;
    }

    @Override
    public void rain(boolean rain) {
        this.rain = rain;
    }

    @Override
    public boolean thunder() {
        return thunder;
    }

    @Override
    public void thunder(boolean thunder) {
        this.thunder = thunder;
    }

    @Override
    public int time() {
        return time;
    }

    @Override
    public void time(int time) {
        this.time = time;
    }
}
