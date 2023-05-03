package worlds.generator;

import lombok.Getter;
import net.thenextlvl.worlds.generator.WorldGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import worlds.Worlds;

import java.util.Random;

public class VoidGenerator extends WorldGenerator {
    @Getter
    private static final VoidGenerator instance = new VoidGenerator();

    private VoidGenerator() {
        super(JavaPlugin.getPlugin(Worlds.class), "the void");
    }

    @Override
    public ChunkGenerator getWorldGenerator(String worldName, @Nullable String id) {
        return new ChunkGenerator() {
            @Override
            public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                if (chunkX == 0 && chunkZ == 0) chunkData.setBlock(0, 63, 0, Material.BEDROCK);
            }

            @Override
            public boolean shouldGenerateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ) {
                return chunkX == 0 && chunkZ == 0;
            }

            @Override
            public Location getFixedSpawnLocation(World world, Random random) {
                return new Location(world, 0.5, 64, 0.5, 0, 0);
            }
        };
    }
}
