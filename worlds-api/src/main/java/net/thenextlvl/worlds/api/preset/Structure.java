package net.thenextlvl.worlds.api.preset;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record Structure(String structure) {
    @Override
    public String toString() {
        return structure();
    }

    public static Structure minecraft(String structure) {
        return new Structure("minecraft:" + structure);
    }
}
