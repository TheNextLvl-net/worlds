package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.Base64;

@NullMarked
public class ItemStackAdapter implements TagAdapter<ItemStack> {
    @Override
    public ItemStack deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(tag.getAsString()));
    }

    @Override
    public Tag serialize(ItemStack itemStack, TagSerializationContext context) throws ParserException {
        return new StringTag(Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()));
    }
}
