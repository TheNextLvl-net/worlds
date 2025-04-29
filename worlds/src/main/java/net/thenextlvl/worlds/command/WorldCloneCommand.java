package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static net.thenextlvl.worlds.command.WorldCommand.keyArgument;
import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
class WorldCloneCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("clone")
                .requires(source -> source.getSender().hasPermission("worlds.command.clone"))
                .then(clone(plugin));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> clone(WorldsPlugin plugin) {
        return worldArgument(plugin).then(keyArgument().then(Commands.literal("template")
                        .executes(context -> clone(context, false, plugin)))
                .executes(context -> clone(context, true, plugin)));
    }

    private static int clone(CommandContext<CommandSourceStack> context, boolean full, WorldsPlugin plugin) {
        var world = context.getArgument("world", World.class);
        var key = context.getArgument("key", NamespacedKey.class);
        var clone = clone(world, key, full, plugin);

        if (clone != null) plugin.persistWorld(clone, true);

        var placeholder = Placeholder.parsed("world", world.getName());
        var message = clone != null ? "world.clone.success" : "world.clone.failed";

        if (clone != null && context.getSource().getSender() instanceof Player player)
            player.teleportAsync(clone.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);

        plugin.bundle().sendMessage(context.getSource().getSender(), message, placeholder);
        return clone != null ? Command.SINGLE_SUCCESS : 0;
    }

    private static @Nullable World clone(World world, NamespacedKey key, boolean full, WorldsPlugin plugin) {
        if (plugin.getServer().getWorld(key) != null) return null;
        if (plugin.getServer().getWorld(key.getKey()) != null) return null;
        var target = plugin.getServer().getWorldContainer().toPath().resolve(key.getKey());
        if (Files.exists(target) || (full && !copy(plugin, world, target))) return null;
        return new WorldCreator(key.getKey(), key).copy(world).createWorld();
    }

    private static boolean copy(WorldsPlugin plugin, World world, Path target) {
        try {
            copyDirectory(plugin, world.getWorldFolder().toPath(), target);
            return true;
        } catch (Exception e) {
            plugin.getComponentLogger().error("Failed to copy world {} to {}", world.getName(), target, e);
            return false;
        }
    }

    private static void copyDirectory(WorldsPlugin plugin, Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) throws IOException {
                if (switch (path.getFileName().toString()) {
                    case "advancements", "datapacks", "playerdata", "stats" -> true;
                    default -> false;
                }) return FileVisitResult.SKIP_SUBTREE;
                Files.createDirectories(destination.resolve(source.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
                if (switch (path.getFileName().toString()) {
                    case "uid.dat", "session.lock" -> false;
                    default -> true;
                }) Files.copy(path, destination.resolve(source.relativize(path)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException exc) {
                plugin.getComponentLogger().error("Failed to copy file: {}", path, exc);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
