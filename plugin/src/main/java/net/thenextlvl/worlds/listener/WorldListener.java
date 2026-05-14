package net.thenextlvl.worlds.listener;

import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.Level;
import net.thenextlvl.worlds.WorldOperationException;
import net.thenextlvl.worlds.WorldRegistry;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.CommandFailureHandler;
import net.thenextlvl.worlds.generator.Generator;
import net.thenextlvl.worlds.generator.GeneratorException;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class WorldListener implements Listener {
    private final WorldsPlugin plugin;

    public WorldListener(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(final WorldLoadEvent event) {
        registerEntryPermission(event.getWorld());

        if (plugin.levelView().isNether(event.getWorld())) {
            plugin.getWorldRegistry().registerIfAbsent(event.getWorld().key(), Dimension.THE_NETHER, true, null);
        } else if (plugin.levelView().isEnd(event.getWorld())) {
            plugin.getWorldRegistry().registerIfAbsent(event.getWorld().key(), Dimension.THE_END, true, null);
        }

        if (!plugin.levelView().isOverworld(event.getWorld())) return;
        final var migrating = migrateLegacyWorlds();
        plugin.getWorldRegistry().entrySet()
                .filter(entry -> entry.getValue().enabled())
                .filter(entry -> !migrating.contains(entry.getKey()))
                .forEach(entry -> loadLevel(entry.getKey(), entry.getValue()));
    }

    private void registerEntryPermission(final World world) {
        final var manager = plugin.getServer().getPluginManager();
        final var permission = plugin.getEntryPermission(world);
        if (manager.getPermission(permission) != null) return;
        manager.addPermission(new Permission(
                permission,
                "Allows entering the world " + world.key().asString(),
                PermissionDefault.TRUE
        ));
    }

    private void loadLevel(final Key key, final WorldRegistry.Entry entry) {
        final var level = plugin.levelView().read(key, entry).build();

        if (plugin.getServer().getWorld(level.key()) != null) return;
        if (plugin.getServer().getWorld(level.getName()) != null) return;

        level.create().thenAccept(world -> plugin.getComponentLogger().debug(
                "Loaded dimension {} ({}) from {}",
                world.key().asString(), level.getGeneratorType().key().asString(),
                world.getWorldPath()
        )).exceptionally(throwable -> handleCreationException(throwable, key));
    }

    private Set<Key> migrateLegacyWorlds() {
        final var migrating = new HashSet<Key>();
        final var root = plugin.getServer().getWorldContainer().toPath();
        if (!Files.isDirectory(root)) return migrating;
        try (final var files = Files.list(root)) {
            files.filter(Files::isDirectory)
                    .filter(path -> !path.equals(plugin.getServer().getLevelDirectory()))
                    .map(this::migrateLegacyWorld)
                    .filter(Objects::nonNull)
                    .forEach(migrating::add);
        } catch (final IOException e) {
            plugin.getComponentLogger().warn("Failed to scan legacy worlds in {}", root, e);
        }
        return migrating;
    }

    private @Nullable Key migrateLegacyWorld(final Path path) {
        final var data = plugin.legacyWorldRegistry().read(path).orElse(null);
        if (data == null) return null;

        // todo: does this make sense? it works I guess; no time to double check. future me problem now :)
        final var existing = plugin.getServer().getWorld(data.key());
        if (existing != null) {
            if (!plugin.getWorldRegistry().isRegistered(data.key()))
                plugin.getComponentLogger().warn("Refusing to migrate legacy world {}, a world with the same key ({}) already exists", path, data.key());
            return null;
        }
        // todo end

        try {
            final var generator = data.generator() != null ? Generator.fromString(data.generator()) : null;
            if (!plugin.getWorldRegistry().registerIfAbsent(
                    data.key(), data.dimension(), data.enabled(), generator
            )) return null;
            if (!data.enabled()) return null;

            plugin.handler().warnAndDelayStartupMigration();

            final var level = Level.builder(data.key())
                    .dimension(data.dimension())
                    .generator(generator)
                    .legacyName(path.getFileName().toString())
                    .build();

            level.create().thenAccept(world -> {
                plugin.getComponentLogger().debug(
                        "Migrated legacy world {} ({}) from {}",
                        world.key().asString(), level.getGeneratorType().key().asString(), path
                );
            }).exceptionally(throwable -> handleCreationException(throwable, level.key()));
        } catch (final GeneratorException e) {
            plugin.getComponentLogger().warn("Failed to migrate legacy world {}", data.key());
            plugin.getComponentLogger().warn("{}: {}", e.getClass().getName(), e.getMessage());
        } catch (final Exception e) {
            plugin.getComponentLogger().error("An unexpected error occurred while migrating the legacy world {}", data.key(), e);
            plugin.getComponentLogger().error("Please report the error above on GitHub: {}", WorldsPlugin.ISSUES);
            WorldsPlugin.ERROR_TRACKER.trackError(e);
        }
        return data.key();
    }

    // fixme: this is a pain
    private <T> @Nullable T handleCreationException(final Throwable throwable, final Key key) {
        final var t = throwable.getCause() != null ? throwable.getCause() : throwable;
        if (plugin.handler().isDirectoryLockException(t)) {
            plugin.getComponentLogger().error("Failed to start the minecraft server", t);
            plugin.getServer().shutdown();
        } else if (t instanceof WorldOperationException || t instanceof GeneratorException) {
            CommandFailureHandler.handle(plugin, plugin.getServer().getConsoleSender(), t);
        } else {
            plugin.getComponentLogger().error("An unexpected error occurred while loading the level {}", key, t);
            plugin.getComponentLogger().error("Please report the error above on GitHub: {}", WorldsPlugin.ISSUES);
            WorldsPlugin.ERROR_TRACKER.trackError(t);
        }
        return null;
    }
}
