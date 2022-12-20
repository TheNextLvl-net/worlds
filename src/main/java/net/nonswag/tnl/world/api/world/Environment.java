package net.nonswag.tnl.world.api.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.World;

import javax.annotation.Nullable;

@Getter
@AllArgsConstructor
public enum Environment {
    NORMAL(World.Environment.NORMAL, "normal"),
    NETHER(World.Environment.NETHER, "nether"),
    THE_END(World.Environment.THE_END, "end");

    private final World.Environment environment;
    private final String name;

    @Nullable
    public static Environment getByName(String name) {
        for (Environment environment : values()) {
            if(environment.getName().equalsIgnoreCase(name)) return environment;
            if (environment.name().equalsIgnoreCase(name)) return environment;
        }
        return null;
    }

    public static Environment valueOf(World.Environment bukkit) {
        Environment environment = getByName(bukkit.name());
        return environment != null ? environment : NORMAL;
    }
}
