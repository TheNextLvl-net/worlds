package net.thenextlvl.worlds.link;

import core.api.file.FileIO;
import core.api.file.format.GsonFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Link {
    private static final Map<String, Link> links = new HashMap<>();
    private final GsonFile<WorldLink> file;
    private final String id;

    private Link(WorldLink link, String id) {
        this(new GsonFile<>(new File(Bukkit.getWorldContainer(), id + ".link"), link), id);
    }

    public Link register() {
        links.put(id, this);
        return this;
    }

    public Link save() {
        file.save();
        return this;
    }

    public static Link getOrCreate(String id) {
        if (links.containsKey(id)) return links.get(id);
        return new Link(WorldLink.empty(), id).register();
    }

    public static Link of(File file) {
        var id = file.getName().substring(0, file.getName().length() - 5);
        return new Link(WorldLink.of(file, WorldLink.empty()), id);
    }

    public static Stream<WorldLink> links() {
        return links.values().stream().map(Link::getFile).map(FileIO::getRoot);
    }

    public static List<File> findLinkFiles() {
        var files = Bukkit.getWorldContainer().listFiles(file ->
                file.isFile() && file.getName().endsWith(".link"));
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    public static List<WorldLink> findLinks() {
        return findLinkFiles().stream()
                .map(WorldLink::of)
                .toList();
    }
}
