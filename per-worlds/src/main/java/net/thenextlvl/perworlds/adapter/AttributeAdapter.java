package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AttributeAdapter implements TagAdapter<Attribute> {
    @Override
    public Attribute deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        return Registry.ATTRIBUTE.getOrThrow(context.deserialize(tag, Key.class));
    }

    @Override
    public Tag serialize(Attribute attribute, TagSerializationContext context) throws ParserException {
        return context.serialize(attribute.key());
    }
}
