package net.thenextlvl.worlds.link;

import com.google.gson.reflect.TypeToken;
import core.api.file.format.GsonFile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkFile {
    private final GsonFile<Map<String, Link>> file;

    public LinkFile(File file) {
        this(new GsonFile<>(file, new HashMap<>(), new TypeToken<>() {
        }));
    }

    public Map<String, Link> links() {
        return file.getRoot();
    }

    public void save() {
        file.save();
    }
}
