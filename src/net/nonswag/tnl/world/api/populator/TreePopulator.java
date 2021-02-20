package net.nonswag.tnl.world.api.populator;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class TreePopulator extends BlockPopulator {

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        if (random.nextBoolean()) {
            int amount = random.nextInt(4) + 1;
            for (int i = 1; i < amount; i++) {
                int x = random.nextInt(15);
                int z = random.nextInt(15);
                for (int y = world.getMaxHeight() - 1; chunk.getBlock(x, y, z).getType() == Material.AIR; y--) {
                    world.generateTree(chunk.getBlock(x, y, z).getLocation(), TreeType.TREE);
                }
            }
        }
    }
}
