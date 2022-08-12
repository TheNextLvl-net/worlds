package net.nonswag.tnl.world.api.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record TNLWorld(@Nonnull World bukkit, @Nonnull Environment environment, @Nonnull WorldType type, @Nullable String generator, boolean fullBright) {

    @Nonnull
    private static final HashMap<World, TNLWorld> worlds = new HashMap<>();

    @Nonnull
    public static List<TNLWorld> cast(@Nonnull List<World> worlds) {
        List<TNLWorld> tnlWorlds = new ArrayList<>();
        worlds.forEach(world -> tnlWorlds.add(cast(world)));
        return tnlWorlds;
    }

    @Nonnull
    @SuppressWarnings("deprecation")
    public static TNLWorld cast(@Nonnull World world) {
        if (worlds.containsKey(world)) return worlds.get(world);
        return new TNLWorld(world, Environment.valueOf(world.getEnvironment()), WorldType.valueOf(world.getWorldType()), null, false).register();
    }

    @Nullable
    public static TNLWorld cast(@Nonnull String name) {
        World world = Bukkit.getWorld(name);
        return world != null ? cast(world) : null;
    }

    @Nullable
    public static TNLWorld nullable(@Nullable World world) {
        return world != null ? cast(world) : null;
    }

    @Nonnull
    public TNLWorld register() throws IllegalStateException {
        if (worlds.containsKey(bukkit())) {
            throw new IllegalStateException("%s is already registered".formatted(bukkit().getName()));
        }
        worlds.put(bukkit(), this);
        return this;
    }

    @Nonnull
    public TNLWorld unregister() throws IllegalStateException {
        if (!worlds.containsKey(bukkit())) {
            throw new IllegalStateException("%s is not registered".formatted(bukkit().getName()));
        }
        worlds.remove(bukkit());
        return this;
    }
}
