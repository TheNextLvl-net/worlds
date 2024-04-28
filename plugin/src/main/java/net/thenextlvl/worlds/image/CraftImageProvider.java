package net.thenextlvl.worlds.image;

import com.google.gson.GsonBuilder;
import core.file.FileIO;
import core.file.format.GsonFile;
import core.io.IO;
import core.paper.adapters.key.KeyAdapter;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

@RequiredArgsConstructor
public class CraftImageProvider implements ImageProvider {
    private static final Map<UUID, Image> images = new HashMap<>();
    private final Worlds plugin;

    @Override
    public @Nullable Image load(@Nullable WorldImage image) {
        if (image == null || Bukkit.getWorld(image.name()) != null) return null;
        var build = image.build();
        if (build == null) return null;
        var saved = new CraftImage(plugin, build, image).save();
        register(saved);
        return saved;
    }

    @Override
    public @Nullable Image get(World world) {
        return images.get(world.getUID());
    }

    @Override
    public void register(Image image) {
        images.put(image.getWorld().getUID(), image);
    }

    @Override
    public Image getOrDefault(World world) {
        return images.getOrDefault(world.getUID(), new CraftImage(plugin, world));
    }

    @Override
    public List<File> findImageFiles() {
        var files = Bukkit.getWorldContainer().listFiles(file ->
                file.isFile() && file.getName().endsWith(".image"));
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    @Override
    public List<File> findWorldFiles() {
        var files = Bukkit.getWorldContainer().listFiles(file ->
                file.isDirectory() && new File(file, "level.dat").isFile());
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    @Override
    public List<WorldImage> findImages() {
        return findImageFiles().stream()
                .map(this::of)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public WorldImage createWorldImage() {
        return new CraftWorldImage(true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public CraftWorldImage of(World world) {
        return new CraftWorldImage(
                world.getName(),
                world.getKey(),
                null, null, null,
                world.getEnvironment(),
                Objects.requireNonNullElse(world.getWorldType(), WorldType.NORMAL),
                world.isAutoSave(),
                world.canGenerateStructures(),
                world.isHardcore(),
                true,
                world.getSeed(),
                true
        );
    }

    @Override
    public @Nullable WorldImage of(File file) {
        return file.isFile() ? loadFile(IO.of(file), null).getRoot() : null;
    }

    public static FileIO<WorldImage> loadFile(IO file, @Nullable WorldImage root) {
        var gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(NamespacedKey.class, KeyAdapter.Bukkit.INSTANCE)
                .setPrettyPrinting()
                .create();
        return root != null ? new GsonFile<>(file, root, gson) : new GsonFile<>(file, CraftWorldImage.class, gson);
    }
}
