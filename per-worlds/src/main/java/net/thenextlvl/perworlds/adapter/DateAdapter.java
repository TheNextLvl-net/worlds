package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.LongTag;
import core.nbt.tag.Tag;
import org.jspecify.annotations.NullMarked;

import java.util.Date;

@NullMarked
public class DateAdapter implements TagAdapter<Date> {
    @Override
    public Date deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        return new Date(tag.getAsLong());
    }

    @Override
    public Tag serialize(Date date, TagSerializationContext context) throws ParserException {
        return new LongTag(date.getTime());
    }
}
