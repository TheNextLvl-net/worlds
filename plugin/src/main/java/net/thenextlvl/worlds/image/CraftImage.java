package net.thenextlvl.worlds.image;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class CraftImage {
    private final WorldsPlugin plugin;
    private final World world;

    public boolean canUnload() {
        return /*!Bukkit.isTickingWorlds() && */world.getPlayers().isEmpty();
    }

    public boolean isDeletable() {
        return !getWorld().getKey().toString().equals("minecraft:overworld");
    }

    public boolean unload() {
        return canUnload() && Bukkit.unloadWorld(world, world.isAutoSave());
    }

    public DeleteResult delete(boolean schedule) {
        return schedule ? scheduleDeletion() : deleteNow();
    }

    public DeleteResult deleteNow() {
        if (!isDeletable()) return DeleteResult.EXEMPTED;

        var fallback = Bukkit.getWorlds().getFirst().getSpawnLocation();
        getWorld().getPlayers().forEach(player -> player.teleport(fallback));

        if (!unload()) return DeleteResult.UNLOAD_FAILED;

        return delete(world.getWorldFolder())
                ? DeleteResult.SUCCESS
                : DeleteResult.FAILED;
    }

    public DeleteResult scheduleDeletion() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (delete(getWorld().getWorldFolder())) return;
            plugin.getComponentLogger().error("Failed to delete world {}", getWorld().getName());
        }));
        return DeleteResult.SCHEDULED;
    }

    private boolean delete(File file) {
        if (file.isFile()) return file.delete();
        var files = file.listFiles();
        if (files == null) return false;
        for (var file1 : files) delete(file1);
        return file.delete();
    }
}
