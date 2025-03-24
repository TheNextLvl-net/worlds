package net.thenextlvl.perworlds.model;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupSettings;

import java.util.Set;

public record PersistedGroup(Set<Key> worlds, GroupSettings settings) {
}
