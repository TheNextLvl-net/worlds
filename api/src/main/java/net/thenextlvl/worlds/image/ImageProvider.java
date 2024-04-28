package net.thenextlvl.worlds.image;

import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface ImageProvider {

    @Nullable
    @Deprecated
    Image load(@Nullable WorldImage image);

    @Nullable
    Image get(World world);

    void register(Image image);

    Image getOrDefault(World world);

    List<File> findImageFiles();

    List<File> findWorldFiles();

    @Deprecated
    List<WorldImage> findImages();

    @Deprecated
    WorldImage createWorldImage();

    @Deprecated
    WorldImage of(World world);

    @Nullable
    @Deprecated
    WorldImage of(File file);
}
