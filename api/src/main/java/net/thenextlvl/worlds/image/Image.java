package net.thenextlvl.worlds.image;

import com.google.gson.GsonBuilder;
import core.file.format.GsonFile;
import core.io.IO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Image {
    private static final Map<UUID, Image> images = new HashMap<>();
    private final GsonFile<WorldImage> file;
    private final World world;

    private Image(World world, WorldImage image) {
        this(new GsonFile<>(
                IO.of(Bukkit.getWorldContainer(), image.name() + ".image"),
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

    public DeleteResult delete(boolean keepImage, boolean keepWorld, boolean schedule) {
        return schedule ? deleteOnShutdown(keepImage, keepWorld) : deleteImmediately(keepImage, keepWorld);
    }

    public DeleteResult deleteOnShutdown(boolean keepImage, boolean keepWorld) {
        if (keepWorld && (keepImage || !getFile().getIO().exists()))
            return DeleteResult.WORLD_DELETE_NOTHING;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!keepWorld) delete(getWorld().getWorldFolder());
            if (!keepImage) try {
                getFile().delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        return DeleteResult.WORLD_DELETE_SCHEDULED;
    }

    public DeleteResult deleteImmediately(boolean keepImage, boolean keepWorld) {
        if (getWorld().getKey().toString().equals("minecraft:overworld"))
            return DeleteResult.WORLD_DELETE_ILLEGAL;

        var fallback = Bukkit.getWorlds().get(0).getSpawnLocation();
        getWorld().getPlayers().forEach(player -> player.teleport(fallback));

        try {
            if (!keepImage && file.getIO().exists() && !file.delete())
                return DeleteResult.IMAGE_DELETE_FAILED;
        } catch (IOException e) {
            return DeleteResult.IMAGE_DELETE_FAILED;
        }

        if (keepImage && keepWorld)
            return Bukkit.unloadWorld(world, world.isAutoSave())
                    ? DeleteResult.WORLD_UNLOADED
                    : DeleteResult.WORLD_UNLOAD_FAILED;

        if (!Bukkit.unloadWorld(world, world.isAutoSave() && keepWorld))
            return DeleteResult.WORLD_UNLOAD_FAILED;

        if (!keepWorld && !delete(world.getWorldFolder()))
            return DeleteResult.WORLD_DELETE_FAILED;

        return DeleteResult.WORLD_DELETED;
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

    @Getter
    @RequiredArgsConstructor
    public enum DeleteResult {
        WORLD_DELETE_SCHEDULED("world.delete.scheduled"),
        WORLD_DELETE_ILLEGAL("world.delete.disallowed"),
        WORLD_DELETE_NOTHING("world.delete.nothing"),
        WORLD_DELETE_FAILED("world.delete.failed"),
        WORLD_DELETED("world.delete.success"),

        IMAGE_DELETE_FAILED("image.delete.failed"),

        WORLD_UNLOAD_FAILED("world.unload.failed"),
        WORLD_UNLOADED("world.unload.success");

        private final String message;
    }
}
