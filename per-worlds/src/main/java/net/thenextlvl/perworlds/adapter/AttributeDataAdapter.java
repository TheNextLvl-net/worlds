package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.data.AttributeData;
import net.thenextlvl.perworlds.model.PaperAttributeData;
import org.bukkit.attribute.Attribute;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AttributeDataAdapter implements TagAdapter<AttributeData> {
    @Override
    public AttributeData deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var root = tag.getAsCompound();
        var attribute = context.deserialize(root.get("id"), Attribute.class);
        var value = root.get("base").getAsDouble();
        return new PaperAttributeData(attribute, value);
    }

    @Override
    public Tag serialize(AttributeData data, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("id", context.serialize(data.attribute()));
        tag.add("base", data.baseValue());
        return tag;
    }
}
