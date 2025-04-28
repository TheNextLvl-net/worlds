package net.thenextlvl.perworlds.model.config;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupSettings;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

@NullMarked
public record GroupConfig(Set<Key> worlds, GroupData data, GroupSettings settings) {
}
