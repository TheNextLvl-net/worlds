package net.nonswag.tnl.world.api.generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import javax.annotation.Nonnull;
import java.util.Random;

public class BuildWorldGenerator extends ChunkGenerator {

    @Nonnull
    private final SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(), 8);

    @Nonnull
    public SimplexOctaveGenerator getGenerator() {
        return generator;
    }

    @Nonnull
    @Override
    public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int chunkX, int chunkZ, @Nonnull BiomeGrid biomeGrid) {
        ChunkData chunk = createChunkData(world);
        getGenerator().setScale(0.01D);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int currentHeight = (int) (getGenerator().noise(chunkX * 16 + x, chunkZ * 16 + z, 0.5D, 0.5D, true) * 15D + 70D);
                Biome biome = world.getBiome(x, currentHeight, z);
                if (currentHeight < 64) {
                    chunk.setBlock(x, currentHeight, z, Material.GRAVEL);
                    chunk.setBlock(x, currentHeight - 1, z, Material.GRAVEL);
                    chunk.setBlock(x, currentHeight - 2, z, Material.GRAVEL);
                } else {
                    if (biome.equals(Biome.DESERT)) {
                        chunk.setBlock(x, currentHeight, z, Material.SAND);
                        chunk.setBlock(x, currentHeight - 1, z, Material.SAND);
                        chunk.setBlock(x, currentHeight - 2, z, Material.SAND);
                        chunk.setBlock(x, currentHeight - 3, z, Material.SANDSTONE);
                        chunk.setBlock(x, currentHeight - 4, z, Material.SANDSTONE);
                    } else {
                        chunk.setBlock(x, currentHeight, z, Material.GRASS_BLOCK);
                        chunk.setBlock(x, currentHeight - 1, z, Material.DIRT);
                        chunk.setBlock(x, currentHeight - 2, z, Material.DIRT);
                    }
                }
                for (int i = currentHeight - 3; i > 0; i--) {
                    chunk.setBlock(x, i, z, Material.STONE);
                }
                for (int i = 64; i > 0; i--) {
                    if (chunk.getType(x, i, z).equals(Material.AIR)) {
                        chunk.setBlock(x, i, z, Material.WATER);
                    }
                }
                chunk.setBlock(x, 0, z, Material.BEDROCK);
            }
        }
        return chunk;
    }
}
