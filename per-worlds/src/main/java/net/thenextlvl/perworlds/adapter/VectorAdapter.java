package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class VectorAdapter implements TagAdapter<Vector> {
    @Override
    public Vector deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var root = tag.getAsCompound();
        var x = root.get("x").getAsDouble();
        var y = root.get("y").getAsDouble();
        var z = root.get("z").getAsDouble();
        return new Vector(x, y, z);
    }

    @Override
    public Tag serialize(Vector vector, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("x", vector.getX());
        tag.add("y", vector.getY());
        tag.add("z", vector.getZ());
        return tag;
    }
}
