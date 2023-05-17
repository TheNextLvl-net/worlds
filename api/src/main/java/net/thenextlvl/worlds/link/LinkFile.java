package net.thenextlvl.worlds.link;

import com.google.gson.reflect.TypeToken;
import core.api.file.format.GsonFile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkFile {
    private final GsonFile<Set<Link>> file;

    public LinkFile(File file) {
        this(new GsonFile<>(file, new HashSet<>(), new TypeToken<>() {
        }));
    }

    public Set<Link> links() {
        return file.getRoot();
    }

    public void save() {
        file.save();
    }
}
