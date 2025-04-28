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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
        return worldArgument(plugin).then(keyArgument(plugin).then(Commands.literal("template")
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
        if (new File(plugin.getServer().getWorldContainer(), key.getKey()).isDirectory()) return null;
        if (full) copy(world, new File(plugin.getServer().getWorldContainer(), key.getKey()));
        return new WorldCreator(key.getKey(), key).copy(world).createWorld();
    }

    private static void copy(World world, File destination) {
        var files = world.getWorldFolder().listFiles(WorldCloneCommand::shouldCopy);
        if (files == null) return;
        for (File file : files) copy(file, new File(destination, file.getName()));
    }

    private static boolean shouldCopy(File file, String name) {
        if (name.equals("advancements") && file.isDirectory()) return false;
        if (name.equals("datapacks") && file.isDirectory()) return false;
        if (name.equals("playerdata") && file.isDirectory()) return false;
        if (name.equals("session.lock")) return false;
        if (name.equals("stats") && file.isDirectory()) return false;
        return !name.equals("uid.dat");
    }

    private static void copy(File source, File destination) {
        if (source.isDirectory()) copyDirectory(source, destination);
        else copyFile(source, destination);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void copyDirectory(File source, File destination) {
        if (!destination.exists()) destination.mkdirs();
        var list = source.listFiles();
        if (list == null) return;
        for (var file : list) copy(file, new File(destination, file.getName()));
    }

    private static void copyFile(File source, File destination) {
        try (var in = new FileInputStream(source);
             var out = new FileOutputStream(destination)) {
            int length;
            var buf = new byte[1024];
            while ((length = in.read(buf)) > 0) out.write(buf, 0, length);
        } catch (IOException ignored) {
        }
    }
}
