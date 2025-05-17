package net.thenextlvl.worlds.level;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import io.papermc.paper.FeatureHooks;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.preset.Presets;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.WorldInfo;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@NullMarked
class PaperLevel extends LevelData {
    public PaperLevel(WorldsPlugin plugin, Builder builder) {
        super(plugin, builder);
    }

    @Override
    public Optional<World> create() {
        var server = ((CraftServer) plugin.getServer());
        var console = server.getServer();

        Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create additional worlds on STARTUP");

        File folder = new File(server.getWorldContainer(), name);

        if (folder.exists()) {
            Preconditions.checkArgument(folder.isDirectory(), "File (%s) exists and isn't a folder", name);
        }

        World world = server.getWorld(name);
        World worldByKey = server.getWorld(key);
        if (world != null || worldByKey != null) {
            if (world == worldByKey) return Optional.of(world);
            throw new IllegalArgumentException("Cannot create a world with key " + key + " and name " + name + " one (or both) already match a world that exists");
        }

        var chunkGenerator = Optional.ofNullable(generator)
                .map(generator -> generator.generator(name))
                .orElseGet(() -> server.getGenerator(name));
        var biomeProvider = Optional.ofNullable(generator)
                .map(generator -> generator.biomeProvider(name))
                .orElseGet(() -> server.getBiomeProvider(name));

        var dimensionType = resolveDimensionKey();

        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = LevelStorageSource.createDefault(server.getWorldContainer().toPath()).validateAndCreateAccess(name, dimensionType);
        } catch (IOException | ContentValidationException ex) {
            throw new RuntimeException(ex);
        }

        Dynamic<?> dataTag;
        if (levelStorageAccess.hasWorldData()) {
            LevelSummary summary;
            try {
                dataTag = levelStorageAccess.getDataTag();
                summary = levelStorageAccess.getSummary(dataTag);
            } catch (NbtException | ReportedNbtException | IOException e) {
                LevelStorageSource.LevelDirectory levelDirectory = levelStorageAccess.getLevelDirectory();
                MinecraftServer.LOGGER.warn("Failed to load world data from {}", levelDirectory.dataFile(), e);
                MinecraftServer.LOGGER.info("Attempting to use fallback");

                try {
                    dataTag = levelStorageAccess.getDataTagFallback();
                    summary = levelStorageAccess.getSummary(dataTag);
                } catch (NbtException | ReportedNbtException | IOException e1) {
                    MinecraftServer.LOGGER.error("Failed to load world data from {}", levelDirectory.oldDataFile(), e1);
                    MinecraftServer.LOGGER.error(
                            "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.",
                            levelDirectory.dataFile(),
                            levelDirectory.oldDataFile()
                    );
                    return Optional.empty();
                }

                levelStorageAccess.restoreLevelDataFromOld();
            }

            if (summary.requiresManualConversion()) {
                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return Optional.empty();
            }

            if (!summary.isCompatible()) {
                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
                return Optional.empty();
            }
        } else {
            dataTag = null;
        }

        var generatorSettings = Optional.ofNullable(preset).orElse(Presets.CLASSIC_FLAT).serialize();

        PrimaryLevelData primaryLevelData;
        WorldLoader.DataLoadContext context = console.worldLoader;
        RegistryAccess.Frozen registryAccess = context.datapackDimensions();
        Registry<LevelStem> contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        if (dataTag != null) {
            LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(
                    dataTag, context.dataConfiguration(), contextLevelStemRegistry, context.datapackWorldgen()
            );
            primaryLevelData = (PrimaryLevelData) levelDataAndDimensions.worldData();
            registryAccess = levelDataAndDimensions.dimensions().dimensionsRegistryAccess();
        } else {
            LevelSettings levelSettings;
            WorldOptions worldOptions = new WorldOptions(seed, structures, bonusChest);
            WorldDimensions worldDimensions;

            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(generatorSettings, generatorType.presetName().asString());
            levelSettings = new LevelSettings(
                    name,
                    GameType.byId(server.getDefaultGameMode().getValue()),
                    hardcore, Difficulty.EASY,
                    false,
                    new GameRules(context.dataConfiguration().enabledFeatures()),
                    context.dataConfiguration())
            ;
            worldDimensions = properties.create(context.datapackWorldgen());

            WorldDimensions.Complete complete = worldDimensions.bake(contextLevelStemRegistry);
            Lifecycle lifecycle = complete.lifecycle().add(context.datapackWorldgen().allRegistriesLifecycle());

            primaryLevelData = new PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), lifecycle);
            registryAccess = complete.dimensionsRegistryAccess();
        }

        contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        primaryLevelData.customDimensions = contextLevelStemRegistry;
        primaryLevelData.checkName(name);
        primaryLevelData.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        if (console.options.has("forceUpgrade")) {
            Main.forceUpgrade(levelStorageAccess, primaryLevelData, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, registryAccess, console.options.has("recreateRegionFiles"));
        }

        long i = BiomeManager.obfuscateSeed(primaryLevelData.worldGenOptions().seed());
        List<CustomSpawner> list = ImmutableList.of(
                new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(primaryLevelData)
        );
        LevelStem customStem = contextLevelStemRegistry.getValue(dimensionType);

        WorldInfo worldInfo = new CraftWorldInfo(primaryLevelData, levelStorageAccess, levelStem.dimensionType().toBukkit(), customStem.type().value(), customStem.generator(), server.getHandle().getServer().registryAccess());
        if (biomeProvider == null && chunkGenerator != null) {
            biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo);
        }

        ResourceKey<Level> dimensionKey;
        String levelName = server.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            dimensionKey = Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            dimensionKey = Level.END;
        } else {
            dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(key.namespace(), key.value()));
        }

        if (keepSpawnLoaded.equals(TriState.FALSE)) {
            primaryLevelData.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
        }

        ServerLevel serverLevel = new ServerLevel(
                console,
                console.executor,
                levelStorageAccess,
                primaryLevelData,
                dimensionKey,
                customStem,
                MinecraftServer.getServer().progressListenerFactory.create(primaryLevelData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)),
                primaryLevelData.isDebugWorld(),
                i,
                levelStem == net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD ? list : ImmutableList.of(),
                true,
                console.overworld().getRandomSequences(),
                levelStem.dimensionType().toBukkit(),
                chunkGenerator, biomeProvider
        );

        if (server.getWorld(name.toLowerCase(Locale.ROOT)) == null) return Optional.empty();

        console.addLevel(serverLevel);
        console.initWorld(serverLevel, primaryLevelData, primaryLevelData, primaryLevelData.worldGenOptions());

        serverLevel.setSpawnSettings(true);

        MinecraftServer.getServer().prepareLevels(serverLevel.getChunkSource().chunkMap.progressListener, serverLevel);
        FeatureHooks.tickEntityManager(serverLevel);

        new WorldLoadEvent(serverLevel.getWorld()).callEvent();
        return Optional.of(serverLevel.getWorld());
    }

    private ResourceKey<LevelStem> resolveDimensionKey() {
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD)) return LevelStem.OVERWORLD;
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.NETHER)) return LevelStem.NETHER;
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.END)) return LevelStem.END;
        throw new IllegalArgumentException("Illegal dimension (" + getLevelStem() + ")");
    }
}
