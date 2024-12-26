package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldSuggestionProvider;
import org.bukkit.World;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class WorldLinkCreateCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("create")
                .requires(source -> source.getSender().hasPermission("worlds.command.link.create"))
                .then(Commands.argument("source", ArgumentTypes.world())
                        .suggests(new WorldSuggestionProvider<>(plugin, (context, world) ->
                                world.getEnvironment().equals(World.Environment.NORMAL)))
                        .then(Commands.argument("destination", ArgumentTypes.world())
                                .suggests(new WorldSuggestionProvider<>(plugin, (context, world) ->
                                        !world.getEnvironment().equals(World.Environment.NORMAL)))
                                .executes(context -> {
                                    var source = context.getArgument("source", World.class);
                                    var destination = context.getArgument("destination", World.class);
                                    var link = plugin.linkController().link(source, destination);
                                    var message = link ? "world.link.success" : "world.link.failed";
                                    plugin.bundle().sendMessage(context.getSource().getSender(), message,
                                            Placeholder.parsed("source", source.key().asString()),
                                            Placeholder.parsed("destination", destination.key().asString()));
                                    return link ? Command.SINGLE_SUCCESS : 0;
                                })));
    }
}
