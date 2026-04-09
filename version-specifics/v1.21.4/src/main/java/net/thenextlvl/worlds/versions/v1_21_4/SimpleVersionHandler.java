package net.thenextlvl.worlds.versions.v1_21_4;

import ca.spottedleaf.moonrise.common.util.TickThread;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import io.papermc.paper.FeatureHooks;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.minecraft.FileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import net.thenextlvl.worlds.experimental.GeneratorType;
import net.thenextlvl.worlds.preset.Preset;
import net.thenextlvl.worlds.versions.PluginAccess;
import net.thenextlvl.worlds.versions.VersionHandler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
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
        super(plugin, plugin.isRunningFolia() ? new SimpleFoliaSupport(plugin) : null, true);
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
        if (level.getDragonFight() != null) {
            level.serverLevelData.setEndDragonFightData(level.getDragonFight().saveData());
        }
        level.serverLevelData.setCustomBossEvents(level.getServer().getCustomBossEvents().save(level.registryAccess()));
        level.levelStorageAccess.saveDataTag(level.getServer().registryAccess(), level.serverLevelData, level.getServer().getPlayerList().getSingleplayerData());
        return level.getChunkSource().getDataStorage().scheduleSave().thenApply(ignored -> null);
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
        return load ? player.getRespawnLocation() : player.getPotentialRespawnLocation();
    }

    @Override
    public @Nullable Boolean hasBonusChest(final World world) {
        return null;
    }

    /**
     * @see MinecraftServer#createLevel(LevelStem, PaperWorldLoader.WorldLoadingInfo, LevelStorageSource.LevelStorageAccess, PrimaryLevelData)
     * @see CraftServer#createWorld(org.bukkit.WorldCreator)
     */
    @Override
    @SuppressWarnings("JavadocReference")
    public CompletableFuture<World> createAsync(final net.thenextlvl.worlds.Level level) {
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
                        .flatMap(generator -> generator.generator(name))
                        .orElseGet(() -> server.getGenerator(name)));
        var biomeProvider = level.getBiomeProvider()
                .orElseGet(() -> level.getGenerator()
                        .flatMap(generator -> generator.biomeProvider(name))
                        .orElseGet(() -> server.getBiomeProvider(name)));
        /// Worlds end

        final var dimensionType = resolveDimensionKey(level);

        final LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = LevelStorageSource.createDefault(directory.getParent())
                    .validateAndCreateAccess(directory.getFileName().toString(), dimensionType);
        } catch (final IOException | ContentValidationException ex) {
            plugin.getErrorTracker().trackError(ex);
            return CompletableFuture.failedFuture(ex);
        }

        Dynamic<?> dataTag;
        if (levelStorageAccess.hasWorldData()) {
            LevelSummary summary;
            try {
                dataTag = levelStorageAccess.getDataTag();
                summary = levelStorageAccess.getSummary(dataTag);
            } catch (final NbtException | ReportedNbtException | IOException e) {
                final LevelStorageSource.LevelDirectory levelDirectory = levelStorageAccess.getLevelDirectory();
                plugin.getComponentLogger().warn("Failed to load world data from {}, attempting to use fallback", levelDirectory.dataFile(), e);

                try {
                    dataTag = levelStorageAccess.getDataTagFallback();
                    summary = levelStorageAccess.getSummary(dataTag);
                } catch (final NbtException | ReportedNbtException | IOException e1) {
                    plugin.getComponentLogger().error("Failed to load world data from {}", levelDirectory.oldDataFile(), e1);
                    plugin.getComponentLogger().error(
                            "Failed to load world data from {} and {}. World files may be corrupted.",
                            levelDirectory.dataFile(),
                            levelDirectory.oldDataFile()
                    );
                    return CompletableFuture.failedFuture(e1);
                }

                levelStorageAccess.restoreLevelDataFromOld();
            }

            if (summary.requiresManualConversion()) {
                plugin.getComponentLogger().warn("This world must be opened in an older version (like 1.6.4) to be safely converted");
                return CompletableFuture.failedFuture(new IllegalStateException("World requires manual conversion"));
            }

            if (!summary.isCompatible()) {
                plugin.getComponentLogger().warn("This world was created by an incompatible version.");
                return CompletableFuture.failedFuture(new IllegalStateException("World is incompatible"));
            }
        } else {
            dataTag = null;
        }

        final PrimaryLevelData primaryLevelData;
        final WorldLoader.DataLoadContext context = console.worldLoader;
        RegistryAccess.Frozen registryAccess = context.datapackDimensions();
        Registry<LevelStem> contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        if (dataTag != null && !level.ignoreLevelData()) {
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

            final var generatorSettings = level.getPreset().orElse(Preset.CLASSIC_FLAT).serialize(); /// Worlds - serialize preset
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
            Main.forceUpgrade(levelStorageAccess, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, registryAccess, console.options.has("recreateRegionFiles"));
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

        final ResourceKey<Level> dimensionKey;
        final String levelName = server.getServer().getProperties().levelName;
        if (name.equals(levelName + "_nether")) {
            dimensionKey = Level.NETHER;
        } else if (name.equals(levelName + "_the_end")) {
            dimensionKey = Level.END;
        } else {
            dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(key.namespace(), key.value()));
        }

        final ServerLevel serverLevel = new ServerLevel(
                console,
                console.executor,
                levelStorageAccess,
                primaryLevelData,
                dimensionKey,
                customStem,
                MinecraftServer.getServer().progressListenerFactory.create(primaryLevelData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)),
                primaryLevelData.isDebugWorld(),
                seed,
                level.getLevelStem() == net.thenextlvl.worlds.LevelStem.OVERWORLD ? list : ImmutableList.of(), ///  Worlds
                true,
                console.overworld().getRandomSequences(),
                toBukkit(level.getLevelStem().dimensionType()),
                chunkGenerator, biomeProvider
        );

        if (server.getWorld(name) == null) return CompletableFuture.failedFuture(
                new IllegalStateException("World with name " + name + " was not properly memoized")
        );

        console.addLevel(serverLevel);

        final var future = new CompletableFuture<World>();
        if (plugin.isRunningFolia()) {
            serverLevel.randomSpawnSelection = new ChunkPos(serverLevel.getChunkSource().randomState().sampler().findSpawnPosition());

            final var x = serverLevel.randomSpawnSelection.x;
            final var z = serverLevel.randomSpawnSelection.z;

            plugin.getServer().getRegionScheduler().run(plugin, serverLevel.getWorld(), x, z, scheduledTask -> {
                console.initWorld(serverLevel, primaryLevelData, primaryLevelData, primaryLevelData.worldGenOptions());
                future.complete(serverLevel.getWorld());
            });
        } else {
            console.initWorld(serverLevel, primaryLevelData, primaryLevelData, primaryLevelData.worldGenOptions());
            future.complete(serverLevel.getWorld());
        }

        serverLevel.setSpawnSettings(true);

        /// Worlds start - persist world extra data and start ticking

        console.prepareLevels(serverLevel.getChunkSource().chunkMap.progressListener, serverLevel);
        if (plugin.isRunningFolia())
            io.papermc.paper.threadedregions.RegionizedServer.getInstance().addWorld(serverLevel);
        FeatureHooks.tickEntityManager(serverLevel);

        persistWorld(serverLevel.getWorld(), level.getLevelStem(), level.isEnabled().toBooleanOrElse(true));
        level.getGenerator().ifPresent(generator -> persistGenerator(serverLevel.getWorld(), generator));
        /// Worlds end

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
        final Holder<net.minecraft.world.level.dimension.DimensionType> holder = levelStem == null
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

    private ResourceKey<LevelStem> resolveDimensionKey(final net.thenextlvl.worlds.Level level) {
        if (level.getLevelStem().equals(net.thenextlvl.worlds.LevelStem.OVERWORLD))
            return LevelStem.OVERWORLD;
        if (level.getLevelStem().equals(net.thenextlvl.worlds.LevelStem.NETHER)) return LevelStem.NETHER;
        if (level.getLevelStem().equals(net.thenextlvl.worlds.LevelStem.END)) return LevelStem.END;
        throw new IllegalArgumentException("Illegal dimension (" + level.getLevelStem() + ")");
    }

    @Override
    public String findAvailableName(final Path path, final String name, final String format) throws IOException {
        return FileUtil.findAvailableName(path, name, format);
    }
}
