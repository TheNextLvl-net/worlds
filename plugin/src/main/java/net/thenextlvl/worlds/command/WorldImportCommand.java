package net.thenextlvl.worlds.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.Dimension;
import net.thenextlvl.worlds.Level;
import net.thenextlvl.worlds.WorldOperationException;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.argument.CommandOptionsArgument;
import net.thenextlvl.worlds.command.argument.DimensionArgumentType;
import net.thenextlvl.worlds.command.argument.GeneratorArgument;
import net.thenextlvl.worlds.command.argument.KeyArgument;
import net.thenextlvl.worlds.command.brigadier.SimpleCommand;
import net.thenextlvl.worlds.command.suggestion.WorldKeyImportSuggestionProvider;
import net.thenextlvl.worlds.command.suggestion.WorldPathKeyImportSuggestionProvider;
import net.thenextlvl.worlds.generator.Generator;
import net.thenextlvl.worlds.generator.GeneratorType;
import net.thenextlvl.worlds.preset.Preset;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND;

@NullMarked
final class WorldImportCommand extends SimpleCommand {
    private WorldImportCommand(final WorldsPlugin plugin) {
        super(plugin, "import", "worlds.command.import");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldImportCommand(plugin);
        final var importSuggestions = new WorldKeyImportSuggestionProvider(plugin);

        final var key = Commands.argument("key", new KeyArgument());
        final var path = Commands.argument("path", StringArgumentType.string())
                .suggests(importSuggestions);
        final var pathKey = Commands.argument("key", new KeyArgument())
                .suggests(new WorldPathKeyImportSuggestionProvider(plugin));
        return command.create()
                .then(key.then(command.options()).executes(command))
                .then(path.then(pathKey.then(command.options()).executes(command)));
    }

    private ArgumentBuilder<CommandSourceStack, ?> options() {
        final var options = new HashMap<String, @Nullable ArgumentType<?>>();
        options.put("dimension", new DimensionArgumentType(plugin));
        options.put("generator", new GeneratorArgument(plugin));
        options.put("--void-world", null);
        return Commands.argument("options", new CommandOptionsArgument(options)).executes(this);
    }

    @Override
    public int run(final CommandContext<CommandSourceStack> context) {
        final var sender = context.getSource().getSender();
        final var key = context.getArgument("key", Key.class);

        final var options = tryGetArgument(context, "options", CommandOptionsArgument.Options.class)
                .orElseGet(CommandOptionsArgument.Options::new);
        final var dimension = options.getArgument("dimension", Dimension.class).orElse(null);
        final var generator = options.getArgument("generator", Generator.class).orElse(null);

        if (plugin.getWorldRegistry().isRegistered(key)) {
            CommandFailureHandler.handle(plugin, sender, new WorldOperationException(
                    WorldOperationException.Reason.WORLD_KEY_EXISTS
            ).key(key));
            return 0;
        }

        final var placeholder = Placeholder.parsed("world", key.asString());

        final var builder = Level.builder(key)
                .generator(generator)
                .dimension(dimension);
        if (options.contains("--void-world")) builder
                .generatorType(GeneratorType.FLAT.with(Preset.THE_VOID))
                .ignoreLevelData(true);

        try {
            tryGetArgument(context, "path", String.class)
                    .map(this::resolveSource)
                    .ifPresent(path -> prepareSource(path, builder));
        } catch (final RuntimeException e) {
            CommandFailureHandler.handle(plugin, sender, e, placeholder);
            return 0;
        }
        final var build = builder.build();
        plugin.getWorldRegistry().register(build, true);

        plugin.bundle().sendMessage(sender, "world.import", placeholder);

        build.create().thenAccept(level -> {
            plugin.bundle().sendMessage(sender, "world.import.success",
                    Placeholder.parsed("world", level.key().asString()));
            if (!(sender instanceof final Entity entity)) return;
            entity.teleportAsync(level.getSpawnLocation(), COMMAND);
        }).exceptionally(throwable -> {
            CommandFailureHandler.handle(plugin, sender, throwable, placeholder);
            return null;
        });

        return SINGLE_SUCCESS;
    }

    private Path resolveSource(final String input) {
        final var path = Path.of(input);
        return path.isAbsolute()
                ? path
                : plugin.getServer().getWorldContainer().toPath().resolve(path);
    }

    private void prepareSource(final Path input, final Level.Builder builder) {
        final var source = input.toAbsolutePath().normalize();
        if (!Files.isDirectory(source)) throw new WorldOperationException(
                WorldOperationException.Reason.WORLD_NOT_FOUND
        ).path(source);

        final var target = plugin.resolveLevelDirectory(builder.key()).toAbsolutePath().normalize();

        ensureNotLoaded(source);
        ensureTargetAvailable(source, target);

        if (Files.isRegularFile(source.resolve("level.dat"))
                || Files.isRegularFile(source.resolve("level.dat_old"))) {
            final var legacyName = source.getFileName().toString();
            prepareLegacySource(source, legacyName);
            builder.legacyName(legacyName);
            return;
        }

        if (!Files.isDirectory(source.resolve("region"))) {
            throw new WorldOperationException(WorldOperationException.Reason.WORLD_NOT_FOUND).path(source);
        }

        prepareManagedSource(source, target);
    }

    private void ensureNotLoaded(final Path source) {
        if (source.equals(plugin.getServer().getLevelDirectory().toAbsolutePath().normalize()))
            throw new WorldOperationException(WorldOperationException.Reason.WORLD_DIRECTORY_LOADED).path(source);
        if (plugin.getServer().getWorlds().stream()
                .map(world -> world.getWorldPath().toAbsolutePath().normalize())
                .anyMatch(source::equals))
            throw new WorldOperationException(WorldOperationException.Reason.WORLD_DIRECTORY_LOADED).path(source);
        if (plugin.listLevels().map(path -> path.toAbsolutePath().normalize()).anyMatch(source::equals))
            throw new WorldOperationException(WorldOperationException.Reason.WORLD_PATH_EXISTS).path(source);
    }

    private void ensureTargetAvailable(final Path source, final Path target) {
        if (Files.exists(target) && !source.equals(target)) {
            throw new WorldOperationException(Files.isDirectory(target)
                    ? WorldOperationException.Reason.WORLD_PATH_EXISTS
                    : WorldOperationException.Reason.TARGET_PATH_IS_FILE
            ).path(target);
        }
    }

    private void prepareManagedSource(final Path source, final Path target) {
        if (!source.equals(target)) try {
            Files.createDirectories(target.getParent());
            if (isInServerRoot(source)) Files.move(source, target);
            else plugin.levelView().copyDirectory(source, target, null);
        } catch (final IOException e) {
            throw new WorldOperationException(WorldOperationException.Reason.INTERNAL_ERROR, e).path(target);
        }
    }

    private void prepareLegacySource(final Path source, final String legacyName) {
        final var target = plugin.getServer().getWorldContainer().toPath()
                .resolve(legacyName).toAbsolutePath().normalize();
        if (source.equals(target)) return;
        ensureTargetAvailable(source, target);

        try {
            Files.createDirectories(target.getParent());
            if (isInServerRoot(source)) Files.move(source, target);
            else plugin.levelView().copyDirectory(source, target, null);
        } catch (final IOException e) {
            throw new WorldOperationException(WorldOperationException.Reason.INTERNAL_ERROR, e).path(target);
        }
    }

    private boolean isInServerRoot(final Path source) {
        final var root = plugin.getServer().getWorldContainer().toPath().toAbsolutePath().normalize();
        return source.startsWith(root);
    }
}
