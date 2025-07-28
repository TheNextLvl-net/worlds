package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.LongTag;
import core.nbt.tag.Tag;
import org.jspecify.annotations.NullMarked;

import java.time.Instant;

@NullMarked
public class InstantAdapter implements TagAdapter<Instant> {
    @Override
    public Instant deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        return Instant.ofEpochMilli(tag.getAsLong());
    }

    @Override
    public Tag serialize(Instant instant, TagSerializationContext context) throws ParserException {
        return new LongTag(instant.toEpochMilli());
    }
}
