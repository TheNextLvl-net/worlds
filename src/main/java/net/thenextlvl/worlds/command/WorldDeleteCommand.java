package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandFlagsArgument;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.Set;

@NullMarked
class WorldDeleteCommand {
    private final WorldsPlugin plugin;

    WorldDeleteCommand(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("delete")
                .requires(source -> source.getSender().hasPermission("worlds.command.delete"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .then(Commands.argument("flags", new CommandFlagsArgument(
                                Set.of("--confirm", "--schedule")
                        )).executes(this::delete))
                        .executes(this::confirmationNeeded));
    }

    private int confirmationNeeded(CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        plugin.bundle().sendMessage(sender, "command.confirmation",
                Placeholder.parsed("action", "/" + context.getInput()),
                Placeholder.parsed("confirmation", "/" + context.getInput() + " --confirm"));
        return Command.SINGLE_SUCCESS;
    }

    private int delete(CommandContext<CommandSourceStack> context) {
        var flags = context.getArgument("flags", CommandFlagsArgument.Flags.class);
        if (!flags.contains("--confirm")) return confirmationNeeded(context);
        var world = context.getArgument("world", World.class);
        var result = delete(world, flags.contains("--schedule"));
        plugin.bundle().sendMessage(context.getSource().getSender(), result,
                Placeholder.parsed("world", world.getName()));
        return Command.SINGLE_SUCCESS;
    }

    private String delete(World world, boolean schedule) {

        var dragonBattle = world.getEnderDragonBattle();
        if (dragonBattle != null) dragonBattle.getBossBar().removeAll();

        return schedule ? scheduleDeletion(world) : deleteNow(world);
    }

    private String deleteNow(World world) {
        if (plugin.isRunningFolia())
            return "world.delete.disallowed.folia";
        if (world.getKey().toString().equals("minecraft:overworld"))
            return "world.delete.disallowed";

        var fallback = plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        world.getPlayers().forEach(player -> player.teleportAsync(fallback).join());

        if (!plugin.levelView().unloadLevel(world, false))
            return "world.unload.failed";

        return delete(world.getWorldFolder()) ? "world.delete.success" : "world.delete.failed";
    }

    private String scheduleDeletion(World world) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (delete(world.getWorldFolder())) return;
            plugin.getComponentLogger().error("Failed to delete world {}", world.getName());
        }));
        return "world.delete.scheduled";
    }

    private boolean delete(File file) {
        if (file.isFile()) return file.delete();
        var files = file.listFiles();
        if (files == null) return false;
        for (var file1 : files) delete(file1);
        return file.delete();
    }
}
