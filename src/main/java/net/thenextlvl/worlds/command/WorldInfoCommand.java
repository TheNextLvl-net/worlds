package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.DimensionType;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.level.Level;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
class WorldInfoCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("info")
                .requires(source -> source.getSender().hasPermission("worlds.command.info"))
                .then(info(plugin))
                .executes(context -> info(plugin, context));
    }

    private static int info(WorldsPlugin plugin, CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            plugin.bundle().sendMessage(context.getSource().getSender(), "command.sender");
            return 0;
        } else return list(context.getSource().getSender(), player.getWorld(), plugin);
    }

    private static RequiredArgumentBuilder<CommandSourceStack, World> info(WorldsPlugin plugin) {
        return worldArgument(plugin).executes(context -> {
            var world = context.getArgument("world", World.class);
            return list(context.getSource().getSender(), world, plugin);
        });
    }

    private static int list(CommandSender sender, World world, WorldsPlugin plugin) {
        var path = world.getWorldFolder().toPath();
        var root = plugin.levelView().read(path).map(Level.Builder::build);
        plugin.bundle().sendMessage(sender, "world.info.name",
                Placeholder.parsed("world", world.key().asString()),
                Placeholder.parsed("name", world.getName()));
        plugin.bundle().sendMessage(sender, "world.info.players",
                Formatter.number("players", world.getPlayers().size()));
        plugin.bundle().sendMessage(sender, "world.info.type",
                Placeholder.parsed("type", root.map(Level::getGeneratorType)
                        .orElse(GeneratorType.NORMAL).name()));
        plugin.bundle().sendMessage(sender, "world.info.dimension",
                Placeholder.parsed("dimension", root.map(level -> level.getLevelStem().dimensionType())
                        .orElse(DimensionType.OVERWORLD).key().asString()));
        plugin.levelView().getGenerator(world).ifPresent(generator -> plugin.bundle().sendMessage(sender,
                "world.info.generator", Placeholder.parsed("generator", generator.getName())));
        plugin.bundle().sendMessage(sender, "world.info.seed",
                Placeholder.parsed("seed", String.valueOf(world.getSeed())));
        try {
            var bytes = getSize(path);
            var kb = bytes / 1024d;
            var mb = kb / 1024d;
            var gb = mb / 1024d;
            plugin.bundle().sendMessage(sender, "world.info.size",
                    Formatter.number("size", gb >= 1 ? gb : mb >= 1 ? mb : kb >= 1 ? kb : bytes),
                    Formatter.choice("unit", gb >= 1 ? 0 : mb >= 1 ? 1 : kb >= 1 ? 2 : 3));
        } catch (IOException e) {
            plugin.getComponentLogger().warn("Failed to get world size for {}", world.key(), e);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static long getSize(Path path) throws IOException {
        var size = new AtomicLong(0);
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        return size.get();
    }
}
