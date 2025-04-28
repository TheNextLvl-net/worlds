package net.thenextlvl.perworlds.group;

import core.io.IO;
import core.nbt.NBTInputStream;
import core.nbt.NBTOutputStream;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.data.PlayerData;
import net.thenextlvl.perworlds.model.PaperPlayerData;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static net.thenextlvl.perworlds.SharedWorlds.ISSUES;

@NullMarked
public class PaperWorldGroup implements WorldGroup {
    public static final String LOADING_METADATA_KEY = "perworlds_loading_data";
    protected final PaperGroupProvider provider;

    private final File dataFolder;
    private final File configFile;
    private final File configFileBackup;
    private final GroupConfig config;
    private final String name;

    public PaperWorldGroup(PaperGroupProvider provider, String name, GroupData data, GroupSettings settings, Set<World> worlds) {
        this.name = name;
        this.provider = provider;
        this.dataFolder = new File(provider.getDataFolder(), name);
        this.configFile = new File(provider.getDataFolder(), name + ".dat");
        this.configFileBackup = new File(provider.getDataFolder(), name + ".dat_old");
        this.config = readConfig().orElseGet(() -> new GroupConfig(
                worlds.stream().map(Keyed::key).collect(Collectors.toSet()), data, settings
        ));
    }

    private Optional<GroupConfig> readConfig() {
        try {
            return readFile(configFile, configFileBackup, GroupConfig.class);
        } catch (EOFException e) {
            provider.getLogger().error("The world group config file {} is irrecoverably broken", configFile.getPath());
            return Optional.empty();
        } catch (Exception e) {
            provider.getLogger().error("Failed to load world group data from {}", configFile.getPath(), e);
            provider.getLogger().error("Please look for similar issues or report this on GitHub: {}", ISSUES);
            return Optional.empty();
        }
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    @Override
    public File getConfigFileBackup() {
        return configFileBackup;
    }

    @Override
    public GroupData getGroupData() {
        return config.data();
    }

    @Override
    public PaperGroupProvider getGroupProvider() {
        return provider;
    }

    @Override
    public GroupSettings getSettings() {
        return config.settings();
    }

    @Override
    public @Unmodifiable List<Player> getPlayers() {
        return getWorlds()
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
                .or(() -> getWorlds().min(this::compare));
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
        return Set.copyOf(config.worlds());
    }

    @Override
    public @Unmodifiable Stream<World> getWorlds() {
        return config.worlds().stream()
                .map(provider.getServer()::getWorld)
                .filter(Objects::nonNull);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean addWorld(World world) {
        if (provider.hasGroup(world)) return false;
        var previous = provider.getGroup(world).orElse(provider.getUnownedWorldGroup());
        if (!config.worlds().add(world.key())) return false;
        world.getPlayers().forEach(previous::persistPlayerData);
        world.getPlayers().forEach(this::loadPlayerData);
        updateWorldData(world);
        return true;
    }

    @Override
    public boolean containsWorld(World world) {
        return config.worlds().contains(world.key());
    }

    @Override
    public boolean delete() {
        return provider.removeGroup(this) | configFile.delete() | configFileBackup.delete() | delete(dataFolder);
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
    public boolean persist() {
        try {
            var file = IO.of(configFile);
            if (file.exists()) Files.move(file.getPath(), configFileBackup.toPath(), REPLACE_EXISTING);
            else file.createParents();
            try (var outputStream = new NBTOutputStream(
                    file.outputStream(WRITE, CREATE, TRUNCATE_EXISTING),
                    StandardCharsets.UTF_8
            )) {
                outputStream.writeTag(null, provider.nbt().toTag(config));
                return true;
            }
        } catch (Throwable t) {
            if (configFileBackup.exists()) try {
                Files.copy(configFileBackup.toPath(), configFile.toPath(), REPLACE_EXISTING);
                provider.getLogger().warn("Recovered {} from potential data loss", configFile.getPath());
            } catch (IOException e) {
                provider.getLogger().error("Failed to recover world group config {}", configFile.getPath(), e);
            }
            provider.getLogger().error("Failed to save world group config {}", configFile.getPath(), t);
            provider.getLogger().error("Please look for similar issues or report this on GitHub: {}", ISSUES);
            return false;
        }
    }

    @Override
    public boolean removeWorld(World world) {
        if (!config.worlds().remove(world.key())) return false;
        world.getPlayers().forEach(provider.getUnownedWorldGroup()::loadPlayerData);
        provider.getUnownedWorldGroup().updateWorldData(world);
        return true;
    }

    @Override
    public boolean removeWorld(Key key) {
        var world = provider.getServer().getWorld(key);
        return world != null ? removeWorld(world) : config.worlds().remove(key);
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
            if (file.exists()) Files.move(file.getPath(), backup.getPath(), REPLACE_EXISTING);
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
                Files.copy(backup.getPath(), file.getPath(), REPLACE_EXISTING);
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
        player.setMetadata(LOADING_METADATA_KEY, new FixedMetadataValue(provider.getPlugin(), null));
        return readPlayerData(player).orElseGet(PaperPlayerData::new).load(player, this, position)
                .whenComplete((success, throwable) -> player.removeMetadata(LOADING_METADATA_KEY, provider.getPlugin()))
                .exceptionally(throwable -> {
                    provider.getLogger().error("Failed to load group data for player {}", player.getName(), throwable);
                    provider.getLogger().error("Please look for similar issues or report this on GitHub: {}", ISSUES);
                    player.kick(Component.text("Failed to load group data", NamedTextColor.RED));
                    return false;
                });
    }

    @Override
    public void updateWorldData(World world) {
        provider.getServer().getGlobalRegionScheduler().run(provider.getPlugin(), task -> {
            for (var type : GroupData.Type.values()) updateWorldData(world, type);
        });
    }

    @Override
    public void updateWorldData(World world, GroupData.Type type) {
        if (isEnabled(type)) switch (type) {
            case DIFFICULTY -> world.setDifficulty(getGroupData().difficulty());
            case TIME -> world.setFullTime(getGroupData().time());
            case GAME_RULE -> applyGameRules(world);
            case WORLD_BORDER -> applyWorldBorder(world);
            case HARDCORE -> world.setHardcore(getGroupData().hardcore());
            case WEATHER -> applyWeather(world);
        }
    }

    private boolean isEnabled(GroupData.Type type) {
        return switch (type) {
            case DEFAULT_GAME_MODE -> getSettings().gameMode();
            case DIFFICULTY, HARDCORE -> getSettings().difficulty();
            case GAME_RULE -> getSettings().gameRules();
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
        Arrays.stream(world.getGameRules()).map(GameRule::getByName)
                .map(gameRule -> ((GameRule<Object>) gameRule)).filter(Objects::nonNull)
                .forEach(rule -> Optional.ofNullable(getGroupData().gameRule(rule))
                        .or(() -> Optional.ofNullable(world.getGameRuleDefault(rule)))
                        .ifPresent(value -> world.setGameRule(rule, value)));
    }

    private void applyWorldBorder(World world) {
        var border = getGroupData().worldBorder();
        var worldBorder = world.getWorldBorder();
        worldBorder.setSize(border.size(), TimeUnit.MILLISECONDS, border.duration());
        worldBorder.setCenter(border.centerX(), border.centerZ());
        worldBorder.setDamageAmount(border.damageAmount());
        worldBorder.setDamageBuffer(border.damageBuffer());
        worldBorder.setWarningDistance(border.warningDistance());
        worldBorder.setWarningTime(border.warningTime());
    }

    @Override
    public boolean isLoadingData(Player player) {
        return player.hasMetadata(LOADING_METADATA_KEY);
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
        return readFile(file, new File(file.getPath() + "_old"), PaperPlayerData.class);
    }

    private <T> Optional<T> readFile(File file, File backup, Class<T> type) throws IOException {
        if (!file.exists()) return Optional.empty();
        try (var inputStream = stream(IO.of(file))) {
            return Optional.of(inputStream.readTag()).map(tag -> provider.nbt().fromTag(tag, type));
        } catch (Exception e) {
            if (!backup.exists()) throw e;
            provider.getLogger().warn("Failed to load player data from {}", file.getPath(), e);
            provider.getLogger().warn("Falling back to {}", backup.getPath());
            try (var inputStream = stream(IO.of(backup))) {
                return Optional.of(inputStream.readTag()).map(tag -> provider.nbt().fromTag(tag, type));
            }
        }
    }

    private NBTInputStream stream(IO file) throws IOException {
        return new NBTInputStream(file.inputStream(READ), StandardCharsets.UTF_8);
    }
}
