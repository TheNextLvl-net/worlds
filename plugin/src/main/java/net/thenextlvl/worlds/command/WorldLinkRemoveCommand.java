package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.RelativeArgument;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import net.thenextlvl.worlds.api.link.Relative;
import org.bukkit.World;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class WorldLinkRemoveCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("remove")
                .requires(source -> source.getSender().hasPermission("worlds.command.link.remove"))
                .then(Commands.argument("world", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin, world ->
                                world.getEnvironment().equals(World.Environment.NORMAL)))
                        .then(Commands.argument("relative", new RelativeArgument(relative ->
                                        !relative.equals(Relative.OVERWORLD)))
                                .executes(context -> {
                                    var world = context.getArgument("world", World.class);
                                    var relative = context.getArgument("relative", Relative.class);
                                    var unlink = plugin.linkController().unlink(world, relative);
                                    var message = unlink ? "world.unlink.success" : "world.unlink.failed";
                                    plugin.bundle().sendMessage(context.getSource().getSender(), message,
                                            Placeholder.parsed("relative", relative.key().asString()),
                                            Placeholder.parsed("world", world.key().asString()));
                                    return unlink ? Command.SINGLE_SUCCESS : 0;
                                })));
    }
}
