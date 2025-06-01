package net.thenextlvl.worlds.level;

import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
class FoliaLevel extends LevelData {
    public FoliaLevel(WorldsPlugin plugin, Builder builder) throws IllegalStateException {
        super(plugin, builder);
    }

    @Override
    public Optional<World> create() {
        throw new UnsupportedOperationException("Folia 1.21.5 is not supported yet");
//        var server = ((CraftServer) plugin.getServer());
//        Preconditions.checkState(!server.getWorlds().isEmpty(), "Cannot create additional worlds on STARTUP");
//
//        String name = creator.name();
//        ChunkGenerator generator = creator.generator();
//        BiomeProvider biomeProvider = creator.biomeProvider();
//        File folder = new File(server.getWorldContainer(), name);
//        World world = server.getWorld(name);
//
//        // Paper start
//        World worldByKey = server.getWorld(creator.key());
//        if (world != null || worldByKey != null) {
//            if (world == worldByKey) {
//                return world;
//            }
//            throw new IllegalArgumentException("Cannot create a world with key " + creator.key() + " and name " + name + " one (or both) already match a world that exists");
//        }
//        // Paper end
//
//        if (folder.exists()) {
//            Preconditions.checkArgument(folder.isDirectory(), "File (%s) exists and isn't a folder", name);
//        }
//
//        if (generator == null) {
//            generator = server.getGenerator(name);
//        }
//
//        if (biomeProvider == null) {
//            biomeProvider = server.getBiomeProvider(name);
//        }
//
//        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
//            case NORMAL -> LevelStem.OVERWORLD;
//            case NETHER -> LevelStem.NETHER;
//            case THE_END -> LevelStem.END;
//            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
//        };
//
//        LevelStorageSource.LevelStorageAccess worldSession;
//        try {
//            worldSession = LevelStorageSource.createDefault(server.getWorldContainer().toPath()).validateAndCreateAccess(name, actualDimension);
//        } catch (IOException | ContentValidationException ex) {
//            throw new RuntimeException(ex);
//        }
//
//        Dynamic<?> dynamic;
//        if (worldSession.hasWorldData()) {
//            LevelSummary worldinfo;
//
//            try {
//                dynamic = worldSession.getDataTag();
//                worldinfo = worldSession.getSummary(dynamic);
//            } catch (NbtException | ReportedNbtException | IOException ioexception) {
//                LevelStorageSource.LevelDirectory convertable_b = worldSession.getLevelDirectory();
//
//                MinecraftServer.LOGGER.warn("Failed to load world data from {}", convertable_b.dataFile(), ioexception);
//                MinecraftServer.LOGGER.info("Attempting to use fallback");
//
//                try {
//                    dynamic = worldSession.getDataTagFallback();
//                    worldinfo = worldSession.getSummary(dynamic);
//                } catch (NbtException | ReportedNbtException | IOException ioexception1) {
//                    MinecraftServer.LOGGER.error("Failed to load world data from {}", convertable_b.oldDataFile(), ioexception1);
//                    MinecraftServer.LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", convertable_b.dataFile(), convertable_b.oldDataFile());
//                    return null;
//                }
//
//                worldSession.restoreLevelDataFromOld();
//            }
//
//            if (worldinfo.requiresManualConversion()) {
//                MinecraftServer.LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
//                return null;
//            }
//
//            if (!worldinfo.isCompatible()) {
//                MinecraftServer.LOGGER.info("This world was created by an incompatible version.");
//                return null;
//            }
//        } else {
//            dynamic = null;
//        }
//
//        boolean hardcore = creator.hardcore();
//
//        PrimaryLevelData worlddata;
//        var console = server.getServer();
//        WorldLoader.DataLoadContext worldloader_a = console.worldLoader;
//        RegistryAccess.Frozen iregistrycustom_dimension = worldloader_a.datapackDimensions();
//        Registry<LevelStem> iregistry = iregistrycustom_dimension.lookupOrThrow(Registries.LEVEL_STEM);
//        if (dynamic != null) {
//            LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen());
//
//            worlddata = (PrimaryLevelData) leveldataanddimensions.worldData();
//            iregistrycustom_dimension = leveldataanddimensions.dimensions().dimensionsRegistryAccess();
//        } else {
//            LevelSettings worldsettings;
//            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
//            WorldDimensions worlddimensions;
//
//            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));
//
//            worldsettings = new LevelSettings(name, GameType.byId(server.getDefaultGameMode().getValue()), hardcore, Difficulty.EASY, false, new GameRules(worldloader_a.dataConfiguration().enabledFeatures()), worldloader_a.dataConfiguration());
//            worlddimensions = properties.create(worldloader_a.datapackWorldgen());
//
//            WorldDimensions.Complete worlddimensions_b = worlddimensions.bake(iregistry);
//            Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle());
//
//            worlddata = new PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle);
//            iregistrycustom_dimension = worlddimensions_b.dimensionsRegistryAccess();
//        }
//        iregistry = iregistrycustom_dimension.lookupOrThrow(Registries.LEVEL_STEM);
//        worlddata.customDimensions = iregistry;
//        worlddata.checkName(name);
//        worlddata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());
//
//        if (console.options.has("forceUpgrade")) {
//            Main.forceUpgrade(worldSession, DataFixers.getDataFixer(), console.options.has("eraseCache"), () -> true, iregistrycustom_dimension, console.options.has("recreateRegionFiles"));
//        }
//
//        long j = BiomeManager.obfuscateSeed(worlddata.worldGenOptions().seed()); // Paper - use world seed
//        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
//        LevelStem worlddimension = iregistry.getValue(actualDimension);
//
//        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), worlddimension.type().value(), worlddimension.generator(), server.getHandle().getServer().registryAccess()); // Paper - Expose vanilla BiomeProvider from WorldInfo
//        if (biomeProvider == null && generator != null) {
//            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
//        }
//
//        ResourceKey<Level> worldKey;
//        String levelName = console.getProperties().levelName;
//        if (name.equals(levelName + "_nether")) {
//            worldKey = Level.NETHER;
//        } else if (name.equals(levelName + "_the_end")) {
//            worldKey = Level.END;
//        } else {
//            worldKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(creator.key().namespace(), creator.key().value()));
//        }
//
//        // If set to not keep spawn in memory (changed from default) then adjust rule accordingly
//        if (creator.keepSpawnLoaded() == TriState.FALSE) { // Paper
//            worlddata.getGameRules().getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null);
//        }
//        ServerLevel internal = new ServerLevel(console, console.executor, worldSession, worlddata, worldKey, worlddimension, console.progressListenerFactory.create(worlddata.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)),
//                worlddata.isDebugWorld(), j, creator.environment() == World.Environment.NORMAL ? list : ImmutableList.of(), true, console.overworld().getRandomSequences(), creator.environment(), generator, biomeProvider);
//
//        if (server.getWorld(name.toLowerCase(Locale.ROOT)) == null) {
//            return null;
//        }
//
//        console.addLevel(internal); // Paper - Put world into worldlist before initing the world; move up
//
//        int loadRegionRadius = 1024 >> 4;
//        internal.randomSpawnSelection = new ChunkPos(internal.getChunkSource().randomState().sampler().findSpawnPosition());
//        for (int currX = -loadRegionRadius; currX <= loadRegionRadius; ++currX) {
//            for (int currZ = -loadRegionRadius; currZ <= loadRegionRadius; ++currZ) {
//                ChunkPos pos = new ChunkPos(currX, currZ);
//                internal.chunkSource.addTicketAtLevel(
//                        net.minecraft.server.level.TicketType.UNKNOWN, pos, ca.spottedleaf.moonrise.patches.chunk_system.scheduling.ChunkHolderManager.MAX_TICKET_LEVEL, pos
//                );
//            }
//        }
//
//        // console.initWorld(internal, worlddata, worlddata, worlddata.worldGenOptions());
//
//        internal.setSpawnSettings(true);
//        // Paper - Put world into worldlist before initing the world; move up
//
//        console.prepareLevels(internal.getChunkSource().chunkMap.progressListener, internal);
//        // Paper - rewrite chunk system
//
//        server.paperPluginManager.callEvent(new WorldLoadEvent(internal.getWorld()));
//        return internal.getWorld();
    }
}