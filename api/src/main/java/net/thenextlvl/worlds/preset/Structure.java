package net.thenextlvl.worlds.preset;

import com.google.common.base.Preconditions;

public record Structure(String provider, String structure) {

    public static Structure minecraft(String structure) {
        return new Structure("minecraft", structure);
    }

    public static Structure bukkit(org.bukkit.generator.structure.Structure structure) {
        return new Structure(structure.key().namespace(), structure.key().value());
    }

    public static Structure literal(String string) {
        var split = string.split(":", 2);
        Preconditions.checkArgument(split.length == 2, "Not a valid structure: " + string);
        Preconditions.checkArgument(!split[0].isBlank(), "Structure provider cannot be empty");
        Preconditions.checkArgument(!split[1].isBlank(), "Structure name cannot be empty");
        return new Structure(split[0], split[1]);
    }

    @Override
    public String toString() {
        return provider() + ":" + structure();
    }
}
