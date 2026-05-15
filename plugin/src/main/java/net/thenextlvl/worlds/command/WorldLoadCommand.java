package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import net.thenextlvl.worlds.command.suggestion.WorldLoadSuggestionProvider;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldLoadCommand extends SimpleCommand {
    private WorldLoadCommand(final WorldsPlugin plugin) {
        super(plugin, "load", "worlds.command.load");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldLoadCommand(plugin);
        final var key = Commands.argument("key", new KeyArgument())
                .suggests(new WorldLoadSuggestionProvider(plugin));
        return command.create().then(key.executes(command));
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var key = context.getArgument("key", Key.class);
        final var placeholder = Placeholder.parsed("world", key.asString());

        plugin.bundle().sendMessage(sender, "world.load", placeholder);
        plugin.load(key).thenAccept(world -> {
            plugin.getWorldRegistry().setEnabled(world.key(), true);
            plugin.bundle().sendMessage(sender, "world.load.success", placeholder);
            if (!(sender instanceof final Entity entity)) return;
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            CommandFailureHandler.handle(plugin, sender, throwable, placeholder);
            return null;
        });
        return SINGLE_SUCCESS;
    }
}
