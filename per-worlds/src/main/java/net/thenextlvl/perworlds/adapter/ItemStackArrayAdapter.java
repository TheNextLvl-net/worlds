package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.ListTag;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

@NullMarked
public class ItemStackArrayAdapter implements TagAdapter<ItemStack[]> {
    @Override
    public ItemStack[] deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        return tag.getAsList().stream().map(item -> context.deserialize(item, ItemStack.class)).toArray(ItemStack[]::new);
    }

    @Override
    public Tag serialize(@Nullable ItemStack[] itemStacks, TagSerializationContext context) throws ParserException {
        var tag = new ListTag<>(StringTag.ID);
        Arrays.stream(itemStacks).map(itemStack -> itemStack != null ? itemStack : ItemStack.of(Material.AIR))
                .map(context::serialize).forEach(tag::add);
        return tag;
    }
}
