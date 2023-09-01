package net.thenextlvl.worlds.image;

import com.google.gson.GsonBuilder;
import core.api.file.format.GsonFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Image {
    private static final Map<UUID, Image> images = new HashMap<>();
    private final GsonFile<WorldImage> file;
    private final World world;

    private Image(World world, WorldImage image) {
        this(new GsonFile<>(
                new File(Bukkit.getWorldContainer(), image.name() + ".image"),
                image, new GsonBuilder().setPrettyPrinting().create()
        ), world);
    }

    private Image(World world) {
        this(world, WorldImage.of(world));
    }

    private Image register() {
        images.put(getWorld().getUID(), this);
        return this;
    }

    public Image save() {
        file.save();
        return this;
    }

    public WorldImage getWorldImage() {
        return getFile().getRoot();
    }

    public boolean canUnload() {
        return !Bukkit.isTickingWorlds() && world.getPlayers().isEmpty();
    }

    public boolean unload() {
        return canUnload() && Bukkit.unloadWorld(world, world.isAutoSave());
    }

    public DeleteResult delete(boolean keepImage, boolean keepWorld) {
        if (getWorld().getKey().toString().equals("minecraft:overworld"))
            return DeleteResult.DELETE_NOT_ALLOWED;
        if (!Bukkit.unloadWorld(world, world.isAutoSave() && keepWorld))
            return DeleteResult.UNLOAD_FAILED;
        if (!keepWorld && !delete(world.getWorldFolder()))
            return DeleteResult.WORLD_DELETE_FAILED;
        if (keepImage || !file.getFile().exists() || file.delete())
            return DeleteResult.SUCCESS;
        return DeleteResult.IMAGE_DELETE_FAILED;
    }

    private boolean delete(File file) {
        if (file.isFile()) return file.delete();
        var files = file.listFiles();
        if (files == null) return false;
        for (var file1 : files) delete(file1);
        return file.delete();
    }

    @Nullable
    public static Image load(@Nullable WorldImage image) {
        if (image == null) return null;
        if (Bukkit.getWorld(image.name()) != null) return null;
        var build = image.build();
        return build != null ? new Image(build, image).save().register() : null;
    }

    @Nullable
    public static Image get(World world) {
        return images.get(world.getUID());
    }

    public static Image getOrCreate(World world) {
        if (!images.containsKey(world.getUID()))
            return new Image(world).save().register();
        return images.get(world.getUID());
    }

    public static Image getOrDefault(World world) {
        return images.getOrDefault(world.getUID(), new Image(world));
    }

    public static List<File> findWorlds() {
        var files = Bukkit.getWorldContainer().listFiles(file ->
                file.isDirectory() && new File(file, "level.dat").isFile());
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    public static List<File> findImageFiles() {
        var files = Bukkit.getWorldContainer().listFiles(file ->
                file.isFile() && file.getName().endsWith(".image"));
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    public static List<WorldImage> findImages() {
        return findImageFiles().stream()
                .map(WorldImage::of)
                .filter(Objects::nonNull)
                .toList();
    }

    public enum DeleteResult {
        DELETE_NOT_ALLOWED, WORLD_DELETE_FAILED, IMAGE_DELETE_FAILED, UNLOAD_FAILED, SUCCESS
    }
}
