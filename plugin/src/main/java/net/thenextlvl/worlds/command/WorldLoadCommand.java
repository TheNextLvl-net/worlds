package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.entity.Entity;

import java.io.File;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldLoadCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("load")
                .requires(source -> source.getSender().hasPermission("worlds.command.load"))
                .then(Commands.argument("world", StringArgumentType.string())
                        .suggests(new LevelSuggestionProvider<>(plugin))
                        .executes(this::load));
    }

    private int load(CommandContext<CommandSourceStack> context) {
        var name = context.getArgument("world", String.class);
        var level = new File(plugin.getServer().getWorldContainer(), name);
        var world = plugin.levelView().isLevel(level) ? plugin.levelView().loadLevel(level,
                optional -> optional.map(extras -> !extras.enabled()).isPresent()) : null;
        var message = world != null ? "world.load.success" : "world.load.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world != null ? world.key().asString() : name));
        if (world != null && context.getSource().getSender() instanceof Entity entity)
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        return world != null ? Command.SINGLE_SUCCESS : 0;
    }
}
