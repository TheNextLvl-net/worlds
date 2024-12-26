package net.thenextlvl.worlds.api.preset;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core.file.format.GsonFile;
import core.io.IO;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

@NullMarked
public record PresetFile(JsonObject settings) {
    public static @Nullable PresetFile of(IO io) {
        if (!io.exists()) return null;
        var gson = new GsonFile<JsonObject>(io, JsonObject.class, new Gson());
        if (!gson.getRoot().has("settings")) return new PresetFile(gson.getRoot());
        return new PresetFile(gson.getRoot().getAsJsonObject("settings"));
    }

    public static List<File> findPresets(File dataFolder) {
        File[] files = dataFolder.listFiles((file, name) -> name.endsWith(".json"));
        return files != null ? List.of(files) : Collections.emptyList();
    }
}
