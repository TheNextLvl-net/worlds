package net.thenextlvl.perworlds.group;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.UnownedWorldGroup;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
public class PaperUnownedWorldGroup extends PaperWorldGroup implements UnownedWorldGroup {
    public PaperUnownedWorldGroup(PaperGroupProvider provider) {
        super(provider, "unowned", new PaperGroupData(provider.getServer()), new PaperGroupSettings(), Set.of());
    }

    @Override
    public boolean delete() {
        var deleted = getConfigFile().delete() | getConfigFileBackup().delete() | delete(getDataFolder());
        if (deleted) getPlayers().forEach(this::loadPlayerData);
        return deleted;
    }

    @Override
    public Stream<World> getWorlds() {
        return provider.getServer().getWorlds().stream()
                .filter(this::containsWorld);
    }

    @Override
    public @Unmodifiable Set<Key> getPersistedWorlds() {
        return getWorlds().map(World::key)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean addWorld(World world) {
        return containsWorld(world);
    }

    @Override
    public boolean removeWorld(World world) {
        return containsWorld(world);
    }

    @Override
    public boolean containsWorld(World world) {
        return !provider.hasGroup(world);
    }
}
