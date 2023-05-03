package worlds.generator;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class SuperFlatGenerator extends CustomGenerator {
    @Getter
    private static final SuperFlatGenerator instance = new SuperFlatGenerator();

    private SuperFlatGenerator() {
        super("super-flat");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String name, @Nullable String id) {
        return new ChunkGenerator() {
            @Nonnull
            @Override
            public ChunkGenerator.ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.BiomeGrid biome) {
                ChunkGenerator.ChunkData chunkData = createChunkData(world);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        chunkData.setBlock(x, 0, z, Material.BEDROCK);
                        for (int y = 1; y < 40; y++) chunkData.setBlock(x, y, z, Material.STONE);
                    }
                }
                return chunkData;
            }

            @Override
            public Location getFixedSpawnLocation(World world, Random random) {
                return new Location(world, 0.5, 40, 0.5, 0, 0);
            }
        };
    }
}
