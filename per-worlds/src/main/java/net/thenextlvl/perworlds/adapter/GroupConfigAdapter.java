package net.thenextlvl.perworlds.adapter;

import core.nbt.serialization.ParserException;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.ListTag;
import core.nbt.tag.Tag;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.group.PaperGroupData;
import net.thenextlvl.perworlds.group.PaperGroupSettings;
import net.thenextlvl.perworlds.model.config.GroupConfig;
import org.bukkit.Server;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.stream.Collectors;

@NullMarked
public class GroupConfigAdapter implements TagAdapter<GroupConfig> {
    private final Server server;

    public GroupConfigAdapter(Server server) {
        this.server = server;
    }

    @Override
    public GroupConfig deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
        var root = tag.getAsCompound();
        var data = root.optional("data")
                .map(tag1 -> context.deserialize(tag1, GroupData.class))
                .orElseGet(() -> new PaperGroupData(server));
        var settings = root.optional("settings")
                .map(tag1 -> context.deserialize(tag1, GroupSettings.class))
                .orElseGet(PaperGroupSettings::new);
        var worlds = root.optional("worlds").map(Tag::getAsList)
                .map(tags -> tags.stream()
                        .map(world -> context.deserialize(world, Key.class))
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
        return new GroupConfig(worlds, data, settings);
    }

    @Override
    public Tag serialize(GroupConfig config, TagSerializationContext context) throws ParserException {
        var tag = new CompoundTag();
        tag.add("data", context.serialize(config.data()));
        tag.add("settings", context.serialize(config.settings()));
        if (!config.worlds().isEmpty()) tag.add("worlds", new ListTag<>(
                config.worlds().stream().map(context::serialize).toList()
        ));
        return tag;
    }
}
