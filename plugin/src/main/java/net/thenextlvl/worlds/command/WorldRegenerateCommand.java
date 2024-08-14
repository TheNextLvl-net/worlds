package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandFlagsArgument;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;

import java.io.File;
import java.util.Set;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldRegenerateCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("regenerate")
                .requires(source -> source.getSender().hasPermission("worlds.command.regenerate"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin))
                        .then(Commands.argument("flags", new CommandFlagsArgument(
                                Set.of("--confirm", "--schedule")
                        )).executes(this::regenerate))
                        .executes(this::confirmationNeeded));
    }

    private int confirmationNeeded(CommandContext<CommandSourceStack> context) {
        var sender = context.getSource().getSender();
        plugin.bundle().sendMessage(sender, "command.confirmation",
                Placeholder.parsed("action", "/" + context.getInput()),
                Placeholder.parsed("confirmation", "/" + context.getInput() + " --confirm"));
        return Command.SINGLE_SUCCESS;
    }

    private int regenerate(CommandContext<CommandSourceStack> context) {
        var flags = context.getArgument("flags", CommandFlagsArgument.Flags.class);
        if (!flags.contains("--confirm")) return confirmationNeeded(context);
        var world = context.getArgument("world", World.class);
        var result = regenerate(world, flags.contains("--schedule"));
        plugin.bundle().sendMessage(context.getSource().getSender(), result,
                Placeholder.parsed("world", world.key().asString()));
        return Command.SINGLE_SUCCESS;
    }

    private String regenerate(World world, boolean schedule) {
        return schedule ? scheduleRegeneration(world) : regenerateNow(world);
    }

    private String regenerateNow(World world) {
        if (world.getKey().toString().equals("minecraft:overworld"))
            return "world.regenerate.disallowed";

        var environment = world.getEnvironment();
        var worldFolder = world.getWorldFolder();
        var players = world.getPlayers();

        var fallback = plugin.getServer().getWorlds().getFirst().getSpawnLocation();
        players.forEach(player -> player.teleport(fallback, COMMAND));

        if (!plugin.getServer().unloadWorld(world, false))
            return "world.unload.failed";

        regenerate(worldFolder);

        var regenerated = plugin.levelView().loadLevel(worldFolder, environment);
        if (regenerated != null) players.forEach(player ->
                player.teleportAsync(regenerated.getSpawnLocation(), COMMAND));
        return regenerated != null ? "world.regenerate.success" : "world.regenerate.failed";
    }

    private String scheduleRegeneration(World world) {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                regenerate(world.getWorldFolder())));
        return "world.regenerate.scheduled";
    }

    private void regenerate(File level) {
        delete(new File(level, "DIM-1"));
        delete(new File(level, "DIM1"));
        delete(new File(level, "advancements"));
        delete(new File(level, "data"));
        delete(new File(level, "entities"));
        delete(new File(level, "playerdata"));
        delete(new File(level, "poi"));
        delete(new File(level, "region"));
        delete(new File(level, "stats"));
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean delete(File file) {
        if (file.isFile()) return file.delete();
        var files = file.listFiles();
        if (files == null) return false;
        for (var file1 : files) delete(file1);
        return file.delete();
    }
}
