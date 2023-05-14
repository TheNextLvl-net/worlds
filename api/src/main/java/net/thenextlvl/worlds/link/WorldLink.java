package net.thenextlvl.worlds.link;

import core.api.file.format.GsonFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public record WorldLink(
        @Nullable String overworld,
        @Nullable String nether,
        @Nullable String end
) {
    public static @Nullable WorldLink of(File file) {
        return new GsonFile<WorldLink>(file, WorldLink.class).getRoot();
    }
}
