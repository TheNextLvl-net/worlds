package net.thenextlvl.worlds.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import core.file.format.JsonFile;
import core.io.IO;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.preset.Preset;
import net.thenextlvl.worlds.command.suggestion.WorldPresetSuggestionProvider;

import java.io.File;

public class WorldPresetArgument extends WrappedArgumentType<String, Preset> {
    public WorldPresetArgument(WorldsPlugin plugin) {
        super(StringArgumentType.string(), (reader, type) -> {
            var file = new File(plugin.presetsFolder(), type + ".json");
            if (!file.exists()) throw new IllegalStateException("No preset found");
            var root = new JsonFile<>(IO.of(file), new JsonObject()).getRoot();
            if (root.isJsonObject()) return Preset.deserialize(root);
            throw new IllegalStateException("Not a valid preset");
        }, new WorldPresetSuggestionProvider(plugin));
    }
}
