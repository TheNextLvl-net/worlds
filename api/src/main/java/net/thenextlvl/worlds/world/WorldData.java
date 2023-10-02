package net.thenextlvl.worlds.world;

import com.google.gson.annotations.SerializedName;
import core.nbt.annotation.RootName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.kyori.adventure.bossbar.BossBar;
import net.thenextlvl.worlds.data.*;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;

import java.util.Set;

@Getter
@Setter
@ToString
@RootName("Data")
@Accessors(chain = true)
public class WorldData {
    private @SerializedName("BorderCenterX") double borderCenterX = 0d;
    private @SerializedName("BorderCenterZ") double borderCenterZ = 0d;
    private @SerializedName("BorderDamagePerBlock") double borderDamagePerBlock = 0.2d;
    private @SerializedName("BorderSafeZone") double borderSafeZone = 5d;
    private @SerializedName("BorderSize") double borderSize = 59999968d;
    private @SerializedName("BorderSizeLerpTarget") double borderSizeLerpTarget = 59999968d;
    private @SerializedName("BorderSizeLerpTime") long borderSizeLerpTime = 0L;
    private @SerializedName("BorderWarningBlocks") double borderWarningBlocks = 5d;
    private @SerializedName("BorderWarningTime") double borderWarningTime = 15d;
    private @SerializedName("CustomBossEvents") KeyMap<BossBar> bossBars = new KeyMap<>();
    private @SerializedName("DataPacks") DataPacks dataPacks = new DataPacks(
            Set.of("bundle"),
            Set.of("vanilla")
    );
    private @SerializedName("DataVersion") int dataVersion = 3465;
    private @SerializedName("DayTime") long dayTime = 0L;
    private @SerializedName("Difficulty") Difficulty difficulty = Difficulty.NORMAL;
    private @SerializedName("DifficultyLocked") boolean difficultyLocked = false;
    private @SerializedName("DragonFight") DragonFight dragonFight = new DragonFight();
    private @SerializedName("GameRules") GameRules gameRules = new GameRules();
    private @SerializedName("GameType") GameMode gameType = GameMode.SURVIVAL;
    private @SerializedName("LastPlayed") long lastPlayed = 0L;
    private @SerializedName("LevelName") String levelName = "world";
    private @SerializedName("ScheduledEvents") KeyMap<ScheduledEvent> scheduledEvents = new KeyMap<>();
    private @SerializedName("ServerBrands") Set<String> serverBrands = Set.of("vanilla");
    private @SerializedName("SpawnAngle") float spawnAngle = 0f;
    private @SerializedName("SpawnX") int spawnX = 0;
    private @SerializedName("SpawnY") int spawnY = 70;
    private @SerializedName("SpawnZ") int spawnZ = 0;
    private @SerializedName("Time") long time = 0L;
    private @SerializedName("Version") Version version = new Version(3465, "1.20.1", "main", false);
    private @SerializedName("WanderingTraderSpawnChance") int wanderingTraderSpawnChance = 25;
    private @SerializedName("WanderingTraderSpawnDelay") int wanderingTraderSpawnDelay = 24000;
    private @SerializedName("WasModded") boolean wasModded = false;
    private @SerializedName("WorldGenSettings") GeneratorSettings worldGenSettings = new GeneratorSettings();
    private @SerializedName("allowCommands") boolean allowCheats = false;
    private @SerializedName("clearWeatherTime") int clearWeatherTime = 0;
    private @SerializedName("hardcore") boolean hardcore = false;
    private @SerializedName("initialized") boolean initialized = false;
    private @SerializedName("rainTime") int rainTime = 0;
    private @SerializedName("raining") boolean raining = false;
    private @SerializedName("thunderTime") int thunderTime = 0;
    private @SerializedName("thundering") boolean thundering = false;
    private @SerializedName("version") int nbtVersion = 19133;
}
