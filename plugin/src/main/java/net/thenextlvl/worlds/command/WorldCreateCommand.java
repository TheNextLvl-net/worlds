package net.thenextlvl.worlds.command;

import com.google.gson.JsonObject;
import core.io.IO;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Worlds;
import net.thenextlvl.worlds.image.DeletionType;
import net.thenextlvl.worlds.image.Generator;
import net.thenextlvl.worlds.preset.Preset;
import net.thenextlvl.worlds.preset.PresetFile;
import net.thenextlvl.worlds.preset.Presets;
import net.thenextlvl.worlds.util.WorldReader;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.bukkit.parser.WorldParser;
import org.incendo.cloud.component.TypedCommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
class WorldCreateCommand {
    private final Worlds plugin;
    private final Command.Builder<CommandSourceStack> builder;

    Command.Builder<CommandSourceStack> create() {
        return builder.literal("create")
                .permission("worlds.command.world.create")
                .required("name", StringParser.stringParser(),
                        SuggestionProvider.blocking((context, input) ->
                                plugin.imageProvider().findWorldFiles().stream()
                                        .map(File::getName)
                                        .filter(s -> Bukkit.getWorld(s) == null)
                                        .map(Suggestion::suggestion)
                                        .toList()))
                .flag(CommandFlag.<CommandSourceStack>builder("type").withAliases("t")
                        .withDescription(RichDescription.of(Component.text("The world type")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, WorldType>ofType(WorldType.class, "type")
                                .parser(EnumParser.enumParser(WorldType.class))))
                .flag(CommandFlag.<CommandSourceStack>builder("environment").withAliases("e")
                        .withDescription(RichDescription.of(Component.text("The environment")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, Environment>ofType(Environment.class, "environment")
                                .parser(EnumParser.enumParser(Environment.class))))
                .flag(CommandFlag.<CommandSourceStack>builder("generator").withAliases("g")
                        .withDescription(RichDescription.of(Component.text("The generator plugin")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, String>ofType(String.class, "generator")
                                .parser(StringParser.greedyFlagYieldingStringParser())
                                .suggestionProvider(SuggestionProvider.blocking((context, input) ->
                                        Arrays.stream(Bukkit.getPluginManager().getPlugins())
                                                .filter(plugin -> Generator.hasChunkGenerator(plugin.getClass())
                                                                  || Generator.hasBiomeProvider(plugin.getClass()))
                                                .map(Plugin::getName)
                                                .map(Suggestion::suggestion)
                                                .toList()))))
                .flag(CommandFlag.<CommandSourceStack>builder("base").withAliases("b")
                        .withDescription(RichDescription.of(Component.text("The world to clone")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, World>ofType(World.class, "world")
                                .parser(WorldParser.worldParser())))
                .flag(CommandFlag.<CommandSourceStack>builder("preset")
                        .withDescription(RichDescription.of(Component.text("The preset to use")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, String>ofType(String.class, "preset")
                                .parser(StringParser.greedyFlagYieldingStringParser())
                                .suggestionProvider(SuggestionProvider.blocking((context, input) ->
                                        PresetFile.findPresets(plugin.presetsFolder()).stream()
                                                .map(file -> file.getName().substring(0, file.getName().length() - 5))
                                                .map(name -> name.contains(" ") ? "\"" + name + "\"" : name)
                                                .map(Suggestion::suggestion)
                                                .toList()))))
                .flag(CommandFlag.<CommandSourceStack>builder("deletion").withAliases("d")
                        .withDescription(RichDescription.of(Component.text("What to do with the world on shutdown")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, DeletionType>ofType(DeletionType.class, "deletion")
                                .parser(EnumParser.enumParser(DeletionType.class))))
                .flag(CommandFlag.<CommandSourceStack>builder("identifier").withAliases("i")
                        .withDescription(RichDescription.of(Component.text("The identifier of the world generator")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, String>ofType(String.class, "identifier")
                                .parser(StringParser.greedyFlagYieldingStringParser())))
                .flag(CommandFlag.<CommandSourceStack>builder("key")
                        .withDescription(RichDescription.of(Component.text("The namespaced key")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, NamespacedKey>ofType(NamespacedKey.class, "key")
                                .parser(NamespacedKeyParser.namespacedKeyParser(true))))
                .flag(CommandFlag.<CommandSourceStack>builder("seed").withAliases("s")
                        .withDescription(RichDescription.of(Component.text("The seed")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, String>ofType(String.class, "seed")
                                .parser(StringParser.greedyFlagYieldingStringParser())))
                .flag(CommandFlag.<CommandSourceStack>builder("auto-save")
                        .withDescription(RichDescription.of(Component.text("Whether the world should auto-save")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, Boolean>ofType(boolean.class, "auto-save")
                                .parser(BooleanParser.booleanParser())))
                .flag(CommandFlag.<CommandSourceStack>builder("structures")
                        .withDescription(RichDescription.of(Component.text("Whether structures should generate")))
                        .withComponent(TypedCommandComponent.<CommandSourceStack, Boolean>ofType(boolean.class, "structures")
                                .parser(BooleanParser.booleanParser())))
                .flag(CommandFlag.<CommandSourceStack>builder("hardcore")
                        .withDescription(RichDescription.of(Component.text("Whether hardcore is enabled"))))
                .flag(CommandFlag.<CommandSourceStack>builder("load-manual")
                        .withDescription(RichDescription.of(Component.text("Whether the world must be loaded manual on startup"))))
                .handler(this::execute);
    }

    @SuppressWarnings("deprecation")
    private void execute(CommandContext<CommandSourceStack> context) {
        var name = context.<String>get("name");

        var sender = context.sender().getSender();

        if (Bukkit.getWorld(name) != null) {
            plugin.bundle().sendMessage(sender, "world.known", Placeholder.parsed("world", name));
            return;
        }

        var worldReader = new WorldReader(name);

        var environment = context.flags().<Environment>getValue("environment").orElse(Environment.NORMAL);
        if (environment.equals(Environment.CUSTOM)) {
            plugin.bundle().sendMessage(sender, "environment.custom");
            return;
        }

        var base = context.flags().<World>getValue("base");
        var key = context.flags().<NamespacedKey>getValue("key").orElse(new NamespacedKey("worlds", name.toLowerCase()));
        var type = context.flags().<WorldType>getValue("type")
                .orElse(context.flags().contains("preset") ? WorldType.FLAT : WorldType.NORMAL);
        var identifier = context.flags().<String>getValue("identifier", null);
        var generator = context.flags().<String>getValue("generator")
                .map(string -> new Generator(string, identifier)).orElse(null);
        var seed = context.flags().<String>getValue("seed").map(s -> {
                    try {
                        return Long.parseLong(s);
                    } catch (NumberFormatException e) {
                        return s.hashCode();
                    }
                }).orElse(worldReader.seed().orElse(base.map(WorldInfo::getSeed)
                        .orElse(ThreadLocalRandom.current().nextLong())))
                .longValue();
        var deletion = context.flags().<DeletionType>getValue("deletion", null);
        var loadManual = context.flags().contains("load-manual");
        var structures = context.flags().<Boolean>getValue("structures")
                .orElse(worldReader.generateStructures().orElse(base.map(World::canGenerateStructures).orElse(true)));
        var autoSave = context.flags().<Boolean>getValue("auto-save")
                .orElse(base.map(World::isAutoSave).orElse(true));
        var hardcore = context.flags().contains("hardcore") || worldReader.hardcore()
                .orElse(base.map(World::isHardcore).orElse(false));
        var preset = context.flags().<String>getValue("preset", null);
        JsonObject settings = null;

        if (preset != null && generator != null) {
            plugin.bundle().sendMessage(sender, "command.flag.combination",
                    Placeholder.parsed("flag-1", "generator"),
                    Placeholder.parsed("flag-2", "preset"));
            return;
        } else if (preset != null && !Objects.equals(type, WorldType.FLAT)) {
            plugin.bundle().sendMessage(sender, "world.preset.flat");
            return;
        } else if (preset != null) {
            final var fileName = preset + ".json";
            var match = PresetFile.of(IO.of(plugin.presetsFolder(), fileName));
            if (match != null) settings = match.settings();
        } else if (type.equals(WorldType.FLAT)) {
            settings = Preset.serialize(Presets.CLASSIC_FLAT);
        }

        base.ifPresent(world -> {
            var placeholder = Placeholder.parsed("world", world.getName());
            if (copy(world.getWorldFolder(), new File(Bukkit.getWorldContainer(), name)))
                plugin.bundle().sendMessage(sender, "world.clone.success", placeholder);
            else plugin.bundle().sendMessage(sender, "world.clone.failed", placeholder);
        });

        var image = plugin.imageProvider().load(plugin.imageProvider().createWorldImage()
                .name(name).key(key).settings(settings).generator(generator).deletionType(deletion)
                .environment(environment).worldType(type).autoSave(autoSave).generateStructures(structures)
                .hardcore(hardcore).loadOnStart(!loadManual).seed(seed));

        var message = image != null ? "world.create.success" : "world.create.failed";
        plugin.bundle().sendMessage(sender, message, Placeholder.parsed("world", name));
        if (image == null || !(sender instanceof Entity entity)) return;
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
