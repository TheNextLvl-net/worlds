package net.thenextlvl.worlds.link;

import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.api.link.LinkTree;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@NullMarked
public class WorldLinkTree implements LinkTree {
    public static final NamespacedKey LINK_NETHER = new NamespacedKey("worlds", "link_nether");
    public static final NamespacedKey LINK_END = new NamespacedKey("worlds", "link_end");

    private final WorldLinkProvider provider;
    private final World overworld;
    private @Nullable Key nether = null;
    private @Nullable Key end = null;

    public WorldLinkTree(WorldLinkProvider provider, World overworld) {
        this.provider = provider;
        this.overworld = overworld;
    }

    @Override
    public World getOverworld() {
        return overworld;
    }

    @Override
    public Optional<World> getNether() {
        return getPersistedNether().map(provider.getServer()::getWorld);
    }

    @Override
    public Optional<Key> getPersistedNether() {
        return Optional.ofNullable(nether);
    }

    @Override
    public boolean setNether(@Nullable World world) {
        if (world != null && !world.getEnvironment().equals(Environment.NETHER)) return false;
        return setNether(world != null ? world.key() : null);
    }

    boolean setNether(@Nullable Key key) {
        if (key != null && getLinkProvider().hasLinkTree(key)) return false;
        this.nether = key;
        return true;
    }

    @Override
    public Optional<World> getEnd() {
        return getPersistedEnd().map(provider.getServer()::getWorld);
    }

    @Override
    public Optional<Key> getPersistedEnd() {
        return Optional.ofNullable(end);
    }

    @Override
    public boolean setEnd(@Nullable World world) {
        if (world != null && !world.getEnvironment().equals(Environment.THE_END)) return false;
        return setEnd(world != null ? world.key() : null);
    }

    @Override
    public boolean isEmpty() {
        return nether == null && end == null;
    }

    boolean setEnd(@Nullable Key key) {
        if (key != null && getLinkProvider().hasLinkTree(key)) return false;
        this.end = key;
        return true;
    }

    @Override
    public boolean contains(Key key) {
        return key.equals(overworld.key()) || key.equals(nether) || key.equals(end);
    }

    @Override
    public boolean contains(World world) {
        return contains(world.key());
    }

    @Override
    public boolean remove(Key key) {
        if (key.equals(end)) {
            this.end = null;
            return true;
        } else if (key.equals(nether)) {
            this.nether = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(World world) {
        return remove(world.key());
    }

    @Override
    public Optional<World> getWorld(Environment environment) {
        return switch (environment) {
            case NORMAL -> Optional.of(getOverworld());
            case NETHER -> getNether();
            case THE_END -> getEnd();
            default -> Optional.empty();
        };
    }

    @Override
    public WorldLinkProvider getLinkProvider() {
        return provider;
    }

    @Override
    public String toString() {
        if (nether == null && end == null) return overworld.key().asString();
        if (nether == null) return overworld.key() + " -> " + end;
        if (end == null) return overworld.key() + " -> " + nether;
        return overworld.key() + " -> " + nether + " & " + end;
    }
}
