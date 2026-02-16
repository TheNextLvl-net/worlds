package net.thenextlvl.worlds.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.brigadier.OptionCommand;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldCloneCommand extends OptionCommand {
    private WorldCloneCommand(final WorldsPlugin plugin) {
        super(plugin, "clone", "worlds.command.clone");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldCloneCommand(plugin);
        return command.create().then(command.createCommand());
    }

    @Override
    protected RequiredArgumentBuilder<CommandSourceStack, ?> createCommand() {
        final var world = worldArgument(plugin);

        addOptions(world, false, Set.of(
                new Option("full", BoolArgumentType.bool()),
                new Option("key", new KeyArgument()),
                new Option("name", StringArgumentType.string())
        ), null);

        return world.executes(this);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var world = context.getArgument("world", World.class);

        final var full = tryGetArgument(context, "full", Boolean.class).orElse(true);

        final var sender = context.getSource().getSender();
        final var placeholder = Placeholder.parsed("world", world.getName());

        plugin.bundle().sendMessage(sender, "world.clone", placeholder);
        plugin.levelView().cloneAsync(world, builder -> {
            tryGetArgument(context, "name", String.class).ifPresent(builder::name);
            tryGetArgument(context, "key", Key.class).ifPresent(builder::key);
        }, full).thenAccept(clone -> {
            if (sender instanceof final Player player) player.teleportAsync(clone.getSpawnLocation(), COMMAND);
            plugin.bundle().sendMessage(sender, "world.clone.success", placeholder);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().warn("Failed to clone world {}", world.getName(), throwable);
            plugin.bundle().sendMessage(sender, "world.clone.failed", placeholder);
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
