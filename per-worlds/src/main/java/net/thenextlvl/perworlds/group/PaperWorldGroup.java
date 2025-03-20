package net.thenextlvl.perworlds.group;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.WorldGroup;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public class PaperWorldGroup implements WorldGroup {
    private final @Nullable GameMode gameMode;
    private final GroupSettings settings;
    private final Key key;
    private final Set<World> worlds;

    private PaperWorldGroup(Key key, GroupSettings settings, Set<World> worlds, @Nullable GameMode gameMode) {
        this.gameMode = gameMode;
        this.settings = settings;
        this.key = key;
        this.worlds = worlds;
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
                .toList();
    }

    @Override
    public Optional<GameMode> getGameMode() {
        return Optional.ofNullable(gameMode);
    }

    @Override
    public Optional<ItemStack[]> getEnderChestContents(OfflinePlayer player) {
        return Optional.empty(); // todo: implement
    }

    @Override
    public Optional<ItemStack[]> getInventoryContents(OfflinePlayer player) {
        return Optional.empty(); // todo: implement
    }

    @Override
    public @Unmodifiable Set<World> getWorlds() {
        return Set.copyOf(worlds);
    }

    @Override
    public boolean addWorld(World world) {
        return false; // todo: implement
    }

    @Override
    public boolean containsWorld(World world) {
        return worlds.contains(world);
    }

    @Override
    public boolean removeWorld(World world) {
        return false; // todo: implement
    }

    @Override
    public void loadPlayerData(OfflinePlayer player) {
        // todo: implement
    }

    @Override
    public void persistPlayerData(OfflinePlayer player) {
        // todo: implement
    }

    @Override
    public void setEnderChestContents(OfflinePlayer player, ItemStack[] contents) {
        // todo: implement
    }

    @Override
    public void setInventoryContents(OfflinePlayer player, ItemStack[] contents) {
        // todo: implement
    }

    @Override
    public Key key() {
        return key;
    }

    static class Builder implements WorldGroup.Builder {
        private @Nullable GameMode gameMode;
        private Key key;
        private Set<World> worlds = new HashSet<>();
        private final GroupSettings settings = new PaperGroupSettings();

        Builder(Key key) {
            this.key = key;
        }

        @Override
        public WorldGroup.Builder addWorld(World world) {
            worlds.add(world);
            return this;
        }

        @Override
        public WorldGroup.Builder addWorlds(Collection<World> worlds) {
            this.worlds.addAll(worlds);
            return this;
        }

        @Override
        public WorldGroup.Builder addWorlds(World... worlds) {
            return addWorlds(List.of(worlds));
        }

        @Override
        public WorldGroup.Builder gameMode(@Nullable GameMode gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        @Override
        public WorldGroup.Builder key(Key key) {
            this.key = key;
            return this;
        }

        @Override
        public WorldGroup.Builder setWorlds(Collection<World> worlds) {
            this.worlds = new HashSet<>(worlds);
            return this;
        }

        @Override
        public WorldGroup.Builder setWorlds(World... worlds) {
            return setWorlds(List.of(worlds));
        }

        @Override
        public WorldGroup.Builder settings(Consumer<GroupSettings> settings) {
            settings.accept(this.settings);
            return this;
        }

        @Override
        public WorldGroup build() {
            return new PaperWorldGroup(key, settings, worlds, gameMode);
        }
    }
}
