package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.util.Messages;
import net.thenextlvl.worlds.volume.Generator;
import net.thenextlvl.worlds.volume.Volume;
import net.thenextlvl.worlds.volume.WorldImage;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Arrays;

import static org.bukkit.World.Environment.CUSTOM;
import static org.bukkit.World.Environment.NORMAL;

class WorldCreateCommand {

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder
                .literal("create")
                .argument(StringArgument.<CommandSender>builder("name").withSuggestionsProvider((context, token) ->
                        Volume.findWorlds().stream()
                                .map(File::getName)
                                .filter(s -> s.startsWith(token))
                                .filter(s -> Bukkit.getWorld(s) == null)
                                .toList()))
                .flag(CommandFlag.builder("type").withAliases("t")
                        .withArgument(StringArgument.builder("type").withSuggestionsProvider((context, token) ->
                                Arrays.stream(WorldType.values())
                                        .map(type -> type.name().toLowerCase().replace("_", "-"))
                                        .filter(s -> s.startsWith(token))
                                        .toList())))
                .flag(CommandFlag.builder("environment").withAliases("e")
                        .withArgument(StringArgument.builder("environment").withSuggestionsProvider((context, token) ->
                                Arrays.stream(Environment.values())
                                        .filter(environment -> !environment.equals(CUSTOM))
                                        .map(type -> type.name().toLowerCase().replace("_", "-"))
                                        .filter(s -> s.startsWith(token))
                                        .toList())))
                .flag(CommandFlag.builder("generator").withAliases("g")
                        .withArgument(StringArgument.builder("generator").withSuggestionsProvider((context, token) ->
                                Arrays.stream(Bukkit.getPluginManager().getPlugins())
                                        .filter(plugin -> Generator.hasChunkGenerator(plugin.getClass())
                                                || Generator.hasBiomeProvider(plugin.getClass()))
                                        .map(Plugin::getName)
                                        .filter(s -> s.startsWith(token))
                                        .toList())))
                .flag(CommandFlag.builder("identifier").withAliases("i")
                        .withArgument(StringArgument.builder("identifier").quoted()))
                .flag(CommandFlag.builder("seed").withAliases("s")
                        .withArgument(LongArgument.builder("seed")))
                .flag(CommandFlag.builder("structures")
                        .withArgument(BooleanArgument.builder("structures")))
                .flag(CommandFlag.builder("hardcore")
                        .withArgument(BooleanArgument.builder("hardcore")))
                .handler(WorldCreateCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        try {
            handleCreate(context);
        } catch (Exception e) {
            var sender = context.getSender();
            var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
            sender.sendRichMessage(Messages.INVALID_ARGUMENT.message(locale, sender));
        }
    }

    private static void handleCreate(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var name = context.<String>get("name");
        var placeholder = Placeholder.<Audience>of("world", name);
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;

        if (Bukkit.getWorld(name) != null) {
            sender.sendRichMessage(Messages.WORLD_EXISTS.message(locale, sender, placeholder));
            return;
        }

        var environment = context.flags().<String>getValue("environment").map(s ->
                Environment.valueOf(s.toUpperCase().replace("-", "_"))).orElse(NORMAL);
        var type = context.flags().<String>getValue("type").map(s ->
                WorldType.valueOf(s.toUpperCase().replace("-", "_"))).orElse(WorldType.NORMAL);
        var identifier = context.flags().<String>get("identifier");
        var plugin = context.flags().<String>get("generator");
        var generator = plugin != null ? new Generator(plugin, identifier) : null;
        var structures = context.flags().<Boolean>getValue("structures").orElse(true);
        var hardcore = context.flags().<Boolean>getValue("hardcore").orElse(false);
        var seed = context.flags().<Long>getValue("seed").orElse(0L);

        var world = new WorldImage(name, generator, environment, type, structures, hardcore, seed).build();
        Volume volume = null;
        if (world != null) volume = new Volume(world, generator).register().save();
        var message = world != null ? Messages.WORLD_CREATE_SUCCEEDED : Messages.WORLD_CREATE_FAILED;
        sender.sendRichMessage(message.message(locale, sender, placeholder));
        if (volume == null || !(sender instanceof Entity entity)) return;
        entity.teleportAsync(volume.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }
}
