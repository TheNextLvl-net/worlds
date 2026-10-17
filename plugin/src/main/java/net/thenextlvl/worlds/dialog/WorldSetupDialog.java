package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.Level;
import net.thenextlvl.worlds.WorldOperationException;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.CommandFailureHandler;
import net.thenextlvl.worlds.generator.Generator;
import net.thenextlvl.worlds.generator.GeneratorType;
import net.thenextlvl.worlds.preset.Preset;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.thenextlvl.worlds.dialog.DialogSupport.WIDTH;
import static net.thenextlvl.worlds.dialog.DialogSupport.button;
import static net.thenextlvl.worlds.dialog.DialogSupport.getBoolean;
import static net.thenextlvl.worlds.dialog.DialogSupport.getText;
import static net.thenextlvl.worlds.dialog.DialogSupport.option;
import static net.thenextlvl.worlds.dialog.DialogSupport.parseKey;
import static net.thenextlvl.worlds.dialog.DialogSupport.text;
import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
public final class WorldSetupDialog {
    private static final String NO_GENERATOR = "none";
    private static final Map<GeneratorType, String> TYPES = new LinkedHashMap<>();

    static {
        TYPES.put(GeneratorType.NORMAL, "world.type.normal");
        TYPES.put(GeneratorType.FLAT, "world.type.flat");
        TYPES.put(GeneratorType.LARGE_BIOMES, "world.type.large_biomes");
        TYPES.put(GeneratorType.AMPLIFIED, "world.type.amplified");
        TYPES.put(GeneratorType.SINGLE_BIOME, "world.type.fixed");
        TYPES.put(GeneratorType.DEBUG, "world.type.debug");
    }

    private WorldSetupDialog() {
    }

    public static Dialog create(final WorldsPlugin plugin, final Player player) {
        return create(plugin, player, Settings.defaults(plugin), null);
    }

    public static Dialog recreate(final WorldsPlugin plugin, final Player player, final World source) {
        return create(plugin, player, Settings.of(plugin, source), source);
    }

    private static Dialog create(final WorldsPlugin plugin, final Player player,
                                 final Settings settings, @Nullable final World source) {
        final var inputs = new ArrayList<DialogInput>();
        inputs.add(DialogInput.text("key", text(plugin, player, "dialog.create.key"))
                .width(WIDTH).initial(settings.key()).maxLength(128).build());
        inputs.add(DialogInput.bool("hardcore", text(plugin, player, "dialog.create.hardcore"))
                .initial(settings.hardcore()).build());
        inputs.add(DialogInput.singleOption("difficulty", text(plugin, player, "dialog.create.difficulty"),
                Stream.of(Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD)
                        .map(difficulty -> option(id(difficulty), Component.translatable(difficulty), id(settings.difficulty())))
                        .toList()).width(WIDTH).build());
        inputs.add(DialogInput.text("seed", text(plugin, player, "dialog.create.seed"))
                .width(WIDTH).initial(settings.seed()).maxLength(64).build());
        inputs.add(DialogInput.bool("structures", text(plugin, player, "dialog.create.structures"))
                .initial(settings.structures()).build());
        inputs.add(DialogInput.bool("bonus_chest", text(plugin, player, "dialog.create.bonus-chest"))
                .initial(settings.bonusChest()).build());
        inputs.add(DialogInput.singleOption("dimension", text(plugin, player, "dialog.create.dimension"),
                Stream.concat(Dimension.dimensions(), plugin.customDimensions()).distinct()
                        .map(dimension -> option(dimension.key().asString(), Component.text(dimension.key().asString()), settings.dimension()))
                        .toList()).width(WIDTH).build());
        final var generators = generators(plugin);
        if (!generators.isEmpty())
            inputs.add(DialogInput.singleOption("generator", text(plugin, player, "dialog.create.generator"),
                    Stream.concat(Stream.of(option(NO_GENERATOR, text(plugin, player, "dialog.create.generator.none"), settings.generator())),
                            generators.stream().map(name -> option(name, Component.text(name), settings.generator()))
                    ).toList()).width(WIDTH).build());

        final var permission = source != null ? "worlds.command.recreate" : "worlds.command.create";
        final var submit = button(plugin, player, source != null ? "dialog.button.recreate" : "dialog.button.create", (viewer, response) -> {
            if (viewer.hasPermission(permission)) createWorld(plugin, viewer, settings.update(response), source);
        });
        final var gameRules = button(plugin, player, "dialog.button.gamerules", (viewer, response) -> {
            final var updated = settings.update(response);
            final var title = text(plugin, viewer, "dialog.gamerules.title.new");
            viewer.showDialog(GameRuleDialog.create(plugin, viewer, title, updated::gameRule,
                    (target, rules) -> target.showDialog(create(plugin, target, updated.withGameRules(rules), source)),
                    target -> target.showDialog(create(plugin, target, updated, source))));
        });
        final var cancel = button(plugin, player, "dialog.button.cancel", (viewer, response) -> {
            if (source != null) viewer.showDialog(WorldEditDialog.create(plugin, viewer, source));
        });
        final var type = button(text(plugin, player, "dialog.create.type",
                        Placeholder.component("type", text(plugin, player, TYPES.get(type(settings))))), WIDTH,
                (viewer, response) -> viewer.showDialog(typeDialog(plugin, viewer, settings.update(response), source)));

        final var actions = new ArrayList<ActionButton>();
        actions.add(type);
        if (GeneratorType.FLAT.name().equals(settings.type()))
            actions.add(presetButton(plugin, player, settings, source));
        if (GeneratorType.SINGLE_BIOME.name().equals(settings.type()))
            actions.add(biomeButton(plugin, player, settings, source));
        actions.add(gameRules);
        actions.add(submit);

        final var title = source != null
                ? text(plugin, player, "dialog.recreate.title", Placeholder.parsed("world", source.key().asString()))
                : text(plugin, player, "dialog.create.title");
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.multiAction(actions).exitAction(cancel).columns(1).build())
                .base(DialogBase.builder(title).canCloseWithEscape(true).inputs(inputs).build()));
    }

    private static GeneratorType type(final Settings settings) {
        return TYPES.keySet().stream()
                .filter(candidate -> candidate.name().equals(settings.type()))
                .findAny().orElse(GeneratorType.NORMAL);
    }

    private static ActionButton presetButton(final WorldsPlugin plugin, final Player player,
                                             final Settings settings, @Nullable final World source) {
        final var presets = presets();
        final var current = presets.get(settings.preset());
        final var label = text(plugin, player, "dialog.preset.button", Placeholder.component("preset",
                current != null ? PresetDialog.name(current) : Component.text(settings.preset())));
        return button(label, WIDTH, (viewer, response) -> {
            final var updated = settings.update(response);
            viewer.showDialog(PresetDialog.create(plugin, viewer, presets, updated.preset(),
                    (target, preset) -> target.showDialog(create(plugin, target,
                            updated.withType(updated.type(), preset, updated.biome()), source)),
                    target -> target.showDialog(create(plugin, target, updated, source))));
        });
    }

    @SuppressWarnings("PatternValidation")
    private static ActionButton biomeButton(final WorldsPlugin plugin, final Player player,
                                            final Settings settings, @Nullable final World source) {
        final var label = text(plugin, player, "dialog.biome.button",
                Placeholder.component("biome", BiomeDialog.name(Key.key(settings.biome()))));
        return button(label, WIDTH, (viewer, response) -> {
            final var updated = settings.update(response);
            viewer.showDialog(BiomeDialog.create(plugin, viewer, Key.key(updated.biome()),
                    (target, biome) -> target.showDialog(create(plugin, target,
                            updated.withType(updated.type(), updated.preset(), biome.asString()), source)),
                    target -> target.showDialog(create(plugin, target, updated, source))));
        });
    }

    private static Dialog typeDialog(final WorldsPlugin plugin, final Player player,
                                     final Settings settings, @Nullable final World source) {
        final var current = type(settings);
        final var actions = TYPES.entrySet().stream().map(entry -> {
            final var label = text(plugin, player, entry.getValue());
            return button(entry.getKey().equals(current) ? label.color(NamedTextColor.GREEN) : label, (viewer, response) ->
                    viewer.showDialog(create(plugin, viewer, settings.withType(entry.getKey().name(), settings.preset(), settings.biome()), source)));
        }).toList();
        final var back = button(plugin, player, "dialog.button.back",
                (viewer, response) -> viewer.showDialog(create(plugin, viewer, settings, source)));
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.multiAction(actions).exitAction(back).columns(2).build())
                .base(DialogBase.builder(text(plugin, player, "dialog.create.type.title")).canCloseWithEscape(true).build()));
    }

    private static void createWorld(final WorldsPlugin plugin, final Player player,
                                    final Settings settings, @Nullable final World source) {
        final var key = parseKey(settings.key()).orElse(null);
        if (key == null) {
            plugin.bundle().sendMessage(player, "dialog.create.key.invalid", Placeholder.unparsed("key", settings.key()));
            player.showDialog(create(plugin, player, settings, source));
            return;
        }
        final var placeholder = Placeholder.parsed("world", key.asString());
        if (plugin.getServer().getWorld(key) != null || plugin.getWorldRegistry().isRegistered(key)) {
            CommandFailureHandler.handle(plugin, player, new WorldOperationException(
                    WorldOperationException.Reason.WORLD_KEY_EXISTS
            ).key(key), placeholder);
            player.showDialog(create(plugin, player, settings, source));
            return;
        }

        final Level level;
        try {
            level = (source != null ? Level.copy(source) : Level.builder(key))
                    .key(key)
                    .hardcore(settings.hardcore())
                    .generatorType(settings.generatorType())
                    .seed(settings.seed().isBlank() ? null : seed(settings.seed()))
                    .structures(settings.structures())
                    .bonusChest(settings.bonusChest())
                    .dimension(new Dimension(Key.key(settings.dimension())))
                    .generator(settings.generator().equals(NO_GENERATOR) ? null : Generator.fromString(settings.generator()))
                    .build();
        } catch (final Exception e) {
            CommandFailureHandler.handle(plugin, player, e, placeholder);
            return;
        }

        final var message = source != null ? "world.recreate" : "world.create";
        plugin.bundle().sendMessage(player, message, placeholder);
        level.create().thenAccept(world -> {
            plugin.getWorldRegistry().register(level, true);
            world.setDifficulty(settings.difficulty());
            settings.gameRules().forEach((rule, value) -> GameRuleDialog.apply(world, rule, value));
            plugin.bundle().sendMessage(player, message + ".success", placeholder);
            player.teleportAsync(world.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            CommandFailureHandler.handle(plugin, player, throwable, placeholder);
            return null;
        });
    }

    private static long seed(final String seed) {
        try {
            return Long.parseLong(seed.trim());
        } catch (final NumberFormatException ignored) {
            return seed.hashCode();
        }
    }

    private static Map<String, Preset> presets() {
        return Preset.presets().stream()
                .filter(preset -> preset.name().isPresent())
                .collect(Collectors.toMap(WorldSetupDialog::id, preset -> preset));
    }

    private static List<String> generators(final WorldsPlugin plugin) {
        return Arrays.stream(plugin.getServer().getPluginManager().getPlugins())
                .filter(Plugin::isEnabled)
                .filter(plugin.generatorView()::hasGenerator)
                .map(Plugin::getName)
                .sorted()
                .toList();
    }

    private static String id(final Difficulty difficulty) {
        return difficulty.name().toLowerCase(Locale.ROOT);
    }

    private static String id(final Preset preset) {
        return preset.name().orElseThrow().toLowerCase(Locale.ROOT).replace(" ", "-").replace("'", "");
    }

    private record Settings(
            String key, boolean hardcore, Difficulty difficulty, String type, String preset, String biome,
            String seed, boolean structures, boolean bonusChest, String dimension, String generator,
            Map<GameRule<?>, Object> gameRules
    ) {
        static Settings defaults(final WorldsPlugin plugin) {
            final var key = plugin.levelView().findFreeKey("worlds", "new_world").asString();
            return new Settings(key, false, Difficulty.NORMAL, GeneratorType.NORMAL.name(),
                    id(Preset.CLASSIC_FLAT), "minecraft:plains", "", true, false,
                    Dimension.OVERWORLD.key().asString(), NO_GENERATOR, Map.of());
        }

        static Settings of(final WorldsPlugin plugin, final World world) {
            final var level = Level.copy(world);
            final var defaults = defaults(plugin);
            final var type = level.generatorType().orElse(GeneratorType.NORMAL);
            final var gameRules = new HashMap<GameRule<?>, Object>();
            GameRuleDialog.rules(plugin).forEach(rule -> {
                final var value = world.getGameRuleValue(rule);
                if (value != null) gameRules.put(rule, value);
            });
            return new Settings(
                    plugin.levelView().findFreeKey(world.key()).asString(),
                    level.hardcore().orElse(world.isHardcore()),
                    world.getDifficulty(),
                    type.name(),
                    type instanceof final GeneratorType.Flat flat && flat.preset().name().isPresent()
                            ? id(flat.preset()) : defaults.preset(),
                    type instanceof final GeneratorType.SingleBiome single ? single.biome().asString() : defaults.biome(),
                    String.valueOf(world.getSeed()),
                    level.structures().orElse(world.canGenerateStructures()),
                    level.bonusChest().orElse(defaults.bonusChest()),
                    level.dimension().orElse(plugin.getDimension(world)).key().asString(),
                    level.generator().map(generator -> generator.getPlugin().getName()).orElse(NO_GENERATOR),
                    Map.copyOf(gameRules)
            );
        }

        Settings update(final DialogResponseView response) {
            final var key = getText(response, "key", this.key).trim();
            return new Settings(
                    key.isBlank() ? this.key : key,
                    getBoolean(response, "hardcore", hardcore),
                    Optional.ofNullable(response.getText("difficulty"))
                            .map(id -> Difficulty.valueOf(id.toUpperCase(Locale.ROOT))).orElse(difficulty),
                    getText(response, "type", type),
                    getText(response, "preset", preset),
                    getText(response, "biome", biome),
                    getText(response, "seed", seed),
                    getBoolean(response, "structures", structures),
                    getBoolean(response, "bonus_chest", bonusChest),
                    getText(response, "dimension", dimension),
                    getText(response, "generator", generator),
                    gameRules
            );
        }

        Settings withType(final String type, final String preset, final String biome) {
            return new Settings(key, hardcore, difficulty, type, preset, biome, seed,
                    structures, bonusChest, dimension, generator, gameRules);
        }

        Settings withGameRules(final Map<GameRule<?>, Object> gameRules) {
            return new Settings(key, hardcore, difficulty, type, preset, biome, seed,
                    structures, bonusChest, dimension, generator, Map.copyOf(gameRules));
        }

        Object gameRule(final GameRule<?> rule) {
            final var value = gameRules.get(rule);
            if (value != null) return value;
            return rule.getDefaultValue();
        }

        @SuppressWarnings("PatternValidation")
        GeneratorType generatorType() {
            final var generatorType = GeneratorType.generatorTypes()
                    .filter(candidate -> candidate.name().equals(type))
                    .findAny().orElse(GeneratorType.NORMAL);
            if (generatorType instanceof final GeneratorType.Flat flat)
                return Optional.ofNullable(presets().get(preset)).map(flat::with).orElse(flat);
            if (generatorType instanceof final GeneratorType.SingleBiome single)
                return single.with(Key.key(biome));
            return generatorType;
        }
    }
}
