package net.thenextlvl.perworlds.model;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupSettings;

import java.util.Map;

public record PerWorldsConfig(
        Map<Key, GroupSettings> settings
) {
}
