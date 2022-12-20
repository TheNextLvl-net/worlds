package net.nonswag.tnl.world.api.world;

import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record TNLWorld(World bukkit, Environment environment, WorldType type, @Nullable String generator, boolean fullBright) {

    private static final HashMap<World, TNLWorld> worlds = new HashMap<>();

    public static List<TNLWorld> cast(List<World> worlds) {
        List<TNLWorld> tnlWorlds = new ArrayList<>();
        worlds.forEach(world -> tnlWorlds.add(cast(world)));
        return tnlWorlds;
    }

    @SuppressWarnings("deprecation")
    public static TNLWorld cast(World world) {
        if (worlds.containsKey(world)) return worlds.get(world);
        return new TNLWorld(world, Environment.valueOf(world.getEnvironment()), WorldType.valueOf(world.getWorldType()), null, false).register();
    }

    @Nullable
    public static TNLWorld cast(String name) {
        World world = Bukkit.getWorld(name);
        return world != null ? cast(world) : null;
    }

    @Nullable
    public static TNLWorld nullable(@Nullable World world) {
        return world != null ? cast(world) : null;
    }

    public TNLWorld register() throws IllegalStateException {
        if (worlds.containsKey(bukkit())) {
            throw new IllegalStateException("%s is already registered".formatted(bukkit().getName()));
        }
        worlds.put(bukkit(), this);
        return this;
    }

    public TNLWorld unregister() throws IllegalStateException {
        if (!worlds.containsKey(bukkit())) {
            throw new IllegalStateException("%s is not registered".formatted(bukkit().getName()));
        }
        worlds.remove(bukkit());
        return this;
    }
}
