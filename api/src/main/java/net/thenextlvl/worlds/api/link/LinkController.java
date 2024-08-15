package net.thenextlvl.worlds.api.link;

import org.bukkit.NamespacedKey;
import org.bukkit.PortalType;
import org.bukkit.World;

import java.util.Optional;

public interface LinkController {
    Optional<NamespacedKey> getTarget(World world, PortalType type);

    Optional<NamespacedKey> getTarget(World world, Relative relative);

    Optional<NamespacedKey> getTarget(World world, World.Environment type);

    boolean canLink(World source, World destination);

    boolean link(World source, World destination);

    boolean unlink(World source, Relative relative);
}
