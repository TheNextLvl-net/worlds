package net.nonswag.tnl.world.generators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class FlatGenerator extends CustomGenerator {

    @Nonnull
    private static final FlatGenerator instance = new FlatGenerator();

    private FlatGenerator() {
        super("FlatGenerator");
    }

    @Nonnull
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@Nonnull String name, @Nullable String id) {
        return new ChunkGenerator() {
            @Nonnull
            @Override
            public ChunkGenerator.ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int chunkX, int chunkZ, @Nonnull ChunkGenerator.BiomeGrid biome) {
                ChunkGenerator.ChunkData chunkData = createChunkData(world);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        chunkData.setBlock(x, 0, z, Material.BEDROCK);
                        for (int y = 1; y < 40; y++) chunkData.setBlock(x, y, z, Material.STONE);
                    }
                }
                return chunkData;
            }

            @Nonnull
            @Override
            public Location getFixedSpawnLocation(@Nonnull World world, @Nonnull Random random) {
                return new Location(world, 0.5, 40, 0.5, 0, 0);
            }
        };
    }

    @Nonnull
    public static FlatGenerator getInstance() {
        return instance;
    }
}
