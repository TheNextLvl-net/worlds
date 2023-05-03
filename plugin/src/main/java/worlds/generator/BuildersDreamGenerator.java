package worlds.generator;

import lombok.Getter;
import net.thenextlvl.worlds.generator.WorldGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import worlds.Worlds;

import java.util.Random;

public class BuildersDreamGenerator extends WorldGenerator {
    @Getter
    private static final BuildersDreamGenerator instance = new BuildersDreamGenerator();

    private BuildersDreamGenerator() {
        super(JavaPlugin.getPlugin(Worlds.class), "builders dream");
    }

    @Override
    public ChunkGenerator getWorldGenerator(String worldName, @Nullable String id) {
        return new ChunkGenerator() {
            @Override
            public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                int minHeight = chunkData.getMinHeight();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        chunkData.setBlock(x, chunkData.getMaxHeight() - minHeight * 2, z, Material.GRASS_BLOCK);
                        for (int y = minHeight; y < chunkData.getMaxHeight() - minHeight * 2; y++) {
                            chunkData.setBlock(x, y, z, Material.STONE);
                        }
                    }
                }
            }

            @Override
            public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        chunkData.setBlock(x, worldInfo.getMinHeight(), z, Material.BEDROCK);
                    }
                }
            }

            @Override
            public Location getFixedSpawnLocation(World world, Random random) {
                return new Location(world, 0.5, world.getHighestBlockYAt(0, 0) + 1, 0.5, 0, 0);
            }

            @Override
            public boolean shouldGenerateSurface() {
                return true;
            }
        };
    }
}
