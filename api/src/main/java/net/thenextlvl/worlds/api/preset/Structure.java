package net.thenextlvl.worlds.api.preset;

public record Structure(String structure) {
    Structure(org.bukkit.generator.structure.Structure structure) {
        this(structure.key().asString());
    }

    public static Structure minecraft(String structure) {
        return new Structure("minecraft:" + structure);
    }

    @Override
    public String toString() {
        return structure();
    }
}
