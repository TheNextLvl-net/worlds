package net.thenextlvl.worlds.generator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

@Getter
@RequiredArgsConstructor
public abstract class WorldGenerator {
    public static final Map<Plugin, Set<WorldGenerator>> GENERATORS = new WeakHashMap<>();

    private final Plugin owner;
    private final String name;


    /**
     * @see Plugin#getDefaultWorldGenerator(String, String)
     */
    @Nullable
    public ChunkGenerator getWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return getOwner().getDefaultWorldGenerator(worldName, id);
    }

    /**
     * @see Plugin#getDefaultBiomeProvider(String, String)
     */
    @Nullable
    public BiomeProvider getBiomeProvider(@NotNull String worldName, @Nullable String id) {
        return getOwner().getDefaultBiomeProvider(worldName, id);
    }

    public WorldGenerator register() {
        var set = Collections.<WorldGenerator>newSetFromMap(new WeakHashMap<>());
        var generators = GENERATORS.getOrDefault(getOwner(), set);
        generators.add(this);
        GENERATORS.put(getOwner(), generators);
        return this;
    }

    public void unregister() {
        var generators = GENERATORS.get(getOwner());
        if (generators == null) return;
        generators.remove(this);
        GENERATORS.put(getOwner(), generators);
    }

    public boolean isRegistered() {
        var generators = GENERATORS.get(getOwner());
        return generators != null && generators.contains(this);
    }

    @Nullable
    public static WorldGenerator getGenerator(Plugin plugin, String name) {
        return GENERATORS.get(plugin).stream()
                .filter(generator -> generator.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static WorldGenerator getGenerator(String name) {
        for (var plugin : GENERATORS.keySet()) {
            var generator = getGenerator(plugin, name);
            if (generator != null) return generator;
        }
        return null;
    }
}
