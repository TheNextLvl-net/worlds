package net.thenextlvl.perworlds.group;

import com.google.gson.GsonBuilder;
import core.file.FileIO;
import core.file.format.GsonFile;
import core.io.IO;
import core.nbt.NBTInputStream;
import core.nbt.NBTOutputStream;
import core.paper.adapters.key.KeyAdapter;
import core.paper.adapters.world.LocationAdapter;
import core.paper.adapters.world.WorldAdapter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.adapter.GroupDataAdapter;
import net.thenextlvl.perworlds.adapter.GroupSettingsAdapter;
import net.thenextlvl.perworlds.data.PlayerData;
import net.thenextlvl.perworlds.model.PaperPlayerData;
import net.thenextlvl.perworlds.model.PaperWorldBorderData;
import net.thenextlvl.perworlds.model.config.GroupConfig;
import org.bukkit.GameRule;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static net.thenextlvl.perworlds.SharedWorlds.ISSUES;

@NullMarked
public class PaperWorldGroup implements WorldGroup {
    protected final PaperGroupProvider provider;

    private final File dataFolder;
    private final File file;
    private final FileIO<GroupConfig> config;
    private final String name;

    public PaperWorldGroup(PaperGroupProvider provider, String name, GroupData data, GroupSettings settings, Set<World> worlds) {
        this.dataFolder = new File(provider.getDataFolder(), name);
        this.file = new File(provider.getDataFolder(), name + ".json");
        this.config = new GsonFile<>(IO.of(file), new GroupConfig(
                worlds.stream().map(Keyed::key).collect(Collectors.toSet()), data, settings
        ), new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(GroupData.class, new GroupDataAdapter(provider.getServer()))
                .registerTypeHierarchyAdapter(GroupSettings.class, new GroupSettingsAdapter())
                .registerTypeHierarchyAdapter(Key.class, new KeyAdapter.Kyori())
                .registerTypeHierarchyAdapter(Location.class, new LocationAdapter.Complex())
                .registerTypeHierarchyAdapter(World.class, new WorldAdapter.Key())
                .create()).saveIfAbsent();
        this.provider = provider;
        this.name = name;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public GroupData getGroupData() {
        return config.getRoot().data();
    }

    @Override
    public GroupSettings getSettings() {
        return config.getRoot().settings();
    }

    @Override
    public @Unmodifiable List<Player> getPlayers() {
        return getWorlds().stream()
                .map(World::getPlayers)
                .flatMap(List::stream)
                .filter(player -> !player.hasMetadata("NPC"))
                .toList();
    }

    @Override
    public Optional<Location> getSpawnLocation(OfflinePlayer player) {
        return readPlayerData(player).flatMap(this::getSpawnLocation);
    }

    @Override
    public Optional<Location> getSpawnLocation(PlayerData data) {
        return Optional.ofNullable(data.lastLocation())
                .filter(location -> getSettings().lastLocation())
                .or(this::getSpawnLocation);
    }

    @Override
    public Optional<Location> getSpawnLocation() {
        return Optional.ofNullable(getGroupData().spawnLocation())
                .or(() -> getSpawnWorld().map(World::getSpawnLocation));
    }

    @Override
    public Optional<World> getSpawnWorld() {
        return Optional.ofNullable(getGroupData().spawnLocation())
                .map(Location::getWorld)
                .or(() -> getWorlds().stream().min(this::compare));
    }

    private int compare(World world, World other) {
        var x = getPriority(world.getEnvironment());
        var y = getPriority(other.getEnvironment());
        return Integer.compare(x, y);
    }

    private int getPriority(Environment environment) {
        return switch (environment) {
            case NORMAL -> 0;
            case NETHER -> 1;
            case THE_END -> 2;
            default -> 3;
        };
    }

    @Override
    public @Unmodifiable Set<Key> getPersistedWorlds() {
        return Set.copyOf(config.getRoot().worlds());
    }

    @Override
    public @Unmodifiable Set<World> getWorlds() {
        return config.getRoot().worlds().stream()
                .map(provider.getServer()::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean addWorld(World world) {
        if (provider.hasGroup(world)) return false;
        var previous = provider.getGroup(world).orElse(provider.getUnownedWorldGroup());
        if (!config.getRoot().worlds().add(world.key())) return false;
        world.getPlayers().forEach(previous::persistPlayerData);
        world.getPlayers().forEach(this::loadPlayerData); // todo: do we need blocking?
        return true;
    }

    @Override
    public boolean containsWorld(World world) {
        return config.getRoot().worlds().contains(world.key());
    }

    @Override
    public boolean delete() {
        return provider.removeGroup(this) | file.delete() | delete(dataFolder);
    }

    @Override
    public boolean hasPlayerData(OfflinePlayer player) {
        return new File(getDataFolder(), player.getUniqueId() + ".dat").exists();
    }

    protected boolean delete(File file) {
        var files = file.listFiles();
        return (files == null || Arrays.stream(files).allMatch(this::delete)) | file.delete();
    }

    @Override
    public void persist() {
        config.save();
    }

    @Override
    public boolean removeWorld(World world) {
        if (!config.getRoot().worlds().remove(world.key())) return false;
        world.getPlayers().forEach(provider.getUnownedWorldGroup()::loadPlayerData); // todo: do we need blocking?
        return true;
    }

    @Override
    public Optional<PaperPlayerData> readPlayerData(OfflinePlayer player) {
        var file = new File(getDataFolder(), player.getUniqueId() + ".dat");
        try {
            return readPlayerData(file);
        } catch (EOFException e) {
            provider.getLogger().error("The player data file {} is irrecoverably broken", file.getPath());
            return Optional.empty();
        } catch (Exception e) {
            provider.getLogger().error("Failed to load player data from {}", file.getPath(), e);
            provider.getLogger().error("Please look for similar issues or report this on GitHub: {}", ISSUES);
            return Optional.empty();
        }
    }

    @Override
    public boolean writePlayerData(OfflinePlayer player, PlayerData data) {
        var file = IO.of(new File(getDataFolder(), player.getUniqueId() + ".dat"));
        var backup = IO.of(new File(getDataFolder(), player.getUniqueId() + ".dat_old"));
        try {
            if (file.exists()) Files.move(file.getPath(), backup.getPath(), StandardCopyOption.REPLACE_EXISTING);
            else file.createParents();
            try (var outputStream = new NBTOutputStream(
                    file.outputStream(WRITE, CREATE, TRUNCATE_EXISTING),
                    StandardCharsets.UTF_8
            )) {
                outputStream.writeTag(null, provider.nbt().toTag(data, PaperPlayerData.class));
                return true;
            }
        } catch (Throwable t) {
            if (backup.exists()) try {
                Files.copy(backup.getPath(), file.getPath(), StandardCopyOption.REPLACE_EXISTING);
                provider.getLogger().warn("Recovered {} from potential data loss", player.getUniqueId());
            } catch (IOException e) {
                provider.getLogger().error("Failed to recover player data {}", player.getUniqueId(), e);
            }
            provider.getLogger().error("Failed to save player data {}", player.getUniqueId(), t);
            provider.getLogger().error("Please look for similar issues or report this on GitHub: {}", ISSUES);
            return false;
        }
    }

    @Override
    public CompletableFuture<Boolean> loadPlayerData(Player player) {
        return loadPlayerData(player, false);
    }

    @Override
    public CompletableFuture<Boolean> loadPlayerData(Player player, boolean position) {
        if (isLoadingData(player)) return CompletableFuture.completedFuture(false);
        player.setMetadata("loading", new FixedMetadataValue(provider.getPlugin(), null));
        return readPlayerData(player).orElseGet(PaperPlayerData::new).load(player, this, position)
                .whenComplete((success, throwable) -> player.removeMetadata("loading", provider.getPlugin()))
                .exceptionally(throwable -> {
                    provider.getLogger().error("Failed to load group data for player {}", player.getName(), throwable);
                    provider.getLogger().error("Please look for similar issues or report this on GitHub: {}", ISSUES);
                    player.kick(Component.text("Failed to load group data", NamedTextColor.RED));
                    return false;
                });
    }

    @Override
    public void updateWorldData(World world) {
        for (var type : GroupData.Type.values()) updateWorldData(world, type);
    }

    // todo: update on on world add/remove/unload
    @Override
    public void updateWorldData(World world, GroupData.Type type) {
        if (isEnabled(type)) switch (type) {
            case DIFFICULTY -> world.setDifficulty(getGroupData().difficulty());
            case TIME -> world.setTime(getGroupData().time());
            case GAME_RULE -> applyGameRules(world);
            case WORLD_BORDER -> applyWorldBorder(world);
            case HARDCORE -> world.setHardcore(getGroupData().hardcore());
            case WEATHER -> applyWeather(world);
        }
    }

    private boolean isEnabled(GroupData.Type type) {
        return switch (type) {
            case DEFAULT_GAME_MODE -> getSettings().gameMode();
            case DIFFICULTY -> getSettings().difficulty();
            case GAME_RULE -> getSettings().gameRules();
            case HARDCORE -> getSettings().hardcore();
            case SPAWN_LOCATION -> true;
            case TIME -> getSettings().time();
            case WEATHER -> getSettings().weather();
            case WORLD_BORDER -> getSettings().worldBorder();
        };
    }

    private void applyWeather(World world) {
        world.setStorm(getGroupData().raining());
        world.setThundering(getGroupData().thundering());
        world.setClearWeatherDuration(getGroupData().clearWeatherDuration());
        world.setThunderDuration(getGroupData().thunderDuration());
        world.setWeatherDuration(getGroupData().rainDuration());
    }

    @SuppressWarnings("unchecked")
    private void applyGameRules(World world) {
        Arrays.stream(GameRule.values())
                .map(rule -> (GameRule<Object>) rule)
                .forEach(rule -> {
                    var value = getGroupData().gameRule(rule);
                    if (value != null) world.setGameRule(rule, value);
                });
    }

    private void applyWorldBorder(World world) {
        var border = Optional.ofNullable(getGroupData().worldBorder())
                .orElseGet(PaperWorldBorderData::new);
        var worldBorder = world.getWorldBorder();
        worldBorder.setSize(border.size());
        worldBorder.setCenter(border.centerX(), border.centerZ());
        worldBorder.setDamageAmount(border.damageAmount());
        worldBorder.setDamageBuffer(border.damageBuffer());
        worldBorder.setWarningDistance(border.warningDistance());
        worldBorder.setWarningTime(border.warningTime());
    }

    @Override
    public boolean isLoadingData(Player player) {
        return player.hasMetadata("loading");
    }

    @Override
    public void persistPlayerData() {
        getPlayers().forEach(this::persistPlayerData);
    }

    @Override
    public void persistPlayerData(Player player) {
        writePlayerData(player, PaperPlayerData.of(player, this));
    }

    @Override
    public void persistPlayerData(Player player, Consumer<PlayerData> data) {
        var playerData = PaperPlayerData.of(player, this);
        data.accept(playerData);
        writePlayerData(player, playerData);
    }

    private Optional<PaperPlayerData> readPlayerData(File file) throws IOException {
        if (!file.exists()) return Optional.empty();
        try (var inputStream = stream(IO.of(file))) {
            return Optional.of(inputStream.readTag()).map(tag ->
                    provider.nbt().fromTag(tag, PaperPlayerData.class));
        } catch (Exception e) {
            var io = IO.of(file.getPath() + "_old");
            if (!io.exists()) throw e;
            provider.getLogger().warn("Failed to load player data from {}", file.getPath(), e);
            provider.getLogger().warn("Falling back to {}", io);
            try (var inputStream = stream(io)) {
                return Optional.of(inputStream.readTag()).map(tag ->
                        provider.nbt().fromTag(tag, PaperPlayerData.class));
            }
        }
    }

    private NBTInputStream stream(IO file) throws IOException {
        return new NBTInputStream(file.inputStream(READ), StandardCharsets.UTF_8);
    }
}
