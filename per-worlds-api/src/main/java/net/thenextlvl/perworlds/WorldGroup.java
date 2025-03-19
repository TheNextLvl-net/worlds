package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Keyed;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public interface WorldGroup extends Keyed {
    List<Player> getPlayers();

    List<World> getWorlds();

    Optional<GameMode> getGameMode();

    Optional<ItemStack[]> getEnderChestContents(OfflinePlayer player);

    Optional<ItemStack[]> getInventoryContents(OfflinePlayer player);

    boolean addWorld(World world);

    boolean removeWorld(World world);

    void setEnderChestContents(OfflinePlayer player, ItemStack[] contents);

    void setInventoryContents(OfflinePlayer player, ItemStack[] contents);
}
