package net.thenextlvl.worlds.preset;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core.api.file.format.GsonFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;

public record PresetFile(JsonObject settings) {

    public static @Nullable PresetFile of(File file) {
        if (!file.exists()) return null;
        var gson = new GsonFile<JsonObject>(file, JsonObject.class, new Gson());
        if (!gson.getRoot().has("settings")) return new PresetFile(gson.getRoot());
        return new PresetFile(gson.getRoot().getAsJsonObject("settings"));
    }

    public static List<File> findPresets(File dataFolder) {
        File[] files = dataFolder.listFiles((file, name) -> name.endsWith(".json"));
        return files != null ? List.of(files) : Collections.emptyList();
    }
}
