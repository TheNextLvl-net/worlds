package net.nonswag.tnl.world.api;

import org.bukkit.WorldCreator;

import java.util.Objects;

public class WorldGenerator {

    private final WorldCreator worldCreator;

    public WorldGenerator(String name) {
        this.worldCreator = new WorldCreator(name);
    }

    public WorldCreator getWorldCreator() {
        return worldCreator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldGenerator that = (WorldGenerator) o;
        return Objects.equals(worldCreator, that.worldCreator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldCreator);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "WorldGenerator{" +
                "worldCreator=" + worldCreator +
                '}';
    }
}
