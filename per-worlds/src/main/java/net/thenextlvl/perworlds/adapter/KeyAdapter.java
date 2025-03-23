package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class KeyAdapter implements TagAdapter<Key> {
    @Override
    @SuppressWarnings("PatternValidation")
    public Key deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var split = tag.getAsString().split(":", 2);
        return Key.key(split[0], split[1]);
    }

    @Override
    public Tag serialize(Key key, TagSerializationContext context) throws ParserException {
        return new StringTag(key.asString());
    }
}
