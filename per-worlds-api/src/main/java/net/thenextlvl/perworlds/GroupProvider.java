package net.thenextlvl.perworlds;

import net.kyori.adventure.key.Key;
import org.bukkit.World;

import java.util.List;
import java.util.Optional;

public interface GroupProvider {
    List<WorldGroup> getGroups();

    Optional<WorldGroup> getGroup(Key key);

    Optional<WorldGroup> getGroup(World world);
}
