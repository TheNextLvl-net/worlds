package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.data.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@NullMarked
public interface WorldGroup {
    File getDataFolder();

    File getFile();

    GroupSettings getSettings();

    @Unmodifiable
    List<Player> getPlayers();

    Optional<PlayerData> readPlayerData(OfflinePlayer player);

    @Unmodifiable
    Set<Key> getPersistedWorlds();

    @Unmodifiable
    Set<World> getWorlds();

    String getName();

    boolean addWorld(World world);

    boolean containsWorld(World world);

    boolean delete();

    boolean removeWorld(World world);

    boolean writePlayerData(OfflinePlayer player, PlayerData data);

    void loadPlayerData(Player player, boolean position);
    void persist();

    void persistPlayerData();

    void persistPlayerData(Player player);
}
