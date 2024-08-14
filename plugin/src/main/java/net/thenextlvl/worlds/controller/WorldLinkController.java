package net.thenextlvl.worlds.controller;

import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.link.LinkController;
import net.thenextlvl.worlds.link.Relative;
import org.bukkit.NamespacedKey;
import org.bukkit.PortalType;
import org.bukkit.World;

import java.util.Optional;

import static org.bukkit.persistence.PersistentDataType.STRING;

@RequiredArgsConstructor
public class WorldLinkController implements LinkController {
    private final WorldsPlugin plugin;

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
        return !source.getEnvironment().equals(destination.getEnvironment())
               && getTarget(source, destination.getEnvironment()).isEmpty()
               && getTarget(destination, source.getEnvironment()).isEmpty();
    }

    @Override
    public boolean link(World source, World destination) {
        if (!canLink(source, destination)) return false;
        var child = switch (destination.getEnvironment()) {
            case NETHER -> Relative.NETHER.key();
            case THE_END -> Relative.THE_END.key();
            default -> null;
        };
        var parent = Relative.valueOf(source.getEnvironment()).map(Relative::key);
        if (child == null || parent.isEmpty()) return false;
        destination.getPersistentDataContainer().set(parent.get(), STRING, source.key().asString());
        source.getPersistentDataContainer().set(child, STRING, destination.key().asString());
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
