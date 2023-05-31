package net.thenextlvl.worlds.preset;

import com.google.common.base.Preconditions;

public record Structure(String provider, String structure) {

    public static Structure minecraft(String structure) {
        return new Structure("minecraft", structure);
    }

    public static Structure bukkit(org.bukkit.generator.structure.Structure structure) {
        return new Structure(structure.getKey().getNamespace(), structure.getKey().getKey());
    }

    public static Structure literal(String string) {
        var split = string.split(":", 2);
        Preconditions.checkArgument(split.length == 2, "Not a valid structure: " + string);
        return new Structure(split[0], split[1]);
    }

    @Override
    public String toString() {
        return provider() + ":" + structure();
    }
}
