package net.thenextlvl.worlds.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.World;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.flag.CommandFlag;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldDeleteCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSourceStack> builder;

    Command.Builder<CommandSourceStack> create() {
        return builder.literal("delete")
                .permission("worlds.command.world.delete")
                .required("world", WorldParser.worldParser())
                .flag(CommandFlag.builder("keep-image"))
                .flag(CommandFlag.builder("keep-world"))
                .flag(CommandFlag.builder("schedule"))
                .flag(CommandFlag.builder("confirm"))
                .handler(this::execute);
    }

    @SuppressWarnings("deprecation")
    private void execute(CommandContext<CommandSourceStack> context) {
        if (!context.flags().contains("confirm")) {
            plugin.bundle().sendMessage(context.sender().getSender(), "command.confirmation",
                    Placeholder.parsed("action", "/" + context.rawInput().input()),
                    Placeholder.parsed("confirmation", "/" + context.rawInput().input() + " --confirm"));
            return;
        }
        var world = context.<World>get("world");
        var keepImage = context.flags().contains("keep-image");
        var keepWorld = context.flags().contains("keep-world");
        var schedule = context.flags().contains("schedule");
        var image = plugin.imageProvider().getOrDefault(world);
        var result = image.delete(keepImage, keepWorld, schedule);
        plugin.bundle().sendMessage(context.sender().getSender(), result.getMessage(),
                Placeholder.parsed("world", world.getName()),
                Placeholder.parsed("image", image.getWorldImage().name()));
    }
}
