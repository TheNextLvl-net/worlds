package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public interface WorldGroup extends Keyed {
    File getDataFolder();

    GroupSettings getSettings();

    @Unmodifiable
    List<Player> getPlayers();

    Optional<GameMode> getGameMode();

    Optional<@Nullable ItemStack[]> getEnderChestContents(OfflinePlayer player);

    Optional<@Nullable ItemStack[]> getInventoryContents(OfflinePlayer player);

    @Unmodifiable
    Set<World> getWorlds();

    boolean addWorld(World world);

    boolean containsWorld(World world);

    boolean removeWorld(World world);

    void loadPlayerData(Player player);

    void persistPlayerData(Player player);

    void setEnderChestContents(OfflinePlayer player, @Nullable ItemStack[] contents);

    void setGameMode(@Nullable GameMode gameMode);

    void setInventoryContents(OfflinePlayer player, @Nullable ItemStack[] contents);

    interface Builder {
        Builder addWorld(World world);

        Builder addWorlds(Collection<World> worlds);

        Builder addWorlds(World... worlds);

        Builder gameMode(@Nullable GameMode gameMode);

        Builder key(Key key);

        Builder setWorlds(Collection<World> worlds);

        Builder setWorlds(World... worlds);

        Builder settings(Consumer<GroupSettings> settings);

        WorldGroup build() throws IllegalStateException;
    }
}
