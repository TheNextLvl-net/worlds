package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.StringTag;
import core.nbt.tag.Tag;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class NamespacedKeyAdapter implements TagAdapter<NamespacedKey> {
    @Override
    public NamespacedKey deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        return Objects.requireNonNull(NamespacedKey.fromString(tag.getAsString()), "Encountered invalid namespaced key: " + tag.getAsString());
    }

    @Override
    public Tag serialize(NamespacedKey key, TagSerializationContext context) throws ParserException {
        return new StringTag(key.asString());
    }
}
