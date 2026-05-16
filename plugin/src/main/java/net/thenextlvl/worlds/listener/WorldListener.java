package net.thenextlvl.worlds.listener;

import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.LegacyWorldRegistry;
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
        migrateLegacyWorlds();
        plugin.getWorldRegistry().entrySet()
                .filter(entry -> entry.getValue().enabled())
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

    private void migrateLegacyWorlds() {
        final var root = plugin.getServer().getWorldContainer().toPath();
        if (!Files.isDirectory(root)) return;
        try (final var entries = plugin.legacyWorldRegistry().listEntries(root)) {
            entries.forEach(entry -> migrateLegacyWorld(entry.getKey(), entry.getValue()));
        } catch (final IOException ignored) {
        }
    }

    private void migrateLegacyWorld(final Path path, final LegacyWorldRegistry.LegacyWorldData data) {
        try {
            if (!data.enabled()) {
                plugin.getComponentLogger().warn("Skip migrating disabled legacy world {}", path);
                return;
            }
            
            final var generator = data.generator() != null ? Generator.fromString(data.generator()) : null;
            final var level = Level.builder(data.key())
                    .dimension(data.dimension())
                    .generator(generator)
                    .legacyName(path.getFileName().toString())
                    .build();
            
            if (!plugin.getWorldRegistry().registerIfAbsent(level.key(), level.getDimension(), true, generator)) {
                plugin.getComponentLogger().warn("Refusing to migrate legacy world {}, a world with the same key ({}) is already registered", path, level.key());
                return;
            }

            plugin.handler().warnAndDelayStartupMigration();

            level.create().thenAccept(world -> plugin.getComponentLogger().info(
                    "Migrated legacy world {} ({}) from {}",
                    world.key().asString(), level.getGeneratorType(), path
            )).exceptionally(throwable -> handleCreationException(throwable, level.key()));
        } catch (final GeneratorException e) {
            plugin.getComponentLogger().warn("Failed to migrate legacy world {}", data.key());
            plugin.getComponentLogger().warn("{}: {}", e.getClass().getName(), e.getMessage());
        } catch (final Exception e) {
            plugin.getComponentLogger().error("An unexpected error occurred while migrating the legacy world {}", data.key(), e);
            plugin.getComponentLogger().error("Please report the error above on GitHub: {}", WorldsPlugin.ISSUES);
            WorldsPlugin.ERROR_TRACKER.trackError(e);
        }
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
