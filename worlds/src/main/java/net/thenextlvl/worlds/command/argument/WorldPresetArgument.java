package net.thenextlvl.worlds.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import core.file.format.JsonFile;
import core.io.IO;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.suggestion.WorldPresetSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Files;

@NullMarked
public class WorldPresetArgument extends WrappedArgumentType<String, Preset> {
    public WorldPresetArgument(WorldsPlugin plugin) {
        super(StringArgumentType.string(), (reader, type) -> {
            var file = plugin.presetsFolder().resolve(type + ".json");
            if (Files.isRegularFile(file)) throw new IllegalStateException("No preset found");
            var root = new JsonFile<>(IO.of(file), new JsonObject()).getRoot();
            if (root.isJsonObject()) return Preset.deserialize(root);
            throw new IllegalStateException("Not a valid preset");
        }, new WorldPresetSuggestionProvider(plugin));
    }
}
