package net.thenextlvl.perworlds.group;

import com.google.common.base.Preconditions;
import core.nbt.serialization.NBT;
import core.nbt.serialization.adapter.EnumAdapter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.adapter.AdvancementDataAdapter;
import net.thenextlvl.perworlds.adapter.AttributeAdapter;
import net.thenextlvl.perworlds.adapter.AttributeDataAdapter;
import net.thenextlvl.perworlds.adapter.DateAdapter;
import net.thenextlvl.perworlds.adapter.ItemStackArrayAdapter;
import net.thenextlvl.perworlds.adapter.KeyAdapter;
import net.thenextlvl.perworlds.adapter.LocationAdapter;
import net.thenextlvl.perworlds.adapter.NamespacedKeyAdapter;
import net.thenextlvl.perworlds.adapter.PlayerDataAdapter;
import net.thenextlvl.perworlds.adapter.PotionEffectAdapter;
import net.thenextlvl.perworlds.adapter.PotionEffectTypeAdapter;
import net.thenextlvl.perworlds.adapter.StatisticsAdapter;
import net.thenextlvl.perworlds.adapter.VectorAdapter;
import net.thenextlvl.perworlds.adapter.WardenSpawnTrackerAdapter;
import net.thenextlvl.perworlds.adapter.WorldAdapter;
import net.thenextlvl.perworlds.data.AdvancementData;
import net.thenextlvl.perworlds.data.AttributeData;
import net.thenextlvl.perworlds.data.PlayerData;
import net.thenextlvl.perworlds.data.WardenSpawnTracker;
import net.thenextlvl.perworlds.statistics.Stats;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public class PaperGroupProvider implements GroupProvider {
    private final File dataFolder;
    private final List<WorldGroup> groups = new ArrayList<>(); // todo: move to config
    private final NBT nbt;
    private final SharedWorlds commons;

    public PaperGroupProvider(SharedWorlds commons) {
        this.commons = commons;
        this.dataFolder = new File(commons.getDataFolder(), "groups");
        this.nbt = new NBT.Builder()
                .registerTypeHierarchyAdapter(AdvancementData.class, new AdvancementDataAdapter(getServer()))
                .registerTypeHierarchyAdapter(Attribute.class, new AttributeAdapter())
                .registerTypeHierarchyAdapter(AttributeData.class, new AttributeDataAdapter())
                .registerTypeHierarchyAdapter(Date.class, new DateAdapter())
                .registerTypeHierarchyAdapter(GameMode.class, new EnumAdapter<>(GameMode.class))
                .registerTypeHierarchyAdapter(ItemStack[].class, new ItemStackArrayAdapter())
                .registerTypeHierarchyAdapter(Key.class, new KeyAdapter())
                .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
                .registerTypeHierarchyAdapter(NamespacedKey.class, new NamespacedKeyAdapter())
                .registerTypeHierarchyAdapter(PlayerData.class, new PlayerDataAdapter())
                .registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter())
                .registerTypeHierarchyAdapter(PotionEffectType.class, new PotionEffectTypeAdapter())
                .registerTypeHierarchyAdapter(Stats.class, new StatisticsAdapter())
                .registerTypeHierarchyAdapter(Vector.class, new VectorAdapter())
                .registerTypeHierarchyAdapter(WardenSpawnTracker.class, new WardenSpawnTrackerAdapter())
                .registerTypeHierarchyAdapter(World.class, new WorldAdapter(getServer()))
                .build();
    }

    public ComponentLogger getLogger() {
        return commons.getPlugin().getComponentLogger();
    }

    public Server getServer() {
        return commons.getServer();
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
    public Optional<WorldGroup> getGroup(String name) {
        return groups.stream().filter(group -> group.getName().equals(name)).findAny();
    }

    @Override
    public Optional<WorldGroup> getGroup(World world) {
        return groups.stream().filter(group -> group.containsWorld(world)).findAny();
    }

    @Override
    public WorldGroup createGroup(String name, Consumer<GroupSettings> settings, Collection<World> worlds) {
        Preconditions.checkState(!hasGroup(name), "Cannot create multiple groups with the same key");
        var invalid = worlds.stream().filter(this::hasGroup).map(Keyed::key).map(Key::asString).toList();
        Preconditions.checkState(invalid.isEmpty(), "Worlds cannot be in multiple groups: {}", String.join(", ", invalid));

        var groupSettings = new PaperGroupSettings();
        settings.accept(groupSettings);

        var group = new PaperWorldGroup(this, name, groupSettings, Set.copyOf(worlds));
        groups.add(group);
        return group;
    }

    @Override
    public WorldGroup createGroup(String name, Consumer<GroupSettings> settings, World... worlds) {
        return createGroup(name, settings, List.of(worlds));
    }

    @Override
    public boolean hasGroup(String name) {
        return groups.stream().anyMatch(group -> group.getName().equals(name));
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
    public boolean removeGroup(String name) {
        return getGroup(name).map(this::removeGroup).orElse(false);
    }

    @Override
    public boolean removeGroup(WorldGroup group) {
        return groups.remove(group);
    }
}
