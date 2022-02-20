package net.nonswag.tnl.world.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.nonswag.tnl.core.api.file.formats.JsonFile;
import net.nonswag.tnl.core.api.file.helper.FileHelper;
import net.nonswag.tnl.core.api.logger.Logger;
import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.world.api.events.WorldDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldUtil {

    @Getter
    @Nonnull
    private static final WorldUtil instance = new WorldUtil();
    @Getter
    @Nonnull
    private static final HashMap<World, net.nonswag.tnl.world.api.WorldType> worldTypes = new HashMap<>();

    @Getter
    @Nonnull
    private final JsonFile saves = new JsonFile("plugins/Worlds/", "saves.json");

    private WorldUtil() {
    }

    @Nonnull
    public List<String> getWorlds() {
        List<String> strings = new ArrayList<>();
        JsonObject root = getSaves().getJsonElement().getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) strings.add(entry.getKey());
        return strings;
    }

    public void export(@Nonnull World world) {
        JsonObject jsonObject = getSaves().getJsonElement().getAsJsonObject();
        if (!jsonObject.has(world.getName())) jsonObject.add(world.getName(), new JsonObject());
        JsonObject jsonWorld = jsonObject.getAsJsonObject(world.getName());
        jsonWorld.addProperty("type", getWorldTypes().getOrDefault(world, WorldType.NORMAL).name());
        jsonWorld.addProperty("environment", world.getEnvironment().name());
        jsonWorld.addProperty("seed", world.getSeed());
        Plugin generator = getGenerator(world);
        if (generator != null) jsonWorld.addProperty("generator", generator.getName());
    }

    public void exportAll() {
        for (World world : Bukkit.getWorlds()) export(world);
        getSaves().save();
    }

    @Nullable
    public Plugin getGenerator(@Nonnull World world) {
        if (world.getGenerator() == null) return null;
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            ChunkGenerator generator = plugin.getDefaultWorldGenerator(world.getName(), null);
            if (generator != null && generator.getClass().equals(world.getGenerator().getClass())) return plugin;
        }
        return null;
    }

    public boolean unloadWorld(@Nonnull World world, boolean save) {
        if (!world.getPlayers().isEmpty()) {
            World to = null;
            for (World bukkit : Bukkit.getWorlds()) if (world != bukkit) to = bukkit;
            for (Player all : world.getPlayers()) {
                TNLPlayer player = TNLPlayer.cast(all);
                if (to != null) player.worldManager().teleport(to.getSpawnLocation());
                else player.disconnect("%prefix%\n§cThere are no loaded worlds");
            }
        }
        ((CraftWorld) world).getHandle().getPlayers().clear();
        return Bukkit.unloadWorld(world, save);
    }

    public boolean deleteWorld(@Nonnull World world) {
        if (unloadWorld(world, false) && new WorldDeleteEvent(world).call()) {
            File file = new File(Bukkit.getWorldContainer(), world.getName());
            FileHelper.deleteDirectory(file);
            JsonObject root = getSaves().getJsonElement().getAsJsonObject();
            root.remove(world.getName());
            return !file.exists();
        } else return false;
    }

    public void loadWorlds() {
        for (String s : getWorlds()) loadWorld(s);
    }

    @Nullable
    public World loadWorld(@Nonnull String name) {
        World bukkit = Bukkit.getWorld(name);
        if (bukkit != null) return bukkit;
        JsonObject root = getSaves().getJsonElement().getAsJsonObject();
        if (!root.has(name) || !root.get(name).isJsonObject()) return null;
        File sessionLock = new File(new File(Bukkit.getWorldContainer(), name), "session.lock");
        if (sessionLock.exists()) FileHelper.deleteDirectory(sessionLock);
        JsonObject world = root.getAsJsonObject(name);
        WorldCreator worldCreator = new WorldCreator(name);
        if (world.has("generator")) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(world.get("generator").getAsString());
            if (plugin != null && plugin.isEnabled()) {
                worldCreator.generator(plugin.getDefaultWorldGenerator(name, null));
            } else worldCreator.generator(((ChunkGenerator) null));
        } else worldCreator.generator(((ChunkGenerator) null));
        WorldType worldType;
        if (world.has("type") && (worldType = WorldType.getByName(world.get("type").getAsString())) != null) {
            worldCreator.type(worldType.getWorldType());
        } else worldCreator.type((worldType = WorldType.NORMAL).getWorldType());
        if (world.has("environment")) {
            Environment environment = Environment.getByName(world.get("environment").getAsString());
            if (environment != null) worldCreator.environment(environment.getEnvironment());
        } else worldCreator.environment(World.Environment.NORMAL);
        if (world.has("seed")) worldCreator.seed(world.get("seed").getAsLong());
        World created = worldCreator.createWorld();
        if (created != null) {
            Logger.debug.println("Loaded world: " + created.getName());
            getWorldTypes().put(created, worldType);
        } else Logger.error.println("Could not create world");
        return created;
    }
}
