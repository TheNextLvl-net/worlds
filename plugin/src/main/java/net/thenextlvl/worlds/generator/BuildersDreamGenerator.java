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

public class BuildersDreamGenerator {
    public static final String NAME = "builders dream";

    public static ChunkGenerator getWorldGenerator(String worldName) {
        return new ChunkGenerator() {
            @Override
            public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
                var filler = switch (worldInfo.getEnvironment()) {
                    case NETHER -> Material.NETHERRACK;
                    case THE_END -> Material.END_STONE;
                    default -> Material.STONE;
                };
                var top = switch (worldInfo.getEnvironment()) {
                    case NETHER, THE_END -> filler;
                    default -> Material.GRASS_BLOCK;
                };
                int minHeight = chunkData.getMinHeight();
                int maxHeight = chunkData.getMaxHeight() / 5;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        chunkData.setBlock(x, maxHeight, z, top);
                        for (int y = minHeight; y < maxHeight; y++) {
                            chunkData.setBlock(x, y, z, filler);
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

    public static BiomeProvider getBiomeProvider(String worldName) {
        return new BiomeProvider() {
            @NotNull
            @Override
            public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                return switch (worldInfo.getEnvironment()) {
                    case NETHER -> Biome.NETHER_WASTES;
                    case THE_END -> Biome.THE_END;
                    default -> Biome.PLAINS;
                };
            }

            @NotNull
            @Override
            public List<Biome> getBiomes(WorldInfo worldInfo) {
                return Collections.singletonList(switch (worldInfo.getEnvironment()) {
                    case NETHER -> Biome.NETHER_WASTES;
                    case THE_END -> Biome.THE_END;
                    default -> Biome.PLAINS;
                });
            }
        };
    }
}
