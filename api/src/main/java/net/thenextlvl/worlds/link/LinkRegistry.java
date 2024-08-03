package net.thenextlvl.worlds.link;

import org.bukkit.World;

import java.util.Set;

public interface LinkRegistry {
    Set<Link> getLinks();

    boolean isRegistered(Link link);

    boolean register(Link link);

    boolean unregister(Link link);

    boolean unregisterAll(World world);
}
