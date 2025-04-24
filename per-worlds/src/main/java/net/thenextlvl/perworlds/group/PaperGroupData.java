package net.thenextlvl.perworlds.group;

import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.data.WorldBorderData;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Server;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public class PaperGroupData implements GroupData {
    private final Map<GameRule<?>, Object> gameRules = new HashMap<>();
    private @Nullable Location spawnLocation = null;
    private @Nullable WorldBorderData worldBorder = null;
    private Difficulty difficulty = Difficulty.NORMAL;
    private GameMode defaultGameMode;
    private boolean hardcore;
    private boolean raining = false;
    private boolean thundering = false;
    private int clearWeatherDuration;
    private int rainDuration;
    private int thunderDuration;
    private long time = 0;

    public PaperGroupData(Server server) {
        // this.defaultGameMode = server.getDefaultGameMode(); // todo: load after overworld? throws npe
        this.defaultGameMode = GameMode.SURVIVAL;
        this.hardcore = server.isHardcore();
    }

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
        return hardcore ? Difficulty.HARD : difficulty;
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
    public @Nullable WorldBorderData worldBorder() {
        return worldBorder;
    }

    @Override
    public void worldBorder(@Nullable WorldBorderData worldBorder) {
        this.worldBorder = worldBorder;
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
    public boolean hardcore() {
        return hardcore;
    }

    @Override
    public void hardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    @Override
    public boolean raining() {
        return raining;
    }

    @Override
    public void raining(boolean raining) {
        this.raining = raining;
    }

    @Override
    public boolean thundering() {
        return thundering;
    }

    @Override
    public void thundering(boolean thundering) {
        this.thundering = thundering;
    }

    @Override
    public int clearWeatherDuration() {
        return clearWeatherDuration;
    }

    @Override
    public void clearWeatherDuration(int duration) {
        this.clearWeatherDuration = duration;
    }

    @Override
    public int thunderDuration() {
        return thunderDuration;
    }

    @Override
    public void thunderDuration(int duration) {
        this.thunderDuration = duration;
    }

    @Override
    public int rainDuration() {
        return rainDuration;
    }

    @Override
    public void rainDuration(int duration) {
        this.rainDuration = duration;
    }

    @Override
    public long time() {
        return time;
    }

    @Override
    public void time(long time) {
        this.time = time;
    }
}
