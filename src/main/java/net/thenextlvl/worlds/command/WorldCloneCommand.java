package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
class WorldCloneCommand extends OptionCommand {

    private WorldCloneCommand(WorldsPlugin plugin) {
        super(plugin);
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldCloneCommand(plugin);
        return Commands.literal("clone")
                .requires(source -> source.getSender().hasPermission("worlds.command.clone"))
                .then(command.createCommand());
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> createCommand() {
        var world = worldArgument(plugin);

        addOptions(world, false, Set.of(
                new Option("full", BoolArgumentType.bool()),
                new Option("key", new KeyArgument()),
                new Option("name", StringArgumentType.string())
        ), null);

        return world.executes(this::execute);
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        var world = context.getArgument("world", World.class);

        var full = tryGetArgument(context, "full", Boolean.class).orElse(true);

        var sender = context.getSource().getSender();
        var placeholder = Placeholder.parsed("world", world.getName());

        plugin.bundle().sendMessage(sender, "world.clone", placeholder);
        plugin.levelView().cloneAsync(world, builder -> {
            tryGetArgument(context, "name", String.class).ifPresent(builder::name);
            tryGetArgument(context, "key", Key.class).ifPresent(builder::key);
        }, full).thenAccept(clone -> {
            if (sender instanceof Player player) player.teleportAsync(clone.getSpawnLocation(), COMMAND);
            plugin.bundle().sendMessage(sender, "world.clone.success", placeholder);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to clone world {}", world.getName(), throwable);
            plugin.bundle().sendMessage(sender, "world.clone.failed", placeholder);
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }
}
