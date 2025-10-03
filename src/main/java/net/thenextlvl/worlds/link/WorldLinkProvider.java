package net.thenextlvl.worlds.link;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.link.LinkProvider;
import net.thenextlvl.worlds.api.link.LinkTree;
import org.bukkit.NamespacedKey;
import org.bukkit.PortalType;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.bukkit.persistence.PersistentDataType.STRING;

@NullMarked
public final class WorldLinkProvider implements LinkProvider {
    private static final NamespacedKey OLD_LINK_NETHER = new NamespacedKey("relative", "nether");
    private static final NamespacedKey OLD_LINK_END = new NamespacedKey("relative", "the_end");

    private final Set<LinkTree> trees = new HashSet<>();
    private final WorldsPlugin plugin;

    public WorldLinkProvider(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    public void unloadTree(World world) {
        if (!world.getEnvironment().equals(World.Environment.NORMAL)) return;
        trees.removeIf(tree -> tree.getOverworld().equals(world));
    }

    public void loadTree(World world) {
        if (!world.getEnvironment().equals(World.Environment.NORMAL)) return;

        var noneMatch = trees.stream().noneMatch(tree -> tree.getOverworld().equals(world));
        Preconditions.checkState(noneMatch, "World tree is already loaded for %s", world.getName());

        var tree = new WorldLinkTree(this, world);
        var data = world.getPersistentDataContainer();

        Optional.ofNullable(data.get(OLD_LINK_NETHER, STRING)).map(Key::key).ifPresent(key -> {
            data.remove(OLD_LINK_NETHER);
            tree.setNether(key);
        });
        Optional.ofNullable(data.get(OLD_LINK_END, STRING)).map(Key::key).ifPresent(key -> {
            data.remove(OLD_LINK_END);
            tree.setEnd(key);
        });

        Optional.ofNullable(data.get(WorldLinkTree.LINK_NETHER, STRING)).map(Key::key).ifPresent(tree::setNether);
        Optional.ofNullable(data.get(WorldLinkTree.LINK_END, STRING)).map(Key::key).ifPresent(tree::setEnd);
        trees.add(tree);
    }

    public void persistTrees() {
        getLinkTrees().forEach(tree -> persistTree(tree.getOverworld()));
    }

    public void persistTree(World world) {
        if (!world.getEnvironment().equals(World.Environment.NORMAL)) return;
        getLinkTree(world).filter(linkTree -> !linkTree.isEmpty()).ifPresent(tree -> {
            var container = world.getPersistentDataContainer();
            tree.getPersistedNether().map(Key::asString).ifPresentOrElse(
                    nether -> container.set(WorldLinkTree.LINK_NETHER, PersistentDataType.STRING, nether),
                    () -> container.remove(WorldLinkTree.LINK_NETHER));
            tree.getPersistedEnd().map(Key::asString).ifPresentOrElse(
                    nether -> container.set(WorldLinkTree.LINK_END, PersistentDataType.STRING, nether),
                    () -> container.remove(WorldLinkTree.LINK_END));
        });
    }

    @Override
    public Stream<LinkTree> getLinkTrees() {
        return trees.stream();
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
                case NORMAL, THE_END -> getLinkTree(world).flatMap(LinkTree::getNether);
                case NETHER -> getLinkTree(world).map(LinkTree::getOverworld);
                default -> Optional.empty();
            };
            case ENDER -> switch (world.getEnvironment()) {
                case NORMAL, NETHER -> getLinkTree(world).flatMap(LinkTree::getEnd);
                case THE_END -> getLinkTree(world).map(LinkTree::getOverworld);
                default -> Optional.empty();
            };
            default -> Optional.empty();
        };
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
