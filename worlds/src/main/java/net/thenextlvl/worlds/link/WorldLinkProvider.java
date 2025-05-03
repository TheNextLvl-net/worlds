package net.thenextlvl.worlds.link;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.link.LinkProvider;
import net.thenextlvl.worlds.api.link.LinkTree;
import net.thenextlvl.worlds.api.link.Relative;
import org.bukkit.PortalType;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.bukkit.persistence.PersistentDataType.STRING;

@NullMarked
public class WorldLinkProvider implements LinkProvider {
    private final Set<LinkTree> trees = new HashSet<>();
    private final WorldsPlugin plugin;

    public WorldLinkProvider(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    public void unloadTree(World world) {
        var overworld = world.getEnvironment().equals(Environment.NORMAL);
        Preconditions.checkArgument(overworld, "Expected NORMAL but was %s for %s", world.getEnvironment(), world.getName());

        trees.removeIf(tree -> tree.getPersistedOverworld().equals(world.key()));
    }

    public void loadTree(World world) {
        var overworld = world.getEnvironment().equals(Environment.NORMAL);
        Preconditions.checkArgument(overworld, "Expected NORMAL but was %s for %s", world.getEnvironment(), world.getName());

        var noneMatch = trees.stream().noneMatch(tree -> tree.getPersistedOverworld().equals(world.key()));
        Preconditions.checkState(noneMatch, "World tree is already loaded for %s", world.getName());

        var tree = new WorldLinkTree(this, world.key());
        var data = world.getPersistentDataContainer();
        Optional.ofNullable(data.get(Relative.NETHER.key(), STRING)).map(Key::key).ifPresent(tree::setNether);
        Optional.ofNullable(data.get(Relative.THE_END.key(), STRING)).map(Key::key).ifPresent(tree::setEnd);
        trees.add(tree);
    }

    @Override
    public @Unmodifiable Set<LinkTree> getLinkTrees() {
        return Set.copyOf(trees);
    }

    @Override
    public Optional<LinkTree> getLinkTree(Key key) {
        return trees.stream().filter(tree -> tree.contains(key)).findAny();
    }

    @Override
    public Optional<LinkTree> getLinkTree(World world) {
        return getLinkTree(world.key());
    }

    @Override
    public Optional<World> getTarget(World world, PortalType type) {
        return switch (type) {
            case NETHER -> switch (world.getEnvironment()) {
                case NORMAL, THE_END -> getTarget(world, Relative.NETHER);
                case NETHER -> getTarget(world, Relative.OVERWORLD);
                default -> Optional.empty();
            };
            case ENDER -> switch (world.getEnvironment()) {
                case NORMAL, NETHER -> getTarget(world, Relative.THE_END);
                case THE_END -> getTarget(world, Relative.OVERWORLD);
                default -> Optional.empty();
            };
            default -> Optional.empty();
        };
    }

    @Override
    public Optional<World> getTarget(World world, Relative relative) {
        return getLinkTree(world).flatMap(tree -> switch (relative) {
            case OVERWORLD -> tree.getOverworld();
            case NETHER -> tree.getNether();
            case THE_END -> tree.getEnd();
        });
    }

    @Override
    public boolean link(World source, World target) {
        if (!source.getEnvironment().equals(Environment.NORMAL)) return false;
        return getLinkTree(source).map(linkTree -> switch (target.getEnvironment()) {
            case NETHER -> linkTree.setNether(target);
            case THE_END -> linkTree.setEnd(target);
            default -> false;
        }).orElse(false);
    }

    @Override
    public boolean unlink(Key source, Key target) {
        return plugin.linkProvider().getLinkTree(source)
                .filter(tree -> !tree.isEmpty())
                .map(tree -> tree.remove(target))
                .orElse(false);
    }

    @Override
    public boolean unlink(World source, World target) {
        return unlink(source.key(), target.key());
    }

    @Override
    public boolean hasLinkTree(Key key) {
        return trees.stream().anyMatch(tree -> tree.contains(key));
    }

    @Override
    public boolean hasLinkTree(World world) {
        return hasLinkTree(world.key());
    }

    public Server getServer() {
        return plugin.getServer();
    }
}
