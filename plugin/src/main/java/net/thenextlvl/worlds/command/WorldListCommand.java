package net.thenextlvl.worlds.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.generator.WorldInfo;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldListCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSourceStack> builder;

    Command.Builder<CommandSourceStack> create() {
        return builder.literal("list")
                .permission("worlds.command.world.list")
                .handler(this::execute);
    }

    private void execute(CommandContext<CommandSourceStack> context) {
        var sender = context.sender().getSender();
        var worlds = Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();
        var joined = Component.join(JoinConfiguration.commas(true), worlds.stream()
                .map(world -> Component.text(world)
                        .hoverEvent(HoverEvent.showText(plugin.bundle().component(sender,
                                "world.list.hover", Placeholder.parsed("world", world))))
                        .clickEvent(ClickEvent.runCommand("/world teleport " + world)))
                .toList());
        plugin.bundle().sendMessage(sender, "world.list",
                Placeholder.parsed("amount", String.valueOf(worlds.size())),
                Placeholder.component("worlds", joined));
    }
}
