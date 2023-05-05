package net.thenextlvl.worlds.volume;

import core.api.file.format.GsonFile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

@Getter
public class Volume {
    private static final Map<UUID, Volume> volumes = new HashMap<>();
    public static final File DATA_FOLDER = new File("worlds");
    private final GsonFile<WorldImage> file;
    private final World world;

    public Volume(World world, @Nullable Generator generator) {
        this.world = world;
        var file = new File(DATA_FOLDER, world.getName() + ".json");
        this.file = new GsonFile<>(file, WorldImage.of(world, generator));
    }

    public Volume(World world) {
        this(world, null);
    }

    public Volume register() {
        volumes.put(getWorld().getUID(), this);
        return this;
    }

    public void save() {
        world.save();
        file.save();
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

    public static Volume load(File file) {
        var gson = new GsonFile<WorldImage>(file, WorldImage.class);
        return new Volume(gson.getRoot().build(), gson.getRoot().generator());
    }

    @Nullable
    public static Volume get(World world) {
        return volumes.get(world.getUID());
    }

    public static List<File> findVolumes() {
        var files = DATA_FOLDER.listFiles((file, name) -> file.isFile() && name.endsWith(".json"));
        return files != null ? Arrays.asList(files) : Collections.emptyList();
    }

    public static List<Volume> loadVolumes() {
        return findVolumes().stream().map(Volume::load).toList();
    }

    public enum DeleteResult {
        DELETE_FAILED, UNLOAD_FAILED, SUCCESS
    }
}
