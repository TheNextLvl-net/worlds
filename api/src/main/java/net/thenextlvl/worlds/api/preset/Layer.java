package net.thenextlvl.worlds.api.preset;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.jspecify.annotations.NullMarked;

/**
 * @since 2.0.0
 */
@NullMarked
public record Layer(Key block, int height) {
    public Layer(Material material, int height) {
        this(material.key(), height);
    }

    public Layer(BlockType blockType, int height) {
        this(blockType.key(), height);
    }

    public Layer(@KeyPattern String block, int height) {
        this(Key.key(block), height);
    }

    @Override
    public String toString() {
        return height() != 1 ? height() + "*" + block().asString() : block().asString();
    }
}
