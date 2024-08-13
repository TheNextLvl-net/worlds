package net.thenextlvl.worlds.link;

import org.bukkit.NamespacedKey;
import org.bukkit.PortalType;
import org.bukkit.World;

import java.util.Optional;

public interface LinkController {

    Optional<NamespacedKey> getChild(World world, Relative relative);

    Optional<NamespacedKey> getParent(World world);

    Optional<NamespacedKey> getTarget(World world, Relative relative);

    Optional<NamespacedKey> getTarget(World world, PortalType type);

    boolean canLink(World source, World destination);

    boolean link(World source, World destination);
}
