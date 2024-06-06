package net.thenextlvl.worlds.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.InvalidSyntaxException;

import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldExportCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSourceStack> builder;

    Command.Builder<CommandSourceStack> create() {
        return builder.literal("export", "save")
                .permission("worlds.command.world.export")
                .optional("world", WorldParser.worldParser())
                .handler(this::execute);
    }

    private void execute(CommandContext<CommandSourceStack> context) {
        var sender = context.sender().getSender();
        var world = context.<World>optional("world").orElse(sender instanceof Player self ? self.getWorld() : null);
        if (world == null) throw new InvalidSyntaxException("world export [world]", context.sender(), List.of());
        var placeholder = Placeholder.parsed("world", world.getName());
        try {
            world.save();
            plugin.bundle().sendMessage(sender, "world.save.success", placeholder);
        } catch (Exception e) {
            plugin.bundle().sendMessage(sender, "world.save.failed", placeholder);
            plugin.getComponentLogger().error("Failed to save world {}", world.getName(), e);
        }
    }
}
