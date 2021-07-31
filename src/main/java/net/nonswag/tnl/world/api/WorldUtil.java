package net.nonswag.tnl.world.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.nonswag.tnl.listener.api.config.JsonConfig;
import net.nonswag.tnl.listener.api.logger.Logger;
import net.nonswag.tnl.listener.api.object.Objects;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldUtil {

    @Nonnull
    protected static final WorldUtil instance = new WorldUtil();

    @Nonnull
    private final JsonConfig saves = new JsonConfig("plugins/Worlds/", "saves.json");

    protected WorldUtil() {
    }

    @Nonnull
    public List<String> getWorlds() {
        List<String> strings = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : getSaves().getJsonElement().getAsJsonObject().entrySet()) {
            strings.add(entry.getKey());
        }
        return strings;
    }

    public void save(@Nonnull World world) {
        JsonObject jsonObject = getSaves().getJsonElement().getAsJsonObject();
        if (!jsonObject.has(world.getName())) jsonObject.add(world.getName(), new JsonObject());
        JsonObject jsonWorld = jsonObject.getAsJsonObject(world.getName());
        if (!jsonWorld.has("type")) {
            WorldType type = world.getWorldType();
            if (type == null) type = WorldType.NORMAL;
            jsonWorld.addProperty("type", type.name());
        }
        if (!jsonWorld.has("environment")) jsonWorld.addProperty("environment", world.getEnvironment().name());
        if (!jsonWorld.has("seed")) jsonWorld.addProperty("seed", world.getSeed());
        if (!jsonWorld.has("generator")) {
            Plugin generator = getGenerator(world);
            if (generator != null) jsonWorld.addProperty("generator", generator.getName());
        }
        getSaves().save();
    }

    @Nullable
    public Plugin getGenerator(@Nonnull World world) {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            ChunkGenerator generator = plugin.getDefaultWorldGenerator(world.getName(), null);
            if (generator != null && generator.equals(world.getGenerator())) return plugin;
        }
        return null;
    }

    public void loadWorlds() {
        for (String s : getWorlds()) loadWorld(s);
    }

    public void loadWorld(@Nonnull String name) {
        if (Bukkit.getWorld(name) == null) {
            File file = new File(name);
            if (file.exists() && file.isDirectory()) {
                try {
                    if (getSaves().getJsonElement().getAsJsonObject().has(name) && getSaves().getJsonElement().getAsJsonObject().get(name).isJsonObject()) {
                        JsonObject world = getSaves().getJsonElement().getAsJsonObject().getAsJsonObject(name);
                        WorldCreator worldCreator = new WorldCreator(name);
                        if (world.has("generator")) {
                            Plugin plugin = Bukkit.getPluginManager().getPlugin(world.get("generator").getAsString());
                            if (plugin != null && plugin.isEnabled()) {
                                worldCreator.generator(plugin.getDefaultWorldGenerator(name, null));
                            } else worldCreator.generator(((ChunkGenerator) null));
                        } else worldCreator.generator(((ChunkGenerator) null));
                        if (world.has("type")) {
                            worldCreator.type(Objects.getOrDefault(WorldType.getByName(world.get("type").getAsString()), WorldType.NORMAL));
                        } else worldCreator.type(WorldType.NORMAL);
                        if (world.has("environment")) {
                            Environment environment = Environment.getByName(world.get("environment").getAsString());
                            if (environment != null) worldCreator.environment(environment.getEnvironment());
                        } else worldCreator.environment(World.Environment.NORMAL);
                        if (world.has("seed")) worldCreator.seed(world.get("seed").getAsLong());
                        World created = worldCreator.createWorld();
                        if (created != null) Logger.debug.println("Loaded world: " + created.getName());
                        else Logger.error.println("Could not create world");
                    }
                } catch (Exception e) {
                    Logger.error.println("Failed to load world '" + name + "'", e);
                }
            }
        }
    }

    @Nonnull
    public JsonConfig getSaves() {
        return saves;
    }

    @Nonnull
    public static WorldUtil getInstance() {
        return instance;
    }
}
