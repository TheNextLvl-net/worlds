package net.nonswag.tnl.world.generators;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class VoidGenerator extends CustomGenerator {

    @Getter
    private static final VoidGenerator instance = new VoidGenerator();

    private VoidGenerator() {
        super("void");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String name, @Nullable String id) {
        return new ChunkGenerator() {
            @Nonnull
            @Override
            public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
                ChunkData chunkData = createChunkData(world);
                if (chunkX == 0 && chunkZ == 0) chunkData.setBlock(0, 63, 0, Material.BEDROCK);
                return chunkData;
            }

            @Override
            public Location getFixedSpawnLocation(World world, Random random) {
                return new Location(world, 0.5, 64, 0.5, 0, 0);
            }
        };
    }
}
