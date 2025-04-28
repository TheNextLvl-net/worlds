package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
class WorldListCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        return Commands.literal("list")
                .requires(source -> source.getSender().hasPermission("worlds.command.list"))
                .executes(context -> list(context, plugin));
    }

    private static int list(CommandContext<CommandSourceStack> context, WorldsPlugin plugin) {
        var sender = context.getSource().getSender();
        var worlds = plugin.getServer().getWorlds();

        var joined = Component.join(JoinConfiguration.commas(true), worlds.stream()
                .map(world -> Component.text(world.getName())
                        .hoverEvent(HoverEvent.showText(plugin.bundle().component(sender,
                                "world.list.hover", Placeholder.parsed("world", world.key().asString()))))
                        .clickEvent(ClickEvent.runCommand("/world teleport " + world.key().asString())))
                .toList());
        plugin.bundle().sendMessage(sender, "world.list",
                Placeholder.parsed("amount", String.valueOf(worlds.size())),
                Placeholder.component("worlds", joined));

        return Command.SINGLE_SUCCESS;
    }
}
