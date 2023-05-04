package net.thenextlvl.worlds.volume;

import org.jetbrains.annotations.Nullable;

public record Generator(String plugin, @Nullable String id) {

    @Override
    public String toString() {
        return id() != null ? plugin() + ":" + id() : plugin();
    }
}
