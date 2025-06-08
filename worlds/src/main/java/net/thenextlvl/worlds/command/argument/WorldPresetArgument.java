package net.thenextlvl.worlds.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import core.file.format.JsonFile;
import core.io.IO;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.api.preset.Presets;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NullMarked
public class WorldPresetArgument extends WrappedArgumentType<String, Preset> {
    private static final Map<String, Preset> identifiers = Presets.presets().stream()
            .filter(preset -> preset.name() != null).collect(Collectors.toMap(
                    preset -> toIdentifier(Objects.requireNonNull(preset.name(), "Preset name cannot be null")),
                    preset -> preset
            ));

    private static String toIdentifier(String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "-").replace("'", "");
    }

    public WorldPresetArgument(WorldsPlugin plugin) {
        super(StringArgumentType.string(), (reader, type) -> {
            var optional = identifiers.get(toIdentifier(type));
            if (optional != null) return optional;

            var file = plugin.presetsFolder().resolve(type + ".json");
            if (!Files.isRegularFile(file)) throw new IllegalStateException("No preset found");

            var root = new JsonFile<>(IO.of(file), new JsonObject()).getRoot();
            if (root.isJsonObject()) return Preset.deserialize(root);
            throw new IllegalStateException("Not a valid preset");

        }, (context, builder) -> CompletableFuture.runAsync(() -> {
            var presets = new HashSet<>(identifiers.keySet());

            try (var directoryStream = Files.newDirectoryStream(plugin.presetsFolder(), "*.json")) {
                for (var path : directoryStream) {
                    var fileName = path.getFileName().toString();
                    var suggestion = fileName.substring(0, fileName.length() - 5);
                    presets.add(suggestion);
                }
            } catch (IOException e) {
                plugin.getComponentLogger().warn("Failed to read presets from disk", e);
            }
            presets.stream().filter(s -> s.toLowerCase(Locale.ROOT).contains(builder.getRemainingLowerCase()))
                    .map(StringArgumentType::escapeIfRequired)
                    .forEach(builder::suggest);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to list presets", throwable);
            return null;
        }).thenApply(unused -> builder.build()));
    }
}
