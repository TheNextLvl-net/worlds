package net.thenextlvl.worlds.level;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import io.papermc.paper.FeatureHooks;
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
import net.thenextlvl.worlds.api.generator.DimensionType;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.api.preset.Presets;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.generator.CraftWorldInfo;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.WorldInfo;
import org.jspecify.annotations.NullMarked;
import org.spigotmc.AsyncCatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.persistence.PersistentDataType.STRING;

@NullMarked
class PaperLevel extends LevelData {
    public PaperLevel(WorldsPlugin plugin, Builder builder) {
        super(plugin, builder);
    }

    /**
     * @see CraftServer#createWorld(org.bukkit.WorldCreator)
     */
    @Override
    public CompletableFuture<World> createAsync() {
        AsyncCatcher.catchOp("world creation");

        var server = ((CraftServer) plugin.getServer());
        var console = server.getServer();

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
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }

        var chunkGenerator = Optional.ofNullable(super.chunkGenerator)
                .orElseGet(() -> Optional.ofNullable(generator)
                        .map(generator -> generator.generator(name))
                        .orElseGet(() -> server.getGenerator(name)));
        var biomeProvider = Optional.ofNullable(super.biomeProvider)
                .orElseGet(() -> Optional.ofNullable(generator)
                        .map(generator -> generator.biomeProvider(name))
                        .orElseGet(() -> server.getBiomeProvider(name)));

        var dimensionType = resolveDimensionKey();

        LevelStorageSource.LevelStorageAccess levelStorageAccess;
        try {
            levelStorageAccess = LevelStorageSource.createDefault(server.getWorldContainer().toPath()).validateAndCreateAccess(name, dimensionType);
        } catch (IOException | ContentValidationException ex) {
            return CompletableFuture.failedFuture(ex);
        }

        Dynamic<?> dataTag;
        if (levelStorageAccess.hasWorldData()) {
            LevelSummary summary;
            try {
                dataTag = levelStorageAccess.getDataTag();
                summary = levelStorageAccess.getSummary(dataTag);
            } catch (NbtException | ReportedNbtException | IOException e) {
                LevelStorageSource.LevelDirectory levelDirectory = levelStorageAccess.getLevelDirectory();
                plugin.getComponentLogger().warn("Failed to load world data from {}, attempting to use fallback", levelDirectory.dataFile(), e);

                try {
                    dataTag = levelStorageAccess.getDataTagFallback();
                    summary = levelStorageAccess.getSummary(dataTag);
                } catch (NbtException | ReportedNbtException | IOException e1) {
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

        long seed = BiomeManager.obfuscateSeed(primaryLevelData.worldGenOptions().seed());
        List<CustomSpawner> list = ImmutableList.of(
                new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(primaryLevelData)
        );
        LevelStem customStem = contextLevelStemRegistry.getValueOrThrow(dimensionType);

        WorldInfo worldInfo = new CraftWorldInfo(primaryLevelData, levelStorageAccess, toBukkit(levelStem.dimensionType()), customStem.type().value(), customStem.generator(), server.getHandle().getServer().registryAccess());
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

        primaryLevelData.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(spawnChunkRadius, null);

        ServerLevel serverLevel = new ServerLevel(
                console,
                console.executor,
                levelStorageAccess,
                primaryLevelData,
                dimensionKey,
                customStem,
                MinecraftServer.getServer().progressListenerFactory.create(primaryLevelData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)),
                primaryLevelData.isDebugWorld(),
                seed,
                levelStem == net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD ? list : ImmutableList.of(),
                true,
                console.overworld().getRandomSequences(),
                toBukkit(levelStem.dimensionType()),
                chunkGenerator, biomeProvider
        );

        if (server.getWorld(name) == null) return CompletableFuture.failedFuture(
                new IllegalStateException("World with name " + name + " was not properly memoized")
        );

        console.addLevel(serverLevel);

        var future = new CompletableFuture<World>();
        if (WorldsPlugin.RUNNING_FOLIA) {
            // todo: uncomment for folia
            // serverLevel.randomSpawnSelection = new ChunkPos(serverLevel.getChunkSource().randomState().sampler().findSpawnPosition());

            var x = 0; // serverLevel.randomSpawnSelection.x;
            var z = 0; // serverLevel.randomSpawnSelection.z;

            plugin.getServer().getRegionScheduler().run(plugin, serverLevel.getWorld(), x, z, scheduledTask -> {
                console.initWorld(serverLevel, primaryLevelData, primaryLevelData, primaryLevelData.worldGenOptions());
                future.complete(serverLevel.getWorld());
            });
        } else {
            console.initWorld(serverLevel, primaryLevelData, primaryLevelData, primaryLevelData.worldGenOptions());
            future.complete(serverLevel.getWorld());
        }

        serverLevel.setSpawnSettings(true);

        console.prepareLevels(serverLevel.getChunkSource().chunkMap.progressListener, serverLevel);
        if (WorldsPlugin.RUNNING_FOLIA)
            ;// todo: uncomment for folia
            // io.papermc.paper.threadedregions.RegionizedServer.getInstance().addWorld(serverLevel);
        FeatureHooks.tickEntityManager(serverLevel);

        persistWorld(serverLevel.getWorld(), enabled.toBooleanOrElse(true));
        if (generator != null) persistGenerator(serverLevel.getWorld(), generator);

        new WorldLoadEvent(serverLevel.getWorld()).callEvent();
        return future;
    }

    public void persistWorld(World world, boolean enabled) {
        var worldKey = new NamespacedKey("worlds", "world_key");
        world.getPersistentDataContainer().set(worldKey, STRING, world.key().asString());
        plugin.levelView().setEnabled(world, enabled);
    }

    public void persistGenerator(World world, Generator generator) {
        var generatorKey = new NamespacedKey("worlds", "generator");
        world.getPersistentDataContainer().set(generatorKey, STRING, generator.asString());
    }

    private ResourceKey<LevelStem> resolveDimensionKey() {
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.OVERWORLD)) return LevelStem.OVERWORLD;
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.NETHER)) return LevelStem.NETHER;
        if (getLevelStem().equals(net.thenextlvl.worlds.api.generator.LevelStem.END)) return LevelStem.END;
        throw new IllegalArgumentException("Illegal dimension (" + getLevelStem() + ")");
    }

    private World.Environment toBukkit(DimensionType type) {
        if (type.equals(DimensionType.THE_END)) return World.Environment.THE_END;
        if (type.equals(DimensionType.THE_NETHER)) return World.Environment.NETHER;
        return World.Environment.NORMAL;
    }
}
