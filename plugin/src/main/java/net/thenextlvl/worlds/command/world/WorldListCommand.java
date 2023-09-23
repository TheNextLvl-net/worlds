package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

class WorldListCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("list")
                .permission("worlds.command.world.list")
                .handler(WorldListCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
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
