package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldCloneCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("clone")
                .requires(source -> source.getSender().hasPermission("worlds.command.clone"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .then(Commands.argument("key", ArgumentTypes.namespacedKey())
                                .then(Commands.literal("template")
                                        .executes(context -> clone(context, false)))
                                .executes(context -> clone(context, true))));
    }

    private int clone(CommandContext<CommandSourceStack> context, boolean full) {
        var world = context.getArgument("world", World.class);
        var name = context.getArgument("key", NamespacedKey.class);
        if (plugin.getServer().getWorld(name) != null) return 0;
        var creator = new WorldCreator(name).copy(world);
        if (full) copy(world, new File(plugin.getServer().getWorldContainer(), creator.name()));
        var copy = creator.createWorld();
        if (copy != null && context.getSource().getSender() instanceof Player player)
            player.teleportAsync(copy.getSpawnLocation());
        return copy != null ? Command.SINGLE_SUCCESS : 0;
    }

    private void copy(World world, File destination) {
        var files = world.getWorldFolder().listFiles(this::shouldCopy);
        if (files == null) return;
        for (File file : files) copy(file, new File(destination, file.getName()));
    }

    private boolean shouldCopy(File file, String name) {
        if (name.equals("advancements") && file.isDirectory()) return false;
        if (name.equals("datapacks") && file.isDirectory()) return false;
        if (name.equals("playerdata") && file.isDirectory()) return false;
        if (name.equals("session.lock")) return false;
        if (name.equals("stats") && file.isDirectory()) return false;
        return !name.equals("uid.dat");
    }

    private void copy(File source, File destination) {
        if (source.isDirectory()) copyDirectory(source, destination);
        else copyFile(source, destination);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void copyDirectory(File source, File destination) {
        if (!destination.exists()) destination.mkdirs();
        var list = source.listFiles();
        if (list == null) return;
        for (var file : list) copy(file, new File(destination, file.getName()));
    }

    private void copyFile(File source, File destination) {
        try (var in = new FileInputStream(source);
             var out = new FileOutputStream(destination)) {
            int length;
            var buf = new byte[1024];
            while ((length = in.read(buf)) > 0) out.write(buf, 0, length);
        } catch (IOException ignored) {
        }
    }
}
