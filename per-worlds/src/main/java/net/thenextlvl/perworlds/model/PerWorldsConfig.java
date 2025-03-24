package net.thenextlvl.perworlds.model;

import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.GroupSettings;

import java.util.HashMap;

public record PerWorldsConfig(
        HashMap<Key, GroupSettings> settings
) {
}
