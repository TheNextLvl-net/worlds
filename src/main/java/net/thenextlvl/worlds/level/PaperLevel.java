package net.thenextlvl.worlds.level;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import io.papermc.paper.FeatureHooks;
import io.papermc.paper.world.PaperWorldLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.ContentValidationException;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.DimensionType;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.generator.GeneratorType;
import net.thenextlvl.worlds.api.preset.Presets;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.generator.WorldInfo;
import org.jspecify.annotations.NullMarked;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.persistence.PersistentDataType.STRING;

@NullMarked
final class PaperLevel extends LevelData {
    public PaperLevel(final WorldsPlugin plugin, final Builder builder) {
        super(plugin, builder);
    }

    @Override
    public CompletableFuture<World> createAsync() {
        return plugin.supplyGlobal(this::createInternal);
    }

    /**
     * @see MinecraftServer#createLevel(LevelStem, PaperWorldLoader.WorldLoadingInfo, LevelStorageSource.LevelStorageAccess, PrimaryLevelData)
     * @see CraftServer#createWorld(org.bukkit.WorldCreator)
     */
    private CompletableFuture<World> createInternal() {
        final var server = ((CraftServer) plugin.getServer());
        final var console = server.getServer();

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
        final var chunkGenerator = Optional.ofNullable(super.chunkGenerator)
                .orElseGet(() -> Optional.ofNullable(generator)
                        .map(generator -> generator.generator(name))
                        .orElseGet(() -> server.getGenerator(name)));
        var biomeProvider = Optional.ofNullable(super.biomeProvider)
                .orElseGet(() -> Optional.ofNullable(generator)
                        .map(generator -> generator.biomeProvider(name))
                        .orElseGet(() -> server.getBiomeProvider(name)));
        /// Worlds end

        final var dimensionType = resolveDimensionKey();

        final LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = LevelStorageSource.createDefault(directory.getParent())
                    .validateAndCreateAccess(directory.getFileName().toString(), dimensionType);
        } catch (final IOException | ContentValidationException ex) {
            WorldsPlugin.ERROR_TRACKER.trackError(ex);
            return CompletableFuture.failedFuture(ex);
        }

        final PrimaryLevelData primaryLevelData;
        final WorldLoader.DataLoadContext context = console.worldLoaderContext;
        RegistryAccess.Frozen registryAccess = context.datapackDimensions();
        Registry<LevelStem> contextLevelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
        /// Worlds start - fail if dimension could not be read
        final var levelData = PaperWorldLoader.getLevelData(levelStorageAccess);
        if (levelData.fatalError()) return CompletableFuture.failedFuture(new IOException("Failed to read level data"));
        /// Worlds end
        final Dynamic<?> dataTag = ignoreLevelData ? null : levelData.dataTag(); /// Worlds - ignore level data

        if (dataTag != null) {
            final LevelDataAndDimensions levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(
                    dataTag, context.dataConfiguration(), contextLevelStemRegistry, context.datapackWorldgen()
            );
            primaryLevelData = (PrimaryLevelData) levelDataAndDimensions.worldData();
            registryAccess = levelDataAndDimensions.dimensions().dimensionsRegistryAccess();

            /// Worlds start - override options
            try {
                final var worldOptions = new WorldOptions(seed, structures, bonusChest);
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
            final WorldOptions worldOptions = new WorldOptions(seed, structures, bonusChest);
            WorldDimensions worldDimensions;

            final var generatorSettings = Optional.ofNullable(preset).orElse(Presets.CLASSIC_FLAT).serialize(); /// Worlds - serialize preset
            final DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(generatorSettings, generatorType.presetName().asString());
            levelSettings = new LevelSettings(
                    name,
                    GameType.byId(server.getDefaultGameMode().getValue()),
                    hardcore, Difficulty.EASY,
                    false,
                    new GameRules(context.dataConfiguration().enabledFeatures()),
                    context.dataConfiguration()
            );
            worldDimensions = properties.create(context.datapackWorldgen());

            /// Worlds start - replace generators
            if (generatorType.equals(GeneratorType.FLAT) || generatorType.equals(GeneratorType.DEBUG)) {
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

        final WorldInfo worldInfo = new CraftWorldInfo(primaryLevelData, levelStorageAccess, toBukkit(levelStem.dimensionType()), customStem.type().value(), customStem.generator(), server.getHandle().getServer().registryAccess());
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
                levelStem == net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD ? list : ImmutableList.of(), ///  Worlds
                true,
                console.overworld().getRandomSequences(),
                toBukkit(levelStem.dimensionType()),
                chunkGenerator, biomeProvider
        );

        /// Worlds start - ensure world is memoized before adding to server
        if (server.getWorld(name) == null) return CompletableFuture.failedFuture(
                new IllegalStateException("World with name " + name + " was not properly memoized")
        );
        /// Worlds end

        console.addLevel(serverLevel);
        console.initWorld(serverLevel, primaryLevelData, primaryLevelData.worldGenOptions());

        serverLevel.setSpawnSettings(true);

        /// Worlds start - persist world extra data
        persistWorld(serverLevel.getWorld(), levelStem, enabled.toBooleanOrElse(true));
        if (generator != null) persistGenerator(serverLevel.getWorld(), generator);
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

    public void persistWorld(final World world, final net.thenextlvl.worlds.api.generator.LevelStem dimension, final boolean enabled) {
        final var worldKey = new NamespacedKey("worlds", "world_key");
        final var dimensionKey = new NamespacedKey("worlds", "dimension");
        world.getPersistentDataContainer().set(worldKey, STRING, world.key().asString());
        world.getPersistentDataContainer().set(dimensionKey, STRING, dimension.dimensionType().key().asString());
        plugin.levelView().setEnabled(world, enabled);
    }

    public void persistGenerator(final World world, final Generator generator) {
        final var generatorKey = new NamespacedKey("worlds", "generator");
        world.getPersistentDataContainer().set(generatorKey, STRING, generator.asString());
    }

    private ResourceKey<LevelStem> resolveDimensionKey() {
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD)) return LevelStem.OVERWORLD;
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.NETHER)) return LevelStem.NETHER;
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.END)) return LevelStem.END;
        throw new IllegalArgumentException("Illegal dimension (" + getLevelStem() + ")");
    }

    private World.Environment toBukkit(final DimensionType type) {
        if (type.equals(DimensionType.OVERWORLD)) return World.Environment.NORMAL;
        if (type.equals(DimensionType.THE_NETHER)) return World.Environment.NETHER;
        if (type.equals(DimensionType.THE_END)) return World.Environment.THE_END;
        return World.Environment.CUSTOM;
    }
}
