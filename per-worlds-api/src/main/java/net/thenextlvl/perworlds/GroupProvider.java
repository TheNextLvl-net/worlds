package net.thenextlvl.perworlds;

import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@NullMarked
public interface GroupProvider {
    File getDataFolder();

    @Unmodifiable
    List<WorldGroup> getGroups();

    Optional<WorldGroup> getGroup(String name);

    Optional<WorldGroup> getGroup(World world);

    WorldGroup createGroup(String name, Consumer<GroupSettings> settings, Collection<World> worlds);

    WorldGroup createGroup(String name, Consumer<GroupSettings> settings, World... worlds);

    boolean hasGroup(String name);

    boolean hasGroup(World world);

    boolean hasGroup(WorldGroup group);

    boolean removeGroup(String name);

    boolean removeGroup(WorldGroup group);
}
