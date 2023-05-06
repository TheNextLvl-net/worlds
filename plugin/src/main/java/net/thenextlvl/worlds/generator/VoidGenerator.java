package net.thenextlvl.worlds.generator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VoidGenerator {
    public static final String NAME = "void";

    public static ChunkGenerator getWorldGenerator(String worldName) {
        return new ChunkGenerator() {
            @Override
            public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                if (chunkX == 0 && chunkZ == 0) chunkData.setBlock(0, 64, 0, Material.BEDROCK);
            }

            @Override
            public boolean shouldGenerateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
                return chunkX == 0 && chunkZ == 0;
            }

            @Override
            public Location getFixedSpawnLocation(World world, Random random) {
                return new Location(world, 0.5, 65, 0.5, 0, 0);
            }
        };
    }

    public static BiomeProvider getBiomeProvider(String worldName) {
        return new BiomeProvider() {
            @NotNull
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return Biome.THE_VOID;
            }

            @NotNull
            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return Collections.singletonList(Biome.THE_VOID);
            }
        };
    }
}
