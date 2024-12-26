package net.thenextlvl.worlds.controller;

import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.link.LinkController;
import net.thenextlvl.worlds.api.link.Relative;
import org.bukkit.NamespacedKey;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

import static org.bukkit.persistence.PersistentDataType.STRING;

@NullMarked
public class WorldLinkController implements LinkController {
    private final WorldsPlugin plugin;

    public WorldLinkController(WorldsPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public Optional<NamespacedKey> getTarget(World world, Relative relative) {
        return Optional.ofNullable(world.getPersistentDataContainer()
                .get(relative.key(), STRING)
        ).map(NamespacedKey::fromString);
    }

    @Override
    public Optional<NamespacedKey> getTarget(World world, PortalType type) {
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
    public Optional<NamespacedKey> getTarget(World world, World.Environment type) {
        return switch (type) {
            case NETHER -> getTarget(world, Relative.NETHER);
            case THE_END -> getTarget(world, Relative.THE_END);
            case NORMAL -> getTarget(world, Relative.OVERWORLD);
            default -> Optional.empty();
        };
    }

    @Override
    public boolean canLink(World source, World destination) {
        return source.getEnvironment().equals(World.Environment.NORMAL)
               && !destination.getEnvironment().equals(World.Environment.NORMAL)
               && getTarget(source, destination.getEnvironment()).isEmpty();
    }

    @Override
    public boolean link(World source, World destination) {
        if (!canLink(source, destination)) return false;
        var child = switch (destination.getEnvironment()) {
            case NETHER -> Relative.NETHER;
            case THE_END -> Relative.THE_END;
            default -> null;
        };
        if (child == null) return false;
        var opposite = child.equals(Relative.NETHER) ? Relative.THE_END : Relative.NETHER;
        getTarget(source, opposite).map(plugin.getServer()::getWorld).ifPresent(sibling -> {
            sibling.getPersistentDataContainer().set(child.key(), STRING, destination.key().asString());
            destination.getPersistentDataContainer().set(opposite.key(), STRING, sibling.key().asString());
        });
        destination.getPersistentDataContainer().set(Relative.OVERWORLD.key(), STRING, source.key().asString());
        source.getPersistentDataContainer().set(child.key(), STRING, destination.key().asString());
        return true;
    }

    @Override
    public boolean unlink(World source, Relative relative) {
        var world = getTarget(source, relative).map(plugin.getServer()::getWorld);
        var parent = Relative.valueOf(source.getEnvironment()).map(Relative::key);
        parent.ifPresent(key -> world.ifPresent(destination -> {
            destination.getPersistentDataContainer().remove(key);
            source.getPersistentDataContainer().remove(relative.key());
        }));
        return world.isPresent();
    }
}
