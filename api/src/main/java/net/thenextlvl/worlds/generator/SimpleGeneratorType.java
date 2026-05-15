package net.thenextlvl.worlds.generator;

import net.kyori.adventure.key.Key;

non-sealed class SimpleGeneratorType implements GeneratorType {
    private final Key key;
    private final String name;

    SimpleGeneratorType(final Key key, final String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public String toString() {
        return "SimpleGeneratorType{" +
                "key=" + key +
                ", name='" + name + '\'' +
                '}';
    }
}
