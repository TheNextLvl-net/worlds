package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import org.bukkit.Server;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldAdapter implements TagAdapter<World> {
    private final Server server;

    public WorldAdapter(Server server) {
        this.server = server;
    }

    @Override
    public World deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var namespace = context.deserialize(tag, Key.class);
        var world = server.getWorld(namespace);
        if (world != null) return world;
        throw new ParserException("World not found: " + namespace);
    }

    @Override
    public Tag serialize(World world, TagSerializationContext context) throws ParserException {
        return context.serialize(world.getKey());
    }
}
