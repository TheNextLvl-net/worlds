package net.nonswag.tnl.world.api.generator;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoidGenerator extends ChunkGenerator {

    @Override
    @Nonnull
    public List<BlockPopulator> getDefaultPopulators(@Nonnull World world) {
        return new ArrayList<>();
    }

    @Override
    public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random seed, int x, int z, @Nonnull BiomeGrid biome) {
        return super.generateChunkData(world, seed, x, z, biome);
    }
}
