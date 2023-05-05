package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.volume.Generator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.thenextlvl.worlds.util.Messages;

import java.util.Arrays;

class WorldCreateCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder
                .literal("create")
                .argument(StringArgument.of("name"))
                .flag(CommandFlag.builder("type").withAliases("t")
                        .withArgument(StringArgument.builder("type").withSuggestionsProvider((context, token) ->
                                Arrays.stream(WorldType.values())
                                        .map(type -> type.name().toLowerCase().replace("_", "-"))
                                        .filter(s -> s.startsWith(token))
                                        .toList())))
                .flag(CommandFlag.builder("environment").withAliases("e")
                        .withArgument(StringArgument.builder("environment").withSuggestionsProvider((context, token) ->
                                Arrays.stream(World.Environment.values())
                                        .map(type -> type.name().toLowerCase().replace("_", "-"))
                                        .filter(s -> s.startsWith(token))
                                        .toList())))
                .flag(CommandFlag.builder("generator").withAliases("g")
                        .withArgument(StringArgument.builder("generator").withSuggestionsProvider((context, token) ->
                                Arrays.stream(Bukkit.getPluginManager().getPlugins())
                                        .filter(plugin -> Generator.isProvided(plugin.getClass()))
                                        .map(Plugin::getName)
                                        .filter(s -> s.startsWith(token))
                                        .toList())))
                .flag(CommandFlag.builder("identifier").withAliases("i")
                        .withArgument(StringArgument.builder("identifier")
                                .greedyFlagYielding()))
                .flag(CommandFlag.builder("seed").withAliases("s")
                        .withArgument(LongArgument.builder("seed")))
                .handler(WorldCreateCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var name = context.<String>get("name");
        var sender = context.getSender();
        var placeholder = Placeholder.<Audience>of("world", name);
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        // TODO: 04.05.23 do stuff
    }
}
