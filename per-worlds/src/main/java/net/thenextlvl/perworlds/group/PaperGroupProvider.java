package net.thenextlvl.perworlds.group;

import com.google.common.base.Preconditions;
import core.file.FileIO;
import core.file.format.GsonFile;
import core.io.IO;
import core.nbt.serialization.NBT;
import core.nbt.serialization.adapter.EnumAdapter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.adapter.AttributeAdapter;
import net.thenextlvl.perworlds.adapter.AttributeDataAdapter;
import net.thenextlvl.perworlds.adapter.ItemStackAdapter;
import net.thenextlvl.perworlds.adapter.ItemStackArrayAdapter;
import net.thenextlvl.perworlds.adapter.KeyAdapter;
import net.thenextlvl.perworlds.adapter.LocationAdapter;
import net.thenextlvl.perworlds.adapter.NamespacedKeyAdapter;
import net.thenextlvl.perworlds.adapter.PlayerDataAdapter;
import net.thenextlvl.perworlds.adapter.PotionEffectAdapter;
import net.thenextlvl.perworlds.adapter.PotionEffectTypeAdapter;
import net.thenextlvl.perworlds.adapter.WorldAdapter;
import net.thenextlvl.perworlds.data.AttributeData;
import net.thenextlvl.perworlds.data.PlayerData;
import net.thenextlvl.perworlds.model.PerWorldsConfig;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@NullMarked
public class PaperGroupProvider implements GroupProvider {
    private final File dataFolder;
    private final FileIO<PerWorldsConfig> config; // todo: do something with that
    private final List<WorldGroup> groups = new ArrayList<>();
    private final NBT nbt;
    private final Plugin plugin;

    public PaperGroupProvider(Plugin plugin) {
        var pluginDirectory = new File("plugins/PerWorlds");
        this.dataFolder = new File(pluginDirectory, "saves");
        this.config = new GsonFile<>(IO.of(pluginDirectory, "config.json"), new PerWorldsConfig(new HashMap<>()));
        this.nbt = new NBT.Builder()
                .registerTypeHierarchyAdapter(Attribute.class, new AttributeAdapter())
                .registerTypeHierarchyAdapter(AttributeData.class, new AttributeDataAdapter())
                .registerTypeHierarchyAdapter(GameMode.class, new EnumAdapter<>(GameMode.class))
                .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeHierarchyAdapter(ItemStack[].class, new ItemStackArrayAdapter())
                .registerTypeHierarchyAdapter(Key.class, new KeyAdapter())
                .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
                .registerTypeHierarchyAdapter(NamespacedKey.class, new NamespacedKeyAdapter())
                .registerTypeHierarchyAdapter(PlayerData.class, new PlayerDataAdapter())
                .registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter())
                .registerTypeHierarchyAdapter(PotionEffectType.class, new PotionEffectTypeAdapter())
                .registerTypeHierarchyAdapter(World.class, new WorldAdapter(plugin.getServer()))
                .build();
        this.plugin = plugin;
    }

    public ComponentLogger getLogger() {
        return plugin.getComponentLogger();
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    public NBT nbt() {
        return nbt;
    }

    @Override
    public @Unmodifiable List<WorldGroup> getGroups() {
        return List.copyOf(groups);
    }

    @Override
    public Optional<WorldGroup> getGroup(Key key) {
        return groups.stream().filter(group -> group.key().equals(key)).findAny();
    }

    @Override
    public Optional<WorldGroup> getGroup(World world) {
        return groups.stream().filter(group -> group.containsWorld(world)).findAny();
    }

    @Override
    public WorldGroup.Builder createGroup(Key key) {
        return new PaperWorldGroup.Builder(this, key);
    }

    @Override
    public boolean addGroup(WorldGroup group) {
        Preconditions.checkState(!hasGroup(group), "Group is already registered");
        return groups.add(group);
    }

    @Override
    public boolean hasGroup(Key key) {
        return groups.stream().anyMatch(group -> group.key().equals(key));
    }

    @Override
    public boolean hasGroup(World world) {
        return groups.stream().anyMatch(group -> group.containsWorld(world));
    }

    @Override
    public boolean hasGroup(WorldGroup group) {
        return groups.contains(group);
    }

    @Override
    public boolean removeGroup(Key key) {
        return groups.removeIf(group -> group.key().equals(key));
    }

    @Override
    public boolean removeGroup(WorldGroup group) {
        return groups.remove(group);
    }
}
