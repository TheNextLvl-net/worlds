package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.List;
import java.util.Optional;

@NullMarked
public interface GroupProvider {
    File getDataFolder();

    GroupSettings getSettings();

    @Unmodifiable
    List<WorldGroup> getGroups();

    Optional<WorldGroup> getGroup(Key key);

    Optional<WorldGroup> getGroup(World world);

    WorldGroup.Builder createGroup(Key key);

    boolean addGroup(WorldGroup group);

    boolean hasGroup(Key key);

    boolean hasGroup(World world);

    boolean hasGroup(WorldGroup group);

    boolean removeGroup(Key key);

    boolean removeGroup(WorldGroup group);
}
