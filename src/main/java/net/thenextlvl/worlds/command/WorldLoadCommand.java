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
import net.thenextlvl.worlds.api.model.Level;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.Optional;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
@RequiredArgsConstructor
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

        var build = plugin.levelView().isLevel(level) ? plugin.levelBuilder(level).build(): null;
        var world = Optional.ofNullable(build).filter(Level::importedBefore).flatMap(Level::create).orElse(null);

        var message = world != null ? "world.load.success" : "world.load.failed";
        plugin.bundle().sendMessage(context.getSource().getSender(), message,
                Placeholder.parsed("world", world != null ? world.getName() : name));

        if (world != null && context.getSource().getSender() instanceof Entity entity)
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);

        if (world != null) {
            plugin.persistStatus(world, true, true);
            plugin.levelView().saveLevelData(world, true);
        }

        return world != null ? Command.SINGLE_SUCCESS : 0;
    }
}
