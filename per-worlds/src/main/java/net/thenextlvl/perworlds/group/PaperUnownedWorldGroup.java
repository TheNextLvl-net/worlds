package net.thenextlvl.perworlds.group;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.UnownedWorldGroup;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.stream.Collectors;

@NullMarked
public class PaperUnownedWorldGroup extends PaperWorldGroup implements UnownedWorldGroup {
    public PaperUnownedWorldGroup(PaperGroupProvider provider) {
        super(provider, "unowned", new PaperGroupData(), new PaperGroupSettings(), Set.of());
    }

    @Override
    public boolean delete() {
        return getFile().delete() | delete(getDataFolder());
    }

    @Override
    public @Unmodifiable Set<World> getWorlds() {
        return groupProvider.getServer().getWorlds().stream()
                .filter(this::containsWorld)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public @Unmodifiable Set<Key> getPersistedWorlds() {
        return getWorlds().stream().map(World::key).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean containsWorld(World world) {
        return !groupProvider.hasGroup(world);
    }
}
