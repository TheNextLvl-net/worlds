package worlds.generator;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class SimplexOctaveGenerator extends CustomGenerator {

    @Getter
    private static final SimplexOctaveGenerator instance = new SimplexOctaveGenerator();

    private SimplexOctaveGenerator() {
        super("simplex-octave");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String name, @Nullable String id) {
        return new ChunkGenerator() {

            int currentHeight = 64;

            @Override
            public boolean shouldGenerateCaves() {
                return true;
            }

            @Override
            public boolean shouldGenerateDecorations() {
                return true;
            }

            @Nonnull
            @Override
            @Deprecated
            public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
                org.bukkit.util.noise.SimplexOctaveGenerator generator = new org.bukkit.util.noise.SimplexOctaveGenerator(world, 8);
                ChunkData chunk = createChunkData(world);
                generator.setScale(0.005D);
                World.Environment environment = world.getEnvironment();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        currentHeight = (int) (generator.noise(chunkX * 16 + x, chunkZ * 16 + z, 0.00001, 0.00001) * 3 + 64);
                        if (environment.equals(World.Environment.NORMAL)) {
                            chunk.setBlock(x, currentHeight, z, Material.GRASS_BLOCK);
                            chunk.setBlock(x, currentHeight - 1, z, Material.DIRT);
                            for (int i = currentHeight - 2; i > 0; i--) chunk.setBlock(x, i, z, Material.STONE);
                        } else if (environment.equals(World.Environment.THE_END)) {
                            for (int i = currentHeight; i > 0; i--) chunk.setBlock(x, i, z, Material.END_STONE);
                        } else if (environment.equals(World.Environment.NETHER)) {
                            for (int i = currentHeight; i > 0; i--) chunk.setBlock(x, i, z, Material.NETHERRACK);
                        }
                        chunk.setBlock(x, 0, z, Material.BEDROCK);
                    }
                }
                return chunk;
            }

            @Override
            public Location getFixedSpawnLocation(World world, Random random) {
                return new Location(world, 0.5, 70, 0.5, 0, 0);
            }
        };
    }
}
