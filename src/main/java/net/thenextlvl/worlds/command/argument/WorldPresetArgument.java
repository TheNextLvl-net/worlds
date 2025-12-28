package net.thenextlvl.worlds.command.argument;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.api.preset.Presets;
import org.jspecify.annotations.NullMarked;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@NullMarked
public final class WorldPresetArgument implements SimpleArgumentType<Preset, String> {
    private static final Map<String, Preset> identifiers = Presets.presets().stream()
            .filter(preset -> preset.name() != null).collect(Collectors.toMap(
                    preset -> toIdentifier(Objects.requireNonNull(preset.name(), "Preset name cannot be null")),
                    preset -> preset
            ));

    private final WorldsPlugin plugin;

    public WorldPresetArgument(final WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Preset convert(final StringReader reader, final String type) {
        final var optional = identifiers.get(toIdentifier(type));
        if (optional != null) return optional;

        final var file = plugin.presetsFolder().resolve(type + ".json");
        if (!Files.isRegularFile(file)) throw new IllegalStateException("No preset found");


        try (final var jsonReader = new JsonReader(new InputStreamReader(new BufferedInputStream(
                Files.newInputStream(file)
        ), StandardCharsets.UTF_8))) {
            final var root = JsonParser.parseReader(jsonReader);
            if (root.isJsonObject()) return Preset.deserialize(root.getAsJsonObject());
            throw new IllegalStateException("Not a valid preset");
        } catch (final IOException e) {
            throw new IllegalStateException("Not a valid preset", e);
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return CompletableFuture.runAsync(() -> {
            final var presets = new HashSet<>(identifiers.keySet());

            try (final var directoryStream = Files.newDirectoryStream(plugin.presetsFolder(), "*.json")) {
                for (final var path : directoryStream) {
                    final var fileName = path.getFileName().toString();
                    final var suggestion = fileName.substring(0, fileName.length() - 5);
                    presets.add(suggestion);
                }
            } catch (final IOException e) {
                plugin.getComponentLogger().warn("Failed to read presets from disk", e);
            }
            presets.stream().filter(s -> s.toLowerCase(Locale.ROOT).contains(builder.getRemainingLowerCase()))
                    .map(StringArgumentType::escapeIfRequired)
                    .forEach(builder::suggest);
        }).exceptionally(throwable -> {
            plugin.getComponentLogger().error("Failed to list presets", throwable);
            return null;
        }).thenApply(ignored -> builder.build());
    }

    private static String toIdentifier(final String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "-").replace("'", "");
    }
}
