package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LocationAdapter implements TagAdapter<Location> {
    @Override
    public Location deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var root = tag.getAsCompound();
        var x = root.get("x").getAsDouble();
        var y = root.get("y").getAsDouble();
        var z = root.get("z").getAsDouble();
        var yaw = root.get("yaw").getAsFloat();
        var pitch = root.get("pitch").getAsFloat();
        var world = context.deserialize(root.get("world"), World.class);
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public Tag serialize(Location location, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("x", location.getX());
        tag.add("y", location.getY());
        tag.add("z", location.getZ());
        tag.add("yaw", location.getYaw());
        tag.add("pitch", location.getPitch());
        tag.add("world", context.serialize(location.getWorld()));
        return tag;
    }
}
