package net.thenextlvl.worlds.command.world;

import com.google.gson.JsonParseException;
import core.io.IO;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
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
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.TypedCommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
class WorldCreateCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSender> builder;

    Command.Builder<CommandSender> create() {
        return builder.literal("create")
                .permission("worlds.command.world.create")
                .required("name", StringParser.stringParser(),
                        SuggestionProvider.blocking((context, input) ->
                                Image.findWorlds().stream()
                                        .map(File::getName)
                                        .filter(s -> Bukkit.getWorld(s) == null)
                                        .map(Suggestion::simple)
                                        .toList()))
                .flag(CommandFlag.builder("type").withAliases("t")
                        .withDescription(RichDescription.of(Component.text("The world type")))
                        .withComponent(TypedCommandComponent.ofType(String.class, "type")
                                .parser(StringParser.stringParser())
                                .suggestionProvider(SuggestionProvider.blocking((context, input) ->
                                        Arrays.stream(WorldType.values())
                                                .map(type -> type.name().toLowerCase().replace("_", "-"))
                                                .map(Suggestion::simple)
                                                .toList()))))
                .flag(CommandFlag.builder("environment").withAliases("e")
                        .withDescription(RichDescription.of(Component.text("The environment")))
                        .withComponent(TypedCommandComponent.ofType(String.class, "environment")
                                .suggestionProvider(SuggestionProvider.blocking((context, input) ->
                                        Arrays.stream(Environment.values())
                                                .filter(environment -> !environment.equals(Environment.CUSTOM))
                                                .map(type -> type.name().toLowerCase().replace("_", "-"))
                                                .map(Suggestion::simple)
                                                .toList()))))
                .flag(CommandFlag.builder("generator").withAliases("g")
                        .withDescription(RichDescription.of(Component.text("The generator plugin")))
                        .withComponent(TypedCommandComponent.ofType(String.class, "generator").suggestionProvider(
                                SuggestionProvider.blocking((context, input) ->
                                        Arrays.stream(Bukkit.getPluginManager().getPlugins())
                                                .filter(plugin -> Generator.hasChunkGenerator(plugin.getClass())
                                                                  || Generator.hasBiomeProvider(plugin.getClass()))
                                                .map(Plugin::getName)
                                                .map(Suggestion::simple)
                                                .toList()))))
                .flag(CommandFlag.builder("base").withAliases("b")
                        .withDescription(RichDescription.of(Component.text("The world to clone")))
                        .withComponent(TypedCommandComponent.ofType(World.class, "world").suggestionProvider(
                                SuggestionProvider.blocking((context, input) -> Bukkit.getWorlds().stream()
                                        .map(WorldInfo::getName)
                                        .map(Suggestion::simple)
                                        .toList()))))
                .flag(CommandFlag.builder("preset")
                        .withDescription(RichDescription.of(Component.text("The preset to use")))
                        .withComponent(TypedCommandComponent.ofType(String.class, "preset")
                                .suggestionProvider(SuggestionProvider.blocking((context, input) ->
                                        PresetFile.findPresets(plugin.presetsFolder()).stream()
                                                .map(file -> file.getName().substring(0, file.getName().length() - 5))
                                                .map(name -> name.contains(" ") ? "\"" + name + "\"" : name)
                                                .map(Suggestion::simple)
                                                .toList()))))
                .flag(CommandFlag.builder("deletion").withAliases("d")
                        .withDescription(RichDescription.of(Component.text("What to do with the world on shutdown")))
                        .withComponent(TypedCommandComponent.ofType(String.class, "deletion")
                                .suggestionProvider(SuggestionProvider.blocking((context, input) ->
                                        Arrays.stream(DeletionType.values())
                                                .map(type -> type.name().toLowerCase().replace("_", "-"))
                                                .map(Suggestion::simple)
                                                .toList()))))
                .flag(CommandFlag.builder("identifier").withAliases("i")
                        .withDescription(RichDescription.of(Component.text("The identifier of the world generator")))
                        .withComponent(TypedCommandComponent.ofType(String.class, "identifier")))
                .flag(CommandFlag.builder("seed").withAliases("s")
                        .withDescription(RichDescription.of(Component.text("The seed")))
                        .withComponent(TypedCommandComponent.ofType(String.class, "seed")))
                .flag(CommandFlag.builder("auto-save")
                        .withDescription(RichDescription.of(Component.text("Whether the world should auto-save")))
                        .withComponent(TypedCommandComponent.ofType(boolean.class, "auto-save")))
                .flag(CommandFlag.builder("structures")
                        .withDescription(RichDescription.of(Component.text("Whether structures should generate")))
                        .withComponent(TypedCommandComponent.ofType(boolean.class, "structures")))
                .flag(CommandFlag.builder("hardcore")
                        .withDescription(RichDescription.of(Component.text("Whether hardcore is enabled"))))
                .flag(CommandFlag.builder("load-manual")
                        .withDescription(RichDescription.of(Component.text("Whether the world must be loaded manual on startup"))))
                .handler(this::execute);
    }

    private void execute(CommandContext<CommandSender> context) {
        try {
            handleCreate(context);
        } catch (JsonParseException e) {
            plugin.bundle().sendMessage(context.sender(), "world.preset.invalid");
        } catch (Exception e) {
            plugin.bundle().sendMessage(context.sender(), "command.argument");
        }
    }

    private void handleCreate(CommandContext<CommandSender> context) {
        var name = context.<String>get("name");
        var placeholder = Placeholder.parsed("world", name);

        if (Bukkit.getWorld(name) != null) {
            plugin.bundle().sendMessage(context.sender(), "world.known", placeholder);
            return;
        }

        var base = context.flags().<String>getValue("base")
                .map(Bukkit::getWorld).orElse(null);
        var environment = context.flags().<String>getValue("environment").map(s ->
                        Environment.valueOf(s.toUpperCase().replace("-", "_")))
                .orElse(base != null ? base.getEnvironment() : Environment.NORMAL);
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
            this.plugin.bundle().sendMessage(context.sender(), "command.flag.combination",
                    Placeholder.parsed("flag-1", "generator"),
                    Placeholder.parsed("flag-2", "preset"));
            return;
        } else if (preset != null && !type.equals(WorldType.FLAT)) {
            this.plugin.bundle().sendMessage(context.sender(), "world.preset.flat");
            return;
        } else if (preset != null) {
            final var fileName = preset + ".json";
            var match = PresetFile.of(IO.of(this.plugin.presetsFolder(), fileName));
            if (match != null) preset = match.settings().toString();
        } else if (type.equals(WorldType.FLAT)) {
            preset = Preset.serialize(Presets.CLASSIC_FLAT).toString();
        }

        if (base != null) {
            var world = Placeholder.parsed("world", base.getName());
            if (copy(base.getWorldFolder(), new File(Bukkit.getWorldContainer(), name)))
                this.plugin.bundle().sendMessage(context.sender(), "world.copy.success", world);
            else this.plugin.bundle().sendMessage(context.sender(), "world.copy.failed", world);
        }
        var image = Image.load(new WorldImage(name, preset, generator, deletion,
                environment, type, autoSave, structures, hardcore, !loadManual, seed));
        var message = image != null ? "world.create.success" : "world.create.failed";
        this.plugin.bundle().sendMessage(context.sender(), message, placeholder);
        if (image == null || !(context.sender() instanceof Entity entity)) return;
        entity.teleportAsync(image.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
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
