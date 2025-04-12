package net.thenextlvl.perworlds.model.config;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupSettings;

import java.util.Set;

public record GroupConfig(Set<Key> worlds, GroupData data, GroupSettings settings) {
}
