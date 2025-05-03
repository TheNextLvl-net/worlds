package net.thenextlvl.worlds.api.link;

import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@NullMarked
public interface LinkTree {
    Optional<World> getOverworld();

    Key getPersistedOverworld();

    Optional<World> getNether();

    Optional<Key> getPersistedNether();

    boolean setNether(@Nullable World world);

    Optional<World> getEnd();

    Optional<Key> getPersistedEnd();

    boolean setEnd(@Nullable World world);

    boolean isEmpty();

    boolean contains(Key key);

    boolean contains(World world);

    boolean remove(Key key);

    boolean remove(World world);

    Optional<World> getWorld(Environment environment);

    LinkProvider getLinkProvider();
}
