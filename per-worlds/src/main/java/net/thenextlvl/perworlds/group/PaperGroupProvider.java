package net.thenextlvl.perworlds.group;

import com.google.common.base.Preconditions;
import core.i18n.file.ComponentBundle;
import core.nbt.serialization.NBT;
import core.nbt.serialization.adapter.EnumAdapter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.util.TriState;
import net.thenextlvl.perworlds.GroupData;
import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.UnownedWorldGroup;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.adapter.AdvancementDataAdapter;
import net.thenextlvl.perworlds.adapter.AttributeAdapter;
import net.thenextlvl.perworlds.adapter.AttributeDataAdapter;
import net.thenextlvl.perworlds.adapter.DateAdapter;
import net.thenextlvl.perworlds.adapter.GroupConfigAdapter;
import net.thenextlvl.perworlds.adapter.GroupDataAdapter;
import net.thenextlvl.perworlds.adapter.GroupSettingsAdapter;
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
import net.thenextlvl.perworlds.adapter.WorldBorderAdapter;
import net.thenextlvl.perworlds.data.AdvancementData;
import net.thenextlvl.perworlds.data.AttributeData;
import net.thenextlvl.perworlds.data.WardenSpawnTracker;
import net.thenextlvl.perworlds.data.WorldBorderData;
import net.thenextlvl.perworlds.model.PaperPlayerData;
import net.thenextlvl.perworlds.model.config.GroupConfig;
import net.thenextlvl.perworlds.statistics.Stats;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public class PaperGroupProvider implements GroupProvider {
    private final File dataFolder;
    private final Set<WorldGroup> groups = new HashSet<>();
    private final NBT nbt;
    private final SharedWorlds commons;
    private final UnownedWorldGroup unownedWorldGroup;

    public PaperGroupProvider(SharedWorlds commons) {
        this.commons = commons;
        this.dataFolder = new File(commons.getDataFolder(), "groups");
        this.nbt = new NBT.Builder()
                .registerTypeHierarchyAdapter(AdvancementData.class, new AdvancementDataAdapter(getServer()))
                .registerTypeHierarchyAdapter(Attribute.class, new AttributeAdapter())
                .registerTypeHierarchyAdapter(AttributeData.class, new AttributeDataAdapter())
                .registerTypeHierarchyAdapter(Date.class, new DateAdapter())
                .registerTypeHierarchyAdapter(Difficulty.class, new EnumAdapter<>(Difficulty.class))
                .registerTypeHierarchyAdapter(GameMode.class, new EnumAdapter<>(GameMode.class))
                .registerTypeHierarchyAdapter(GroupConfig.class, new GroupConfigAdapter(this))
                .registerTypeHierarchyAdapter(GroupData.class, new GroupDataAdapter(this))
                .registerTypeHierarchyAdapter(GroupSettings.class, new GroupSettingsAdapter())
                .registerTypeHierarchyAdapter(ItemStack[].class, new ItemStackArrayAdapter())
                .registerTypeHierarchyAdapter(Key.class, new KeyAdapter())
                .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
                .registerTypeHierarchyAdapter(NamespacedKey.class, new NamespacedKeyAdapter())
                .registerTypeHierarchyAdapter(PaperPlayerData.class, new PlayerDataAdapter())
                .registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter())
                .registerTypeHierarchyAdapter(PotionEffectType.class, new PotionEffectTypeAdapter())
                .registerTypeHierarchyAdapter(Stats.class, new StatisticsAdapter())
                .registerTypeHierarchyAdapter(TriState.class, new EnumAdapter<>(TriState.class))
                .registerTypeHierarchyAdapter(Vector.class, new VectorAdapter())
                .registerTypeHierarchyAdapter(WardenSpawnTracker.class, new WardenSpawnTrackerAdapter())
                .registerTypeHierarchyAdapter(World.class, new WorldAdapter(getServer()))
                .registerTypeHierarchyAdapter(WorldBorderData.class, new WorldBorderAdapter())
                .build();
        this.unownedWorldGroup = new PaperUnownedWorldGroup(this);
    }

    public ComponentLogger getLogger() {
        return commons.getLogger();
    }

    @Override
    public Server getServer() {
        return commons.getServer();
    }

    public Plugin getPlugin() {
        return commons.getPlugin();
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    public NBT nbt() {
        return nbt;
    }

    public ComponentBundle bundle() {
        return commons.bundle();
    }

    @Override
    public @Unmodifiable Set<WorldGroup> getAllGroups() {
        var groups = new HashSet<>(getGroups());
        groups.add(unownedWorldGroup);
        return Set.copyOf(groups);
    }

    @Override
    public @Unmodifiable Set<WorldGroup> getGroups() {
        return Set.copyOf(groups);
    }

    @Override
    public Optional<WorldGroup> getGroup(String name) {
        return unownedWorldGroup.getName().equals(name) ? Optional.of(unownedWorldGroup)
                : groups.stream().filter(group -> group.getName().equals(name)).findAny();
    }

    @Override
    public Optional<WorldGroup> getGroup(World world) {
        return groups.stream().filter(group -> group.containsWorld(world)).findAny();
    }

    @Override
    public UnownedWorldGroup getUnownedWorldGroup() {
        return unownedWorldGroup;
    }

    @Override
    public WorldGroup createGroup(String name, Consumer<GroupData> data, Consumer<GroupSettings> settings, Collection<World> worlds) {
        Preconditions.checkState(!hasGroup(name), "A WorldGroup named '%s' already exists", name);
        var invalid = worlds.stream().filter(this::hasGroup).map(Keyed::key).map(Key::asString).toList();
        Preconditions.checkState(invalid.isEmpty(), "Worlds cannot be in multiple groups: {}", String.join(", ", invalid));

        var groupSettings = new PaperGroupSettings();
        var groupData = new PaperGroupData(this);
        settings.accept(groupSettings);
        data.accept(groupData);

        var group = new PaperWorldGroup(this, name, groupData, groupSettings, Set.copyOf(worlds));
        groups.add(group);
        return group;
    }

    @Override
    public WorldGroup createGroup(String name, Collection<World> worlds) throws IllegalStateException {
        return createGroup(name, data -> {
        }, settings -> {
        }, worlds);
    }

    @Override
    public WorldGroup createGroup(String name, Consumer<GroupData> data, Consumer<GroupSettings> settings, World... worlds) {
        return createGroup(name, data, settings, List.of(worlds));
    }

    @Override
    public WorldGroup createGroup(String name, World... worlds) throws IllegalStateException {
        return createGroup(name, data -> {
        }, settings -> {
        }, worlds);
    }

    @Override
    public boolean hasGroup(String name) {
        return groups.stream().anyMatch(group -> group.getName().equals(name))
               || unownedWorldGroup.getName().equals(name);
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
        if (!groups.remove(group)) return false;
        group.getPlayers().forEach(getUnownedWorldGroup()::loadPlayerData);
        return true;
    }
}
