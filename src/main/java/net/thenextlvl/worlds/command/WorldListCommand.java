package net.thenextlvl.worlds.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class WorldListCommand extends SimpleCommand {
    private WorldListCommand(WorldsPlugin plugin) {
        super(plugin, "list", "worlds.command.list");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(WorldsPlugin plugin) {
        var command = new WorldListCommand(plugin);
        return command.create().executes(command);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var sender = context.getSource().getSender();
        var worlds = plugin.getServer().getWorlds();

        var joined = worlds.stream().map(world -> Component.text(world.getName())
                .hoverEvent(HoverEvent.showText(plugin.bundle().component("world.list.hover", sender,
                        Placeholder.parsed("world", world.key().asString()))))
                .clickEvent(ClickEvent.runCommand("/world teleport " + world.key().asString()))
        ).toList();
        plugin.bundle().sendMessage(sender, "world.list",
                Placeholder.parsed("amount", String.valueOf(worlds.size())),
                Formatter.joining("worlds", joined));
        return SINGLE_SUCCESS;
    }
}
