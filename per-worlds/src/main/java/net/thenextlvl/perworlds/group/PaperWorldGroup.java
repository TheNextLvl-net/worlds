package net.thenextlvl.perworlds.group;

import core.io.IO;
import core.nbt.NBTInputStream;
import core.nbt.NBTOutputStream;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.data.PlayerData;
import net.thenextlvl.perworlds.model.PaperPlayerData;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static net.thenextlvl.perworlds.SharedWorlds.ISSUES;

@NullMarked
public class PaperWorldGroup implements WorldGroup {
    private final File dataFolder;
    private final GroupSettings settings;
    private final Key key;
    private final PaperGroupProvider groupProvider;
    private final Set<World> worlds;

    public PaperWorldGroup(PaperGroupProvider groupProvider, Key key, GroupSettings settings, Set<World> worlds) {
        this.dataFolder = new File(groupProvider.getDataFolder(), key.asString());
        this.groupProvider = groupProvider;
        this.key = key;
        this.settings = settings;
        this.worlds = worlds;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public GroupSettings getSettings() {
        return settings;
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
    public @Unmodifiable Set<World> getWorlds() {
        return Set.copyOf(worlds);
    }

    @Override
    public boolean addWorld(World world) {
        return !groupProvider.hasGroup(world) && worlds.add(world);
    }

    @Override
    public boolean containsWorld(World world) {
        return worlds.contains(world);
    }

    @Override
    public boolean removeWorld(World world) {
        return worlds.remove(world);
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
    public void loadPlayerData(Player player) {
        readPlayerData(player).orElseGet(PaperPlayerData::new).apply(settings, player);
    }

    @Override
    public void persistPlayerData(Player player) {
        writePlayerData(player, PaperPlayerData.of(player));
    }

    @Override
    public Key key() {
        return key;
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
