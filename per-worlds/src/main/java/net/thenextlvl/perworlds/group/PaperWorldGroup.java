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
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.adapter.GroupDataAdapter;
import net.thenextlvl.perworlds.adapter.GroupSettingsAdapter;
import net.thenextlvl.perworlds.data.PlayerData;
import net.thenextlvl.perworlds.model.PaperPlayerData;
import net.thenextlvl.perworlds.model.config.GroupConfig;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static net.thenextlvl.perworlds.SharedWorlds.ISSUES;

@NullMarked
public class PaperWorldGroup implements WorldGroup {
    protected final PaperGroupProvider groupProvider;

    private final File dataFolder;
    private final File file;
    private final FileIO<GroupConfig> config;
    private final String name;

    public PaperWorldGroup(PaperGroupProvider groupProvider, String name, GroupData data, GroupSettings settings, Set<World> worlds) {
        this.dataFolder = new File(groupProvider.getDataFolder(), name);
        this.file = new File(groupProvider.getDataFolder(), name + ".json");
        this.config = new GsonFile<>(IO.of(file), new GroupConfig(
                worlds.stream().map(Keyed::key).collect(Collectors.toSet()), data, settings
        ), new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(GroupData.class, new GroupDataAdapter())
                .registerTypeHierarchyAdapter(GroupSettings.class, new GroupSettingsAdapter())
                .registerTypeHierarchyAdapter(Key.class, new KeyAdapter.Kyori())
                .registerTypeHierarchyAdapter(Location.class, new LocationAdapter.Complex())
                .registerTypeHierarchyAdapter(World.class, new WorldAdapter.Key())
                .create()).saveIfAbsent();
        this.groupProvider = groupProvider;
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
    public @Unmodifiable Set<Key> getPersistedWorlds() {
        return Set.copyOf(config.getRoot().worlds());
    }

    @Override
    public @Unmodifiable Set<World> getWorlds() {
        return config.getRoot().worlds().stream()
                .map(groupProvider.getServer()::getWorld)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean addWorld(World world) {
        return !groupProvider.hasGroup(world) && config.getRoot().worlds().add(world.key());
    }

    @Override
    public boolean containsWorld(World world) {
        return config.getRoot().worlds().contains(world.key());
    }

    @Override
    public boolean delete() {
        return groupProvider.removeGroup(this) | file.delete() | delete(dataFolder);
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
        return config.getRoot().worlds().remove(world.key());
    }

    @Override
    public Optional<PlayerData> readPlayerData(OfflinePlayer player) {
        var file = new File(getDataFolder(), player.getUniqueId() + ".dat");
        try {
            return readPlayerData(file);
        } catch (EOFException e) {
            groupProvider.getLogger().error("The player data file {} is irrecoverably broken", file.getPath());
            return Optional.empty();
        } catch (Exception e) {
            groupProvider.getLogger().error("Failed to load player data from {}", file.getPath(), e);
            groupProvider.getLogger().error("Please look for similar issues or report this on GitHub: {}", ISSUES);
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
                outputStream.writeTag(null, groupProvider.nbt().toTag(data, PlayerData.class));
                return true;
            }
        } catch (Throwable t) {
            if (backup.exists()) try {
                Files.copy(backup.getPath(), file.getPath(), StandardCopyOption.REPLACE_EXISTING);
                groupProvider.getLogger().warn("Recovered {} from potential data loss", player.getUniqueId());
            } catch (IOException e) {
                groupProvider.getLogger().error("Failed to recover player data {}", player.getUniqueId(), e);
            }
            groupProvider.getLogger().error("Failed to save player data {}", player.getUniqueId(), t);
            groupProvider.getLogger().error("Please look for similar issues or report this on GitHub: {}", ISSUES);
            return false;
        }
    }

    @Override
    public void loadPlayerData(Player player, boolean position) {
        readPlayerData(player).orElseGet(PaperPlayerData::new).apply(getSettings(), player, position);
    }

    @Override
    public void persistPlayerData() {
        getPlayers().forEach(this::persistPlayerData);
    }

    @Override
    public void persistPlayerData(Player player) {
        writePlayerData(player, PaperPlayerData.of(player));
    }

    @Override
    public void persistPlayerData(Player player, Consumer<PlayerData> data) {
        var playerData = PaperPlayerData.of(player);
        data.accept(playerData);
        writePlayerData(player, playerData);
    }

    private Optional<PlayerData> readPlayerData(File file) throws IOException {
        if (!file.exists()) return Optional.empty();
        try (var inputStream = stream(IO.of(file))) {
            return Optional.of(inputStream.readTag()).map(tag ->
                    groupProvider.nbt().fromTag(tag, PlayerData.class));
        } catch (Exception e) {
            var io = IO.of(file.getPath() + "_old");
            if (!io.exists()) throw e;
            groupProvider.getLogger().warn("Failed to load player data from {}", file.getPath(), e);
            groupProvider.getLogger().warn("Falling back to {}", io);
            try (var inputStream = stream(io)) {
                return Optional.of(inputStream.readTag()).map(tag ->
                        groupProvider.nbt().fromTag(tag, PlayerData.class));
            }
        }
    }

    private NBTInputStream stream(IO file) throws IOException {
        return new NBTInputStream(file.inputStream(READ), StandardCharsets.UTF_8);
    }
}
