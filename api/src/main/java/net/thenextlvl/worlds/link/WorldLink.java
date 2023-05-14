package net.thenextlvl.worlds.link;

import core.api.file.format.GsonFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public record WorldLink(
        @Nullable String overworld,
        @Nullable String nether,
        @Nullable String end,
        @Nullable String custom
) {
    public static @Nullable WorldLink of(File file) {
        return new GsonFile<WorldLink>(file, WorldLink.class).getRoot();
    }

    public static WorldLink of(File file, WorldLink defaultValue) {
        return new GsonFile<>(file, defaultValue).getRoot();
    }

    public static WorldLink empty() {
        return new WorldLink(null, null, null, null);
    }
}
