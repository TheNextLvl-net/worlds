package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.Level;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandOptionsArgument;
import net.thenextlvl.worlds.command.argument.DimensionArgumentType;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.argument.SeedArgument;
import net.thenextlvl.worlds.command.argument.WorldPresetArgument;
import net.thenextlvl.worlds.command.brigadier.BrigadierCommand;
import net.thenextlvl.worlds.generator.Generator;
import net.thenextlvl.worlds.generator.GeneratorType;
import net.thenextlvl.worlds.preset.Preset;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldCreateCommand extends BrigadierCommand {
    private WorldCreateCommand(final WorldsPlugin plugin) {
        super(plugin, "create", "worlds.command.create");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldCreateCommand(plugin);
        return command.create().then(command.createCommand());
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> createCommand() {
        final var generator = Commands.argument("generator", new GeneratorArgument(plugin))
                .then(createOptionsArgument())
                .executes(context -> run(context, null));

        final var type = Commands.literal("type");
        createTypeArgument(type, GeneratorType.AMPLIFIED, createOptionsArgument(GeneratorType.AMPLIFIED));
        createTypeArgument(type, GeneratorType.DEBUG, createOptionsArgument(GeneratorType.DEBUG));
        createTypeArgument(type, GeneratorType.FLAT, createPresetArgument());
        createTypeArgument(type, GeneratorType.LARGE_BIOMES, createOptionsArgument(GeneratorType.LARGE_BIOMES));
        createTypeArgument(type, GeneratorType.NORMAL, createOptionsArgument(GeneratorType.NORMAL));
        createTypeArgument(type, GeneratorType.SINGLE_BIOME, createBiomeArgument());

        return Commands.argument("key", new KeyArgument())
                .then(Commands.literal("generator").then(generator))
                .then(type)
                .executes(context -> run(context, GeneratorType.NORMAL));
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> createOptionsArgument() {
        return createOptionsArgument(null);
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> createPresetArgument() {
        return Commands.argument("preset", new WorldPresetArgument(plugin))
                .then(createOptionsArgument(GeneratorType.FLAT))
                .executes(context -> run(context, GeneratorType.FLAT));
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> createBiomeArgument() {
        return Commands.argument("biome", ArgumentTypes.resourceKey(RegistryKey.BIOME))
                .then(createOptionsArgument(GeneratorType.SINGLE_BIOME))
                .executes(context -> run(context, GeneratorType.SINGLE_BIOME));
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> createOptionsArgument(final @Nullable GeneratorType generatorType) {
        return Commands.argument("options", new CommandOptionsArgument(Map.of(
                "bonus-chest", BoolArgumentType.bool(),
                "dimension", new DimensionArgumentType(plugin),
                "hardcore", BoolArgumentType.bool(),
                "seed", new SeedArgument(),
                "structures", BoolArgumentType.bool()
        ))).executes(context -> run(context, generatorType));
    }

    private LiteralArgumentBuilder<CommandSourceStack> createTypeArgument(
            final LiteralArgumentBuilder<CommandSourceStack> type,
            final GeneratorType generatorType,
            final ArgumentBuilder<CommandSourceStack, ?> leaf) {
        final var literal = Commands.literal(generatorType.name()).then(leaf).executes(context -> run(context, generatorType));
        type.then(literal);
        return literal;
    }

    private int run(final CommandContext<CommandSourceStack> context, final @Nullable GeneratorType generatorType) {
        final var sender = context.getSource().getSender();
        final var level = buildLevel(context, generatorType);
        final var placeholder = Placeholder.parsed("world", level.key().asString());

        plugin.bundle().sendMessage(sender, "world.create", placeholder);
        level.create().thenAccept(world -> {
            plugin.getWorldRegistry().register(level, true);
            plugin.bundle().sendMessage(sender, "world.create.success", placeholder);
            if (!(sender instanceof final Entity entity)) return;
            entity.teleportAsync(world.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            CommandFailureHandler.handle(plugin, sender, throwable, placeholder);
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }

    private Level buildLevel(
            final CommandContext<CommandSourceStack> context,
            final @Nullable GeneratorType generatorType
    ) {
        final var key = context.getArgument("key", Key.class);

        final var options = tryGetArgument(context, "options", CommandOptionsArgument.Options.class)
                .orElseGet(CommandOptionsArgument.Options::new);
        final var bonusChest = options.getArgument("bonus-chest", Boolean.class).orElse(null);
        final var dimension = options.getArgument("dimension", Dimension.class).orElse(null);
        final var hardcore = options.getArgument("hardcore", Boolean.class).orElse(null);
        final var seed = options.getArgument("seed", Long.class).orElse(null);
        final var structures = options.getArgument("structures", Boolean.class).orElse(null);

        final var generator = tryGetArgument(context, "generator", Generator.class).orElse(null);
        final var type = generatorType != null ? getGeneratorType(context, generatorType) : null;

        return Level.builder(key)
                .dimension(dimension)
                .generator(generator)
                .seed(seed)
                .structures(structures)
                .generatorType(type)
                .bonusChest(bonusChest)
                .hardcore(hardcore)
                .build();
    }

    private GeneratorType getGeneratorType(
            final CommandContext<CommandSourceStack> context,
            final GeneratorType generatorType
    ) {
        return tryGetArgument(context, "preset", Preset.class).<GeneratorType>map(GeneratorType.FLAT::with)
                .or(() -> tryGetArgument(context, "biome", Key.class).map(GeneratorType.SINGLE_BIOME::with))
                .orElse(generatorType);
    }
}
