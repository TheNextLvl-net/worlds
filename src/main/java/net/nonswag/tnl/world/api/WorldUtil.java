package net.nonswag.tnl.world.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.nonswag.tnl.listener.api.file.formats.JsonFile;
import net.nonswag.tnl.listener.api.file.helper.FileHelper;
import net.nonswag.tnl.listener.api.logger.Logger;
import net.nonswag.tnl.listener.api.object.Objects;
import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.world.api.events.WorldDeleteEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
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

    @SuppressWarnings("deprecation")
    public void export(@Nonnull World world) {
        JsonObject jsonObject = getSaves().getJsonElement().getAsJsonObject();
        if (!jsonObject.has(world.getName())) jsonObject.add(world.getName(), new JsonObject());
        JsonObject jsonWorld = jsonObject.getAsJsonObject(world.getName());
        WorldType type = world.getWorldType();
        if (type == null) type = WorldType.NORMAL;
        jsonWorld.addProperty("type", type.name());
        jsonWorld.addProperty("environment", world.getEnvironment().name());
        jsonWorld.addProperty("seed", world.getSeed());
        Plugin generator = getGenerator(world);
        if (generator != null) jsonWorld.addProperty("generator", generator.getName());
    }

    public void exportAll() {
        for (World world : Bukkit.getWorlds()) export(world);
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
                if (to != null) player.teleport(to.getSpawnLocation());
                else player.disconnect("%prefix%\n§cThere are no loaded worlds");
            }
        }
        return Bukkit.unloadWorld(world, save);
    }

    public boolean deleteWorld(@Nonnull World world) {
        if (unloadWorld(world, false) && new WorldDeleteEvent(world).call()) {
            File file = new File(world.getName());
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
        File sessionLock = new File(new File(name), "session.lock");
        if (sessionLock.exists()) FileHelper.deleteDirectory(sessionLock);
        JsonObject world = root.getAsJsonObject(name);
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
        return created;
    }

    @Nonnull
    public JsonFile getSaves() {
        return saves;
    }

    public void saveWorlds() {
        getSaves().save();
    }

    @Nonnull
    public static WorldUtil getInstance() {
        return instance;
    }
}
