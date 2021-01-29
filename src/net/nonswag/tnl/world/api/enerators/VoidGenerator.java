package net.nonswag.tnl.world.api.enerators;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoidGenerator extends ChunkGenerator {

    public List<BlockPopulator> getDefaultPopulators(World world) {
        return new ArrayList<>();
    }

    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid grid) {
        return createChunkData(world);
    }
}
