package net.thenextlvl.worlds.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

public interface Image {

    Image save();

    World getWorld();

    @Deprecated
    WorldImage getWorldImage();

    boolean unload();

    boolean canUnload();

    boolean canDelete();

    DeleteResult delete(boolean keepImage, boolean keepWorld, boolean schedule);

    DeleteResult deleteNow(boolean keepImage, boolean keepWorld);

    DeleteResult scheduleDeletion(boolean keepImage, boolean keepWorld);

    @Getter
    @RequiredArgsConstructor
    enum DeleteResult {
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
