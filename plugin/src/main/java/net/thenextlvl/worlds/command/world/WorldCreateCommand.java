package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.google.gson.JsonParseException;
import core.api.placeholder.Placeholder;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.image.Generator;
import net.thenextlvl.worlds.image.Image;
import net.thenextlvl.worlds.image.WorldImage;
import net.thenextlvl.worlds.preset.Preset;
import net.thenextlvl.worlds.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.World.Environment.CUSTOM;
import static org.bukkit.World.Environment.NORMAL;

class WorldCreateCommand {

    private static final File presets = new File(JavaPlugin.getPlugin(Worlds.class).getDataFolder(), "presets");

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder
                .literal("create")
                .argument(StringArgument.<CommandSender>builder("name").withSuggestionsProvider((context, token) ->
                        Image.findWorlds().stream()
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
                .flag(CommandFlag.builder("preset")
                        .withArgument(StringArgument.builder("preset").withSuggestionsProvider((context, token) ->
                                Preset.findPresets(presets).stream()
                                        .map(file -> file.getName().substring(0, file.getName().length() - 5))
                                        .filter(s -> s.startsWith(token) && !s.contains(" "))
                                        .toList())))
                .flag(CommandFlag.builder("identifier").withAliases("i")
                        .withArgument(StringArgument.builder("identifier").quoted()))
                .flag(CommandFlag.builder("seed").withAliases("s")
                        .withArgument(LongArgument.builder("seed")))
                .flag(CommandFlag.builder("structures"))
                .flag(CommandFlag.builder("hardcore"))
                .flag(CommandFlag.builder("load-manual"))
                .flag(CommandFlag.builder("temp"))
                .handler(WorldCreateCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var locale = sender instanceof Player player ? player.locale() : Messages.ENGLISH;
        try {
            handleCreate(context);
        } catch (JsonParseException e) {
            sender.sendRichMessage(Messages.WORLD_PRESET_INVALID.message(locale, sender));
        } catch (Exception e) {
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
                        WorldType.valueOf(s.toUpperCase().replace("-", "_")))
                .orElse(context.flags().contains("preset") ? WorldType.FLAT : WorldType.NORMAL);
        var identifier = context.flags().<String>get("identifier");
        var plugin = context.flags().<String>get("generator");
        var generator = plugin != null ? new Generator(plugin, identifier) : null;
        var seed = context.flags().<Long>getValue("seed").orElse(ThreadLocalRandom.current().nextLong());
        var loadManual = context.flags().contains("load-manual");
        var deleteOnShutdown = context.flags().contains("temp");
        var structures = context.flags().contains("structures");
        var hardcore = context.flags().contains("hardcore");
        var preset = context.flags().<String>get("preset");

        if (preset != null && generator != null) {
            sender.sendRichMessage(Messages.FLAG_COMBINATION.message(locale, sender,
                    Placeholder.of("flag-1", "generator"),
                    Placeholder.of("flag-2", "preset")));
            return;
        } else if (preset != null && !type.equals(WorldType.FLAT)) {
            sender.sendRichMessage(Messages.WORLD_PRESET_FLAT.message(locale, sender));
            return;
        } else if (preset != null) {
            final var fileName = preset + ".json";
            var match = Preset.findPresets(presets).stream()
                    .filter(file -> file.getName().equals(fileName))
                    .findFirst()
                    .map(Preset::of)
                    .orElse(null);
            if (match != null) preset = match.settings().toString();
            structures = true;
        }

        var image = Image.load(new WorldImage(name, preset, generator, environment, type,
                structures, hardcore, !loadManual, deleteOnShutdown, seed));
        var message = image != null ? Messages.WORLD_CREATE_SUCCEEDED : Messages.WORLD_CREATE_FAILED;
        sender.sendRichMessage(message.message(locale, sender, placeholder));
        if (image == null || !(sender instanceof Entity entity)) return;
        entity.teleportAsync(image.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
    }
}
