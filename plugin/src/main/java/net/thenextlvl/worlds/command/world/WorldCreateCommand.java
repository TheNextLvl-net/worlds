package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import com.google.gson.JsonParseException;
import core.io.IO;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.image.DeletionType;
import net.thenextlvl.worlds.image.Generator;
import net.thenextlvl.worlds.image.Image;
import net.thenextlvl.worlds.image.WorldImage;
import net.thenextlvl.worlds.preset.Preset;
import net.thenextlvl.worlds.preset.PresetFile;
import net.thenextlvl.worlds.preset.Presets;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.World.Environment.CUSTOM;
import static org.bukkit.World.Environment.NORMAL;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

class WorldCreateCommand {
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("create")
                .permission("worlds.command.world.create")
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
                .flag(CommandFlag.builder("base").withAliases("b")
                        .withArgument(StringArgument.builder("world").withSuggestionsProvider((context, token) ->
                                Bukkit.getWorlds().stream()
                                        .map(WorldInfo::getName)
                                        .filter(s -> s.startsWith(token))
                                        .toList())))
                .flag(CommandFlag.builder("preset")
                        .withArgument(StringArgument.builder("preset").withSuggestionsProvider((context, token) ->
                                PresetFile.findPresets(plugin.presetsFolder()).stream()
                                        .map(file -> file.getName().substring(0, file.getName().length() - 5))
                                        .filter(s -> s.startsWith(token) && !s.contains(" "))
                                        .toList())))
                .flag(CommandFlag.builder("identifier").withAliases("i")
                        .withArgument(StringArgument.builder("identifier").quoted()))
                .flag(CommandFlag.builder("deletion").withAliases("d")
                        .withArgument(StringArgument.builder("deletion").withSuggestionsProvider((context, token) ->
                                Arrays.stream(DeletionType.values())
                                        .map(type -> type.name().toLowerCase().replace("_", "-"))
                                        .filter(s -> s.startsWith(token))
                                        .toList())))
                .flag(CommandFlag.builder("seed").withAliases("s")
                        .withArgument(StringArgument.builder("seed")))
                .flag(CommandFlag.builder("auto-save")
                        .withArgument(BooleanArgument.of("auto-save")))
                .flag(CommandFlag.builder("structures")
                        .withArgument(BooleanArgument.of("structures")))
                .flag(CommandFlag.builder("hardcore"))
                .flag(CommandFlag.builder("load-manual"))
                .handler(WorldCreateCommand::execute);
    }

    private static void execute(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        try {
            handleCreate(context);
        } catch (JsonParseException e) {
            plugin.bundle().sendMessage(sender, "world.preset.invalid");
        } catch (Exception e) {
            plugin.bundle().sendMessage(sender, "command.argument");
        }
    }

    private static void handleCreate(CommandContext<CommandSender> context) {
        var sender = context.getSender();
        var name = context.<String>get("name");
        var placeholder = Placeholder.parsed("world", name);

        if (Bukkit.getWorld(name) != null) {
            plugin.bundle().sendMessage(sender, "world.exists", placeholder);
            return;
        }

        var base = context.flags().<String>getValue("base")
                .map(Bukkit::getWorld).orElse(null);
        var environment = context.flags().<String>getValue("environment").map(s ->
                        Environment.valueOf(s.toUpperCase().replace("-", "_")))
                .orElse(base != null ? base.getEnvironment() : NORMAL);
        var type = context.flags().<String>getValue("type").map(s ->
                        WorldType.valueOf(s.toUpperCase().replace("-", "_")))
                .orElse(context.flags().contains("preset") ? WorldType.FLAT : WorldType.NORMAL);
        var identifier = context.flags().<String>get("identifier");
        var plugin = context.flags().<String>get("generator");
        var generator = plugin != null ? new Generator(plugin, identifier) : null;
        var seed = context.flags().<String>getValue("seed").map(s -> {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return s.hashCode();
            }
        }).orElse(base != null ? base.getSeed() : ThreadLocalRandom.current().nextLong()).longValue();
        var deletion = context.flags().<String>getValue("deletion").map(s ->
                        DeletionType.valueOf(s.toUpperCase().replace("-", "_")))
                .orElse(null);
        var loadManual = context.flags().contains("load-manual");
        var structures = context.flags().<Boolean>getValue("structures")
                .orElse(base == null || base.canGenerateStructures());
        var autoSave = context.flags().<Boolean>getValue("auto-save")
                .orElse(base == null || base.isAutoSave());
        var hardcore = context.flags().contains("hardcore") || base != null && base.isHardcore();
        var preset = context.flags().<String>get("preset");

        if (preset != null && generator != null) {
            WorldCreateCommand.plugin.bundle().sendMessage(sender, "command.flag.combination",
                    Placeholder.parsed("flag-1", "generator"),
                    Placeholder.parsed("flag-2", "preset"));
            return;
        } else if (preset != null && !type.equals(WorldType.FLAT)) {
            WorldCreateCommand.plugin.bundle().sendMessage(sender, "world.preset.flat");
            return;
        } else if (preset != null) {
            final var fileName = preset + ".json";
            var match = PresetFile.of(IO.of(WorldCreateCommand.plugin.presetsFolder(), fileName));
            if (match != null) preset = match.settings().toString();
        } else if (type.equals(WorldType.FLAT)) {
            preset = Preset.serialize(Presets.CLASSIC_FLAT).toString();
        }

        if (base != null) {
            var world = Placeholder.parsed("world", base.getName());
            if (copy(base.getWorldFolder(), new File(Bukkit.getWorldContainer(), name)))
                WorldCreateCommand.plugin.bundle().sendMessage(sender, "world.copy.success", world);
            else WorldCreateCommand.plugin.bundle().sendMessage(sender, "world.copy.failed", world);
        }
        var image = Image.load(new WorldImage(name, preset, generator, deletion,
                environment, type, autoSave, structures, hardcore, !loadManual, seed));
        var message = image != null ? "world.create.success" : "world.create.failed";
        WorldCreateCommand.plugin.bundle().sendMessage(sender, message, placeholder);
        if (image == null || !(sender instanceof Entity entity)) return;
        entity.teleportAsync(image.getWorld().getSpawnLocation().add(0.5, 0, 0.5), COMMAND);
    }

    private static boolean copy(File source, File destination) {
        return source.isDirectory() ? copyDirectory(source, destination) : copyFile(source, destination);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean copyDirectory(File source, File destination) {
        if (!destination.exists()) destination.mkdir();
        var list = source.list();
        if (list == null) return false;
        List.of(list).forEach(file -> copy(
                new File(source, file),
                new File(destination, file)
        ));
        return true;
    }

    private static boolean copyFile(File source, File destination) {
        if (source.getName().equals("uid.dat")) return true;
        try (var in = new FileInputStream(source);
             var out = new FileOutputStream(destination)) {
            int length;
            var buf = new byte[1024];
            while ((length = in.read(buf)) > 0)
                out.write(buf, 0, length);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
