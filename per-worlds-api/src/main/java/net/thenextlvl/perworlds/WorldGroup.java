package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Keyed;
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
public interface WorldGroup extends Keyed {
    File getDataFolder();

    GroupSettings getSettings();

    @Unmodifiable
    List<Player> getPlayers();

    Optional<PlayerData> readPlayerData(OfflinePlayer player);

    @Unmodifiable
    Set<World> getWorlds();

    boolean addWorld(World world);

    boolean containsWorld(World world);

    boolean removeWorld(World world);

    boolean writePlayerData(OfflinePlayer player, PlayerData data);

    void loadPlayerData(Player player);

    void persistPlayerData(Player player);
}
