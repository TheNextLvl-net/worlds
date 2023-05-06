package net.thenextlvl.worlds.volume;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record WorldImage(
        String name,
        @Nullable Generator generator,
        World.Environment environment,
        WorldType type,
        boolean generateStructures,
        boolean hardcore,
        long seed
) {

    @Nullable
    public World build() {
        var creator = WorldCreator.name(name()).type(type()).seed(seed())
                .hardcore(hardcore()).environment(environment())
                .generateStructures(generateStructures());
        if (generator != null) creator.generator(generator.toString());
        return creator.createWorld();
    }

    @SuppressWarnings("deprecation")
    public static WorldImage of(World world, @Nullable Generator generator) {
        return new WorldImage(
                world.getName(),
                generator,
                world.getEnvironment(),
                Objects.requireNonNullElse(world.getWorldType(), WorldType.NORMAL),
                world.canGenerateStructures(),
                world.isHardcore(),
                world.getSeed()
        );
    }
}
