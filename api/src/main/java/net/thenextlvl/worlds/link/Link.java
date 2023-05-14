package net.thenextlvl.worlds.link;

import com.google.gson.GsonBuilder;
import core.api.file.format.GsonFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Link {
    private static final List<Link> links = new ArrayList<>();
    private final GsonFile<WorldLink> file;
    private final UUID uniqueId;

    private Link(WorldLink link, UUID uuid) {
        this(new GsonFile<>(new File(Bukkit.getWorldContainer(), uuid + ".link"), link) {
            @Override
            public GsonBuilder load(GsonBuilder builder) {
                return new GsonBuilder().setPrettyPrinting();
            }
        }, uuid);
    }

    private Link register() {
        if (!links.contains(this)) links.add(this);
        return this;
    }

    public Link save() {
        file.save();
        return this;
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
