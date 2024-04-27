package net.thenextlvl.worlds.image;

import core.file.FileIO;
import core.io.IO;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class CraftImage implements Image {
    private final FileIO<WorldImage> file;
    private final Worlds plugin;
    private final World world;

    CraftImage(Worlds plugin, World world, WorldImage image) {
        this(CraftImageProvider.loadFile(IO.of(Bukkit.getWorldContainer(), image.name() + ".image"), image), plugin, world);
    }

    CraftImage(Worlds plugin, World world) {
        this(plugin, world, plugin.imageProvider().of(world));
    }

    @Override
    public CraftImage save() {
        file.save();
        return this;
    }

    @Override
    public WorldImage getWorldImage() {
        return getFile().getRoot();
    }

    @Override
    public boolean canUnload() {
        return !Bukkit.isTickingWorlds() && world.getPlayers().isEmpty();
    }

    @Override
    public boolean canDelete() {
        return getWorld().getKey().toString().equals("minecraft:overworld");
    }

    @Override
    public boolean unload() {
        return canUnload() && Bukkit.unloadWorld(world, world.isAutoSave());
    }

    @Override
    public DeleteResult delete(boolean keepImage, boolean keepWorld, boolean schedule) {
        return schedule ? scheduleDeletion(keepImage, keepWorld) : deleteNow(keepImage, keepWorld);
    }

    @Override
    public DeleteResult deleteNow(boolean keepImage, boolean keepWorld) {
        if (canDelete()) return DeleteResult.WORLD_DELETE_ILLEGAL;

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

    @Override
    public DeleteResult scheduleDeletion(boolean keepImage, boolean keepWorld) {
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

    private boolean delete(File file) {
        if (file.isFile()) return file.delete();
        var files = file.listFiles();
        if (files == null) return false;
        for (var file1 : files) delete(file1);
        return file.delete();
    }
}
