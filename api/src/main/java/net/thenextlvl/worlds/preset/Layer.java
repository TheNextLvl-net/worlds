package net.thenextlvl.worlds.preset;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.bukkit.Material;
import org.bukkit.block.BlockType;

/**
 * Represents a layer in a Superflat world preset, consisting of a block type and a height.
 *
 * @param block  the namespaced key of the block type
 * @param height the number of blocks in this layer
 * @since 4.0.0
 */
public record Layer(Key block, int height) {
    /**
     * Creates a layer from a Bukkit {@link Material}.
     *
     * @param material the block material
     * @param height   the number of blocks in this layer
     * @since 4.0.0
     */
    public Layer(final Material material, final int height) {
        this(material.key(), height);
    }

    /**
     * Creates a layer from a {@link BlockType}.
     *
     * @param blockType the block type
     * @param height    the number of blocks in this layer
     * @since 4.0.0
     */
    public Layer(final BlockType blockType, final int height) {
        this(blockType.key(), height);
    }

    /**
     * Creates a layer from a namespaced key string.
     *
     * @param block  the namespaced key string of the block type
     * @param height the number of blocks in this layer
     * @since 4.0.0
     */
    public Layer(@KeyPattern final String block, final int height) {
        this(Key.key(block), height);
    }

    @Override
    public String toString() {
        return height() != 1 ? height() + "*" + block().asString() : block().asString();
    }
}
