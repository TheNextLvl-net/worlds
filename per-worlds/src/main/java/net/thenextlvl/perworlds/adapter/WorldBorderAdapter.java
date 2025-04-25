package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.data.WorldBorderData;
import net.thenextlvl.perworlds.model.PaperWorldBorderData;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldBorderAdapter implements TagAdapter<WorldBorderData> {
    @Override
    public WorldBorderData deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var data = new PaperWorldBorderData();
        var root = tag.getAsCompound();
        root.optional("x").map(Tag::getAsDouble).ifPresent(data::centerX);
        root.optional("z").map(Tag::getAsDouble).ifPresent(data::centerZ);
        root.optional("size").map(Tag::getAsDouble).ifPresent(data::size);
        root.optional("duration").map(Tag::getAsLong).ifPresent(data::duration);
        root.optional("damageAmount").map(Tag::getAsDouble).ifPresent(data::damageAmount);
        root.optional("damageBuffer").map(Tag::getAsDouble).ifPresent(data::damageBuffer);
        root.optional("warningDistance").map(Tag::getAsInt).ifPresent(data::warningDistance);
        root.optional("warningTime").map(Tag::getAsInt).ifPresent(data::warningTime);
        return data;
    }

    @Override
    public Tag serialize(WorldBorderData data, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("x", data.centerX());
        tag.add("z", data.centerZ());
        tag.add("size", data.size());
        tag.add("duration", data.duration());
        tag.add("damageAmount", data.damageAmount());
        tag.add("damageBuffer", data.damageBuffer());
        tag.add("warningDistance", data.warningDistance());
        tag.add("warningTime", data.warningTime());
        return tag;
    }
}
