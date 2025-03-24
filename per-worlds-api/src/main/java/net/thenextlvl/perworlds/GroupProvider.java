package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Key;
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

    Optional<WorldGroup> getGroup(Key key);

    Optional<WorldGroup> getGroup(World world);

    WorldGroup createGroup(Key key, Consumer<GroupSettings> settings, Collection<World> worlds);

    WorldGroup createGroup(Key key, Consumer<GroupSettings> settings, World... worlds);

    boolean hasGroup(Key key);

    boolean hasGroup(World world);

    boolean hasGroup(WorldGroup group);

    boolean removeGroup(Key key);

    boolean removeGroup(WorldGroup group);
}
