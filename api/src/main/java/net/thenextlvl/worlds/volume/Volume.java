package net.thenextlvl.worlds.volume;

import core.api.file.format.GsonFile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Volume {
    private static final Map<UUID, Volume> volumes = new HashMap<>();
    private final GsonFile<WorldImage> file;
    private final World world;

    public Volume(World world, WorldImage image) {
        this.world = world;
        var file = new File(world.getWorldFolder(), ".volume");
        this.file = new GsonFile<>(file, image);
    }

    public Volume(World world, @Nullable Generator generator) {
        this(world, WorldImage.of(world, generator));
    }

    public Volume(World world) {
        this(world, WorldImage.of(world));
    }

    public Location getSpawnLocation() {
        var random = ThreadLocalRandom.current();
        var generator = getWorld().getGenerator();
        var location = generator != null ? generator.getFixedSpawnLocation(world, random) : null;
        return location != null ? location : world.getSpawnLocation().clone().add(0.5, 0, 0.5);
    }

    public Volume register() {
        volumes.put(getWorld().getUID(), this);
        return this;
    }

    public Volume save() {
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
        return canUnload() && Bukkit.unloadWorld(world, true);
    }

    public boolean forceUnload() {
        return Bukkit.unloadWorld(world, false);
    }

    public DeleteResult delete() {
        if (!forceUnload()) return DeleteResult.UNLOAD_FAILED;
        if (!delete(world.getWorldFolder())) return DeleteResult.DELETE_FAILED;
        if (file.getFile().exists() && !file.delete()) return DeleteResult.DELETE_FAILED;
        return DeleteResult.SUCCESS;
    }

    private boolean delete(File file) {
        if (file.isFile()) return file.delete();
        var files = file.listFiles();
        if (files == null) return false;
        for (var file1 : files) delete(file1);
        return file.delete();
    }

    @Nullable
    public static Volume load(File file) {
        var gson = new GsonFile<WorldImage>(file, WorldImage.class);
        var world = gson.getRoot().build();
        return world != null ? new Volume(world, gson.getRoot()) : null;
    }

    @Nullable
    public static Volume get(World world) {
        return volumes.get(world.getUID());
    }

    public static Volume getOrCreate(World world) {
        if (!volumes.containsKey(world.getUID()))
            volumes.put(world.getUID(), new Volume(world, Generator.of(world)));
        return volumes.get(world.getUID());
    }

    public static List<File> findWorlds() {
        var files = Bukkit.getWorldContainer().listFiles(file ->
                file.isDirectory() && new File(file, "level.dat").isFile());
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    public static List<File> findVolumes() {
        return findWorlds().stream()
                .map(file -> new File(file, ".volume"))
                .filter(File::isFile).toList();
    }

    public static List<Volume> loadVolumes() {
        return findVolumes().stream().map(Volume::load).toList();
    }

    public enum DeleteResult {
        DELETE_FAILED, UNLOAD_FAILED, SUCCESS
    }
}
