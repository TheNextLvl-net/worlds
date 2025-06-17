package net.thenextlvl.perworlds.group;

import net.kyori.adventure.util.TriState;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.data.WorldBorderData;
import net.thenextlvl.perworlds.model.PaperWorldBorderData;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

@NullMarked
public class PaperGroupData implements GroupData {
    private final Map<GameRule<?>, Object> gameRules = new HashMap<>();
    private @Nullable GameMode defaultGameMode = null;
    private @Nullable Location spawnLocation = null;
    private WorldBorderData worldBorder = new PaperWorldBorderData();
    private Difficulty difficulty = Difficulty.NORMAL;
    private TriState hardcore = TriState.NOT_SET;
    private boolean raining = false;
    private boolean thundering = false;
    private int clearWeatherDuration;
    private int rainDuration;
    private int thunderDuration;
    private long time = 0;

    private final GroupProvider provider;

    public PaperGroupData(GroupProvider provider) {
        this.provider = provider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachGameRule(BiConsumer<GameRule<Object>, Object> action) {
        gameRules.forEach((rule, value) -> action.accept((GameRule<Object>) rule, value));
    }

    @Override
    public <T> Optional<T> getGameRule(GameRule<T> rule) {
        var object = gameRules.get(rule);
        if (object == null) return Optional.empty();
        return Optional.of(rule.getType().cast(object));
    }

    @Override
    public <T> boolean setGameRule(GameRule<T> rule, @Nullable T value) {
        if (value == null) return gameRules.remove(rule) != null;
        return !value.equals(gameRules.put(rule, value));
    }

    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public Optional<GameMode> getDefaultGameMode() {
        return Optional.ofNullable(defaultGameMode);
    }

    @Override
    public void setDefaultGameMode(@Nullable GameMode defaultGameMode) {
        this.defaultGameMode = defaultGameMode;
    }

    @Override
    public WorldBorderData getWorldBorder() {
        return worldBorder;
    }

    public void setWorldBorder(WorldBorderData worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public @NonNull Optional<Location> getSpawnLocation() {
        return Optional.ofNullable(spawnLocation);
    }

    @Override
    public void setSpawnLocation(@Nullable Location location) {
        this.spawnLocation = location;
    }

    @Override
    public TriState getHardcore() {
        return hardcore;
    }

    @Override
    public void setHardcore(TriState hardcore) {
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

    @Override
    public GroupProvider getGroupProvider() {
        return provider;
    }
}
