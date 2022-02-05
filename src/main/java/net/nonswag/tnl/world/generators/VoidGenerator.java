package net.nonswag.tnl.world.generators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class VoidGenerator extends CustomGenerator {

    @Nonnull
    private static final VoidGenerator instance = new VoidGenerator();

    private VoidGenerator() {
        super("VoidGenerator");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@Nonnull String name, @Nullable String id) {
        return new ChunkGenerator() {
            @Nonnull
            @Override
            public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int x, int z, @Nonnull BiomeGrid biome) {
                ChunkData chunkData = createChunkData(world);
                if (x == 0 && z == 0) chunkData.setBlock(0, 99, 0, Material.BEDROCK);
                return chunkData;
            }

            @Nonnull
            @Override
            public Location getFixedSpawnLocation(@Nonnull World world, @Nonnull Random random) {
                return new Location(world, 0.5, 100, 0.5, 0, 0);
            }
        };
    }

    @Nonnull
    public static VoidGenerator getInstance() {
        return instance;
    }
}
