package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Base64;

@NullMarked
public class ItemStackArrayAdapter implements TagAdapter<ItemStack[]> {
    @Override
    public ItemStack[] deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var bytes = Base64.getDecoder().decode(tag.getAsString());
        return ItemStack.deserializeItemsFromBytes(bytes);
    }

    @Override
    public Tag serialize(@Nullable ItemStack[] itemStacks, TagSerializationContext context) throws ParserException {
        var bytes = ItemStack.serializeItemsAsBytes(itemStacks);
        return new StringTag(Base64.getEncoder().encodeToString(bytes));
    }
}
