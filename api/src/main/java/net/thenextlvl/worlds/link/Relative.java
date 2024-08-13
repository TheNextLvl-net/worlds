package net.thenextlvl.worlds.link;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public enum Relative implements Keyed {
    OVERWORLD(Key.key("relative", "overworld")),
    NETHER(Key.key("relative", "nether")),
    THE_END(Key.key("relative", "the_end"));

    private final Key key;
}
