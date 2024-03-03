package net.thenextlvl.worlds.link;

import com.google.gson.reflect.TypeToken;
import core.file.format.GsonFile;
import core.io.IO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkFile {
    private final GsonFile<Set<Link>> file;

    public LinkFile(IO io) {
        this(new GsonFile<>(io, new HashSet<>(), new TypeToken<>() {
        }));
    }

    public Set<Link> links() {
        return file.getRoot();
    }

    public void save() {
        file.save();
    }
}
