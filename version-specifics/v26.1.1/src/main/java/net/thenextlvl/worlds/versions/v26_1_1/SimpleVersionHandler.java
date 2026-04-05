package net.thenextlvl.worlds.versions.v26_1_1;

import ca.spottedleaf.moonrise.common.util.TickThread;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.papermc.paper.FeatureHooks;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import io.papermc.paper.world.PaperWorldLoader;
import io.papermc.paper.world.saveddata.PaperWorldPDC;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.FileUtil;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.thenextlvl.worlds.api.preset.Presets;
import net.thenextlvl.worlds.api.view.LevelView;
import net.thenextlvl.worlds.versions.PluginAccess;
import net.thenextlvl.worlds.versions.VersionHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class SimpleVersionHandler extends VersionHandler {
    public SimpleVersionHandler(final PluginAccess plugin) {
        super(plugin, null, false);
    }

    @Override
    public Class<?> getTickThreadClass() {
        return TickThread.class;
    }

    @Override
    public String getOverworldName() {
        final var properties = ((CraftServer) plugin.getServer()).getServer().getProperties();
        return properties.levelName;
    }

    @Override
    public boolean isDirectoryLockException(final Throwable throwable) {
        return throwable.getCause() instanceof DirectoryLock.LockException;
    }

    @Override
    public CompletableFuture<@Nullable Void> saveAsync(final World world, final boolean flush) {
        try {
            final var level = ((CraftWorld) world).getHandle();
            final var oldSave = level.noSave;
            level.noSave = false;
            level.save(null, flush, false);
            level.noSave = oldSave;
            return CompletableFuture.completedFuture(null);
        } catch (final Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<@Nullable Void> saveLevelDataAsync(final World world) {
        final var level = ((CraftWorld) world).getHandle();
        final SavedDataStorage savedDataStorage = level.getChunkSource().getDataStorage();
        savedDataStorage.computeIfAbsent(PaperWorldPDC.TYPE).setFrom((CraftPersistentDataContainer) world.getPersistentDataContainer());
        return savedDataStorage.scheduleSave().thenApply(ignored -> null);
    }

    @Override
    public Optional<JavaPlugin> getGenerator(final World world) {
        return Optional.ofNullable(world.getGenerator())
                .map(chunkGenerator -> chunkGenerator.getClass().getClassLoader())
                .filter(ConfiguredPluginClassLoader.class::isInstance)
                .map(ConfiguredPluginClassLoader.class::cast)
                .map(ConfiguredPluginClassLoader::getPlugin);
    }

    @Override
    public void generateEndPlatform(final World world, final Entity entity) {
        final var handle = ((CraftWorld) world).getHandle();
        final var entityHandle = plugin.isRunningFolia() ? null : ((CraftEntity) entity).getHandle();
        EndPlatformFeature.createEndPlatform(handle, new BlockPos(100, 49, 0), true, entityHandle);
    }

    @Override
    public void handleEndCredits(final Player player) {
        final var level = ((CraftWorld) player.getWorld()).getHandle();
        if (plugin.isRunningFolia() || level.paperConfig().misc.disableEndCredits) {
            ((CraftPlayer) player).getHandle().seenCredits = true;
        } else if (!((CraftPlayer) player).getHandle().seenCredits) {
            ((CraftPlayer) player).getHandle().showEndCredits();
        }
    }

    @Override
    public @Nullable Location getRespawnLocation(final Player player, final boolean load) {
        return player.getRespawnLocation(load);
    }

    @Override
    public Boolean hasBonusChest(final World world) {
        return world.hasBonusChest();
    }

    /**
     * @see MinecraftServer#createLevel(LevelStem, PaperWorldLoader.WorldLoadingInfoAndData, LevelDataAndDimensions.WorldDataAndGenSettings)
     * @see CraftServer#createWorld(org.bukkit.WorldCreator)
     */
    @Override
    public CompletableFuture<World> createAsync(final net.thenextlvl.worlds.api.level.Level level, final LevelView levelView) {
        final var server = ((CraftServer) plugin.getServer());
        final var console = server.getServer();

        final var directory = level.getDirectory();
        final var key = level.key();
        final var name = level.getName();

        try {
            Preconditions.checkState(console.getAllLevels().iterator().hasNext(), "Cannot create worlds before main level is created");
            Preconditions.checkArgument(!Files.exists(directory) || Files.isDirectory(directory), "File (%s) exists and isn't a folder", directory);

            Preconditions.checkArgument(server.getWorld(key) == null, "World with key %s already exists", key);
            Preconditions.checkArgument(server.getWorld(name) == null, "World with name %s already exists", name);

            Preconditions.checkState(plugin.getServer().getWorlds().stream()
                            .map(World::getWorldFolder)
                            .map(File::toPath)
                            .noneMatch(directory::equals),
                    "World with directory %s already exists", directory);
        } catch (final RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }

        /// Worlds start - find generator and biome provider
        final var chunkGenerator = level.getChunkGenerator()
                .orElseGet(() -> level.getGenerator()
                        .map(generator -> generator.generator(name))
                        .orElseGet(() -> server.getGenerator(name)));
        var biomeProvider = level.getBiomeProvider()
                .orElseGet(() -> level.getGenerator()
                        .map(generator -> generator.biomeProvider(name))
                        .orElseGet(() -> server.getBiomeProvider(name)));
        /// Worlds end

        final var actualDimension = resolveDimensionKey(level);

        final ResourceKey<net.minecraft.world.level.Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(key.namespace(), key.value()));
        final WorldLoader.DataLoadContext context = console.worldLoaderContext;
        RegistryAccess.Frozen registryAccess = context.datapackDimensions();
        net.minecraft.core.Registry<LevelStem> contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        final LevelStem configuredStem = console.registryAccess().lookupOrThrow(Registries.LEVEL_STEM).getValue(actualDimension);
        if (configuredStem == null) {
            throw new IllegalStateException("Missing configured level stem " + actualDimension);
        }

        PaperWorldLoader.LoadedWorldData loadedWorldData = PaperWorldLoader.loadWorldData(
                console,
                dimensionKey,
                name
        );

        final PrimaryLevelData primaryLevelData = (PrimaryLevelData) console.getWorldData();
        WorldGenSettings worldGenSettings = LevelStorageSource.readExistingSavedData(console.storageSource, dimensionKey, console.registryAccess(), WorldGenSettings.TYPE)
                .result()
                .orElse(null);
        if (worldGenSettings == null) {
            final WorldOptions worldOptions = new WorldOptions(level.getSeed(), level.hasStructures(), level.hasBonusChest());

            final var generatorSettings = level.getPreset().orElse(Presets.CLASSIC_FLAT).serialize(); /// Worlds - serialize preset
            final DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(generatorSettings, level.getGeneratorType().presetName().asString());
            final WorldDimensions worldDimensions = properties.create(context.datapackWorldgen());

            final WorldDimensions.Complete complete = worldDimensions.bake(contextLevelStemRegistry);
            if (complete.dimensions().getValue(actualDimension) == null) {
                throw new IllegalStateException("Missing generated level stem " + actualDimension + " for world " + name);
            }

            worldGenSettings = new WorldGenSettings(worldOptions, worldDimensions);
            registryAccess = complete.dimensionsRegistryAccess();
            loadedWorldData.levelOverrides().setHardcore(level.isHardcore());
            loadedWorldData = new PaperWorldLoader.LoadedWorldData(
                    loadedWorldData.bukkitName(),
                    loadedWorldData.uuid(),
                    loadedWorldData.pdc(),
                    loadedWorldData.levelOverrides()
            );
        }
        final WorldGenSettings genSettingsFinal = worldGenSettings;

        contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.forceUpgrade(console.storageSource, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, registryAccess, console.options.has("recreateRegionFiles"));
        }

        final long biomeZoomSeed = BiomeManager.obfuscateSeed(genSettingsFinal.options().seed());
        LevelStem customStem = genSettingsFinal.dimensions().get(actualDimension).orElse(null);
        if (customStem == null) {
            customStem = contextLevelStemRegistry.getValue(actualDimension);
        }
        if (customStem == null) {
            throw new IllegalStateException("Missing level stem for world " + name + " using key " + actualDimension);
        }

        final WorldInfo worldInfo = new CraftWorldInfo(loadedWorldData.bukkitName(), genSettingsFinal.options().seed(), primaryLevelData.enabledFeatures(), toBukkit(level.getLevelStem().dimensionType()), customStem.type().value(), customStem.generator(), server.getHandle().getServer().registryAccess(), loadedWorldData.uuid());
        if (biomeProvider == null && chunkGenerator != null) {
            biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo);
        }

        final SavedDataStorage savedDataStorage = new SavedDataStorage(console.storageSource.getDimensionPath(dimensionKey).resolve(LevelResource.DATA.id()), console.getFixerUpper(), console.registryAccess());
        savedDataStorage.set(WorldGenSettings.TYPE, new WorldGenSettings(genSettingsFinal.options(), genSettingsFinal.dimensions()));
        final List<CustomSpawner> list = ImmutableList.of(
                new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(savedDataStorage)
        );

        final ServerLevel serverLevel = new ServerLevel(
                console,
                console.executor,
                console.storageSource,
                genSettingsFinal,
                dimensionKey,
                customStem,
                primaryLevelData.isDebugWorld(),
                biomeZoomSeed,
                level.getLevelStem() == net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD ? list : ImmutableList.of(),
                true,
                actualDimension,
                toBukkit(level.getLevelStem().dimensionType()),
                chunkGenerator,
                biomeProvider,
                savedDataStorage,
                loadedWorldData
        );
        
        
        
        
        
        
        /*
        
        final Dynamic<?> dataTag = level.ignoreLevelData() ? null : levelData.dataTag(); /// Worlds - ignore level data

        if (dataTag != null) {
            final LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(
                    dataTag, context.dataConfiguration(), contextLevelStemRegistry, context.datapackWorldgen()
            );
            primaryLevelData = (PrimaryLevelData) levelDataAndDimensions.worldData();
            registryAccess = levelDataAndDimensions.dimensions().dimensionsRegistryAccess();

            /// Worlds start - override options
            try {
                final var worldOptions = new WorldOptions(level.getSeed(), level.hasStructures(), level.hasBonusChest());
                final var optionsField = PrimaryLevelData.class.getDeclaredField("worldOptions");
                final var accessible = optionsField.canAccess(primaryLevelData);
                if (!accessible) optionsField.trySetAccessible();
                optionsField.set(primaryLevelData, worldOptions);
                if (!accessible) optionsField.setAccessible(false);
            } catch (final NoSuchFieldException | IllegalAccessException e) {
                plugin.getComponentLogger().warn("Failed to override world options", e);
            }
            /// Worlds end
        } else {
            final LevelSettings levelSettings;
            final WorldOptions worldOptions = new WorldOptions(level.getSeed(), level.hasStructures(), level.hasBonusChest());
            WorldDimensions worldDimensions;

            final var generatorSettings = level.getPreset().orElse(Presets.CLASSIC_FLAT).serialize(); /// Worlds - serialize preset
            final DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(generatorSettings, level.getGeneratorType().presetName().asString());
            levelSettings = new LevelSettings(
                    name,
                    GameType.byId(server.getDefaultGameMode().getValue()),
                    level.isHardcore(), Difficulty.EASY,
                    false,
                    new GameRules(context.dataConfiguration().enabledFeatures()),
                    context.dataConfiguration()
            );
            worldDimensions = properties.create(context.datapackWorldgen());

            /// Worlds start - replace generators
            if (level.getGeneratorType().equals(GeneratorType.FLAT) || level.getGeneratorType().equals(GeneratorType.DEBUG)) {
                worldDimensions = replaceGenerator(LevelStem.NETHER, context.datapackWorldgen(), worldDimensions.dimensions(), worldDimensions.overworld());
                worldDimensions = replaceGenerator(LevelStem.END, context.datapackWorldgen(), worldDimensions.dimensions(), worldDimensions.overworld());
            }
            /// Worlds end

            final WorldDimensions.Complete complete = worldDimensions.bake(contextLevelStemRegistry);
            final Lifecycle lifecycle = complete.lifecycle().add(context.datapackWorldgen().allRegistriesLifecycle());

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

        final long seed = BiomeManager.obfuscateSeed(primaryLevelData.worldGenOptions().seed());
        final List<CustomSpawner> list = ImmutableList.of(
                new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(primaryLevelData)
        );
        final LevelStem customStem = contextLevelStemRegistry.getValueOrThrow(dimensionType);

        final WorldInfo worldInfo = new CraftWorldInfo(primaryLevelData, levelStorageAccess, toBukkit(level.getLevelStem().dimensionType()), customStem.type().value(), customStem.generator(), server.getHandle().getServer().registryAccess());
        if (biomeProvider == null && chunkGenerator != null) {
            biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo);
        }

        final ResourceKey<net.minecraft.world.level.Level> dimensionKey;
        final String levelName = server.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            dimensionKey = net.minecraft.world.level.Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            dimensionKey = net.minecraft.world.level.Level.END;
        } else {
            dimensionKey = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(key.namespace(), key.value()));
        }

        final ServerLevel serverLevel = new ServerLevel(
                console,
                console.executor,
                levelStorageAccess,
                primaryLevelData,
                dimensionKey,
                customStem,
                primaryLevelData.isDebugWorld(),
                seed,
                level.getLevelStem() == net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD ? list : ImmutableList.of(), ///  Worlds
                true,
                console.overworld().getRandomSequences(),
                toBukkit(level.getLevelStem().dimensionType()),
                chunkGenerator, biomeProvider
        );
        */

        /// Worlds start - ensure world is memoized before adding to server
        if (server.getWorld(name) == null) return CompletableFuture.failedFuture(
                new IllegalStateException("World with name " + name + " was not properly memoized")
        );
        /// Worlds end

        /// Worlds start - set initialized flag
        switch (level.initialized()) {
            case TRUE -> primaryLevelData.setInitialized(true);
            case FALSE -> primaryLevelData.setInitialized(false);
        }
        /// Worlds end

        console.addLevel(serverLevel);
        console.initWorld(serverLevel);

        serverLevel.setSpawnSettings(true);

        /// Worlds start - persist world extra data
        persistWorld(levelView, serverLevel.getWorld(), level.getLevelStem(), level.isEnabled().toBooleanOrElse(true));
        level.getGenerator().ifPresent(generator -> persistGenerator(serverLevel.getWorld(), generator));
        /// Worlds end

        /// Worlds start - start entity ticking for folia
        FeatureHooks.tickEntityManager(serverLevel);
        /// Worlds end

        console.prepareLevel(serverLevel);

        return CompletableFuture.completedFuture(serverLevel.getWorld());
    }

    /**
     * @see WorldDimensions#replaceOverworldGenerator
     */
    private static WorldDimensions replaceGenerator(final ResourceKey<LevelStem> key, final HolderLookup.Provider registries, final Map<ResourceKey<LevelStem>, LevelStem> dimensions, final ChunkGenerator chunkGenerator) {
        final HolderLookup<net.minecraft.world.level.dimension.DimensionType> holderLookup = registries.lookupOrThrow(Registries.DIMENSION_TYPE);
        final Map<ResourceKey<LevelStem>, LevelStem> map = withGenerator(key, holderLookup, dimensions, chunkGenerator);
        return new WorldDimensions(map);
    }

    /**
     * @see WorldDimensions#withOverworld(HolderLookup, Map, ChunkGenerator)
     */
    private static Map<ResourceKey<LevelStem>, LevelStem> withGenerator(
            final ResourceKey<LevelStem> key, final HolderLookup<net.minecraft.world.level.dimension.DimensionType> dimensionTypeRegistry, final Map<ResourceKey<LevelStem>, LevelStem> dimensions, final ChunkGenerator chunkGenerator
    ) {
        final LevelStem levelStem = dimensions.get(key);
        final Holder<DimensionType> holder = levelStem == null
                ? dimensionTypeRegistry.getOrThrow(BuiltinDimensionTypes.OVERWORLD)
                : levelStem.type();
        return withGenerator(key, dimensions, holder, chunkGenerator);
    }

    /**
     * @see WorldDimensions#withOverworld(Map, Holder, ChunkGenerator)
     */
    private static Map<ResourceKey<LevelStem>, LevelStem> withGenerator(
            final ResourceKey<LevelStem> key, final Map<ResourceKey<LevelStem>, LevelStem> stemMap, final Holder<net.minecraft.world.level.dimension.DimensionType> dimensionType, final ChunkGenerator chunkGenerator
    ) {
        final ImmutableMap.Builder<ResourceKey<LevelStem>, LevelStem> builder = ImmutableMap.builder();
        builder.putAll(stemMap);
        builder.put(key, new LevelStem(dimensionType, chunkGenerator));
        return builder.buildKeepingLast();
    }

    private ResourceKey<LevelStem> resolveDimensionKey(final net.thenextlvl.worlds.api.level.Level level) {
        if (level.getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD))
            return LevelStem.OVERWORLD;
        if (level.getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.NETHER)) return LevelStem.NETHER;
        if (level.getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.END)) return LevelStem.END;
        throw new IllegalArgumentException("Illegal dimension (" + level.getLevelStem() + ")");
    }

    @Override
    public String findAvailableName(final Path path, final String name, final String format) throws IOException {
        return FileUtil.findAvailableName(path, name, format);
    }
}
