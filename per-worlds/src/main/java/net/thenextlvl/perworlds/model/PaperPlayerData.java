package net.thenextlvl.perworlds.model;

import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.WorldGroup;
import net.thenextlvl.perworlds.data.AdvancementData;
import net.thenextlvl.perworlds.data.AttributeData;
import net.thenextlvl.perworlds.data.PlayerData;
import net.thenextlvl.perworlds.data.WardenSpawnTracker;
import net.thenextlvl.perworlds.statistics.Stats;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@NullMarked
public class PaperPlayerData implements PlayerData {
    private static final @Nullable ItemStack[] DEFAULT_ENDERCHEST = new ItemStack[27];
    private static final @Nullable ItemStack[] DEFAULT_INVENTORY = new ItemStack[40];
    private static final @Nullable Location DEFAULT_LAST_DEATH_LOCATION = null;
    private static final @Nullable Location DEFAULT_LAST_LOCATION = null;
    private static final @Nullable Location DEFAULT_RESPAWN_LOCATION = null;
    private static final Vector DEFAULT_VELOCITY = new Vector(0, 0, 0);
    private static final WardenSpawnTracker DEFAULT_WARDEN_SPAWN_TRACKER = new PaperWardenSpawnTracker();
    private static final boolean DEFAULT_FLYING = false;
    private static final boolean DEFAULT_GLIDING = false;
    private static final boolean DEFAULT_INVULNERABLE = false;
    private static final boolean DEFAULT_LOCK_FREEZE_TICKS = false;
    private static final boolean DEFAULT_MAY_FLY = false;
    private static final boolean DEFAULT_SEEN_CREDITS = false;
    private static final boolean DEFAULT_VISUAL_FIRE = false;
    private static final double DEFAULT_ABSORPTION = 0;
    private static final double DEFAULT_HEALTH = 20;
    private static final float DEFAULT_EXHAUSTION = 0;
    private static final float DEFAULT_EXPERIENCE = 0;
    private static final float DEFAULT_FALL_DISTANCE = 0;
    private static final float DEFAULT_SATURATION = 10;
    private static final int DEFAULT_ARROWS_IN_BODY = 0;
    private static final int DEFAULT_BEE_STINGERS_IN_BODY = 0;
    private static final int DEFAULT_FIRE_TICKS = 0;
    private static final int DEFAULT_FOOD_LEVEL = 20;
    private static final int DEFAULT_FREEZE_TICKS = 0;
    private static final int DEFAULT_HELD_ITEM_SLOT = 0;
    private static final int DEFAULT_LEVEL = 0;
    private static final int DEFAULT_PORTAL_COOLDOWN = 0;
    private static final int DEFAULT_REMAINING_AIR = 300;
    private static final int DEFAULT_SCORE = 0;

    private @Nullable GameMode gameMode = null;
    private @Nullable GameMode previousGameMode = null;
    private @Nullable ItemStack[] enderChest = DEFAULT_ENDERCHEST;
    private @Nullable ItemStack[] inventory = DEFAULT_INVENTORY;
    private @Nullable Location lastDeathLocation = DEFAULT_LAST_DEATH_LOCATION;
    private @Nullable Location lastLocation = DEFAULT_LAST_LOCATION;
    private @Nullable Location respawnLocation = DEFAULT_RESPAWN_LOCATION;
    private GameMode defaultGameMode = GameMode.SURVIVAL;
    private List<PotionEffect> potionEffects = List.of();
    private Set<AdvancementData> advancements = Set.of();
    private Set<AttributeData> attributes = Set.of();
    private Set<NamespacedKey> recipes = Set.of();
    private Stats stats = new PaperStats();
    private Vector velocity = DEFAULT_VELOCITY;
    private WardenSpawnTracker wardenSpawnTracker = DEFAULT_WARDEN_SPAWN_TRACKER;
    private boolean flying = DEFAULT_FLYING;
    private boolean gliding = DEFAULT_GLIDING;
    private boolean invulnerable = DEFAULT_INVULNERABLE;
    private boolean lockFreezeTicks = DEFAULT_LOCK_FREEZE_TICKS;
    private boolean mayFly = DEFAULT_MAY_FLY;
    private boolean seenCredits = DEFAULT_SEEN_CREDITS;
    private boolean visualFire = DEFAULT_VISUAL_FIRE;
    private double absorption = DEFAULT_ABSORPTION;
    private double health = DEFAULT_HEALTH;
    private float exhaustion = DEFAULT_EXHAUSTION;
    private float experience = DEFAULT_EXPERIENCE;
    private float fallDistance = DEFAULT_FALL_DISTANCE;
    private float saturation = DEFAULT_SATURATION;
    private int arrowsInBody = DEFAULT_ARROWS_IN_BODY;
    private int beeStingersInBody = DEFAULT_BEE_STINGERS_IN_BODY;
    private int fireTicks = DEFAULT_FIRE_TICKS;
    private int foodLevel = DEFAULT_FOOD_LEVEL;
    private int freezeTicks = DEFAULT_FREEZE_TICKS;
    private int heldItemSlot = DEFAULT_HELD_ITEM_SLOT;
    private int level = DEFAULT_LEVEL;
    private int portalCooldown = DEFAULT_PORTAL_COOLDOWN;
    private int remainingAir = DEFAULT_REMAINING_AIR;
    private int score = DEFAULT_SCORE;

    public static PaperPlayerData of(Player player, WorldGroup group) {
        return new PaperPlayerData()
                .defaultGameMode(group.getGroupData().defaultGameMode())
                .attributes(Registry.ATTRIBUTE.stream()
                        .map(player::getAttribute)
                        .filter(Objects::nonNull)
                        .map(PaperAttributeData::new)
                        .collect(Collectors.toSet()))
                .advancements(StreamSupport.stream(Spliterators.spliteratorUnknownSize(player.getServer().advancementIterator(), 0), false)
                        .map(advancement -> new PaperAdvancementData(player.getAdvancementProgress(advancement)))
                        .filter(AdvancementData::shouldSerialize)
                        .collect(Collectors.toSet()))
                .invulnerable(player.isInvulnerable())
                .portalCooldown(player.getPortalCooldown())
                .gliding(player.isGliding())
                .wardenSpawnTracker(PaperWardenSpawnTracker.of(player))
                .lastDeathLocation(player.getLastDeathLocation())
                .lastLocation(player.getLocation())
                .velocity(player.getVelocity())
                .lockFreezeTicks(player.isFreezeTickingLocked())
                .visualFire(player.isVisualFire())
                .previousGameMode(player.getPreviousGameMode())
                .flying(player.isFlying())
                .mayFly(player.getAllowFlight())
                .enderChest(player.getEnderChest().getContents())
                .inventory(player.getInventory().getContents())
                .respawnLocation(player.getPotentialRespawnLocation())
                .potionEffects(player.getActivePotionEffects())
                .gameMode(player.getGameMode())
                .stats(PaperStats.of(player))
                .discoveredRecipes(player.getDiscoveredRecipes())
                .seenCredits(player.hasSeenWinScreen())
                .absorption(player.getAbsorptionAmount())
                .health(player.getHealth())
                .exhaustion(player.getExhaustion())
                .fallDistance(player.getFallDistance())
                .experience(player.getExp())
                .saturation(player.getSaturation())
                .arrowsInBody(player.getArrowsInBody())
                .beeStingersInBody(player.getBeeStingersInBody())
                .fireTicks(player.getFireTicks())
                .foodLevel(player.getFoodLevel())
                .freezeTicks(player.getFreezeTicks())
                .heldItemSlot(player.getInventory().getHeldItemSlot())
                .level(player.getLevel())
                .remainingAir(player.getRemainingAir())
                .score(player.getDeathScreenScore());
    }

    @Override
    public CompletableFuture<Boolean> load(Player player, WorldGroup group, boolean position) {
        var settings = group.getSettings();
        if (!position && group.containsWorld(player.getWorld())) {
            load(player, group);
            return CompletableFuture.completedFuture(true);
        } else if (!position) {
            var exception = new IllegalStateException("Cannot load player data while groups don't match");
            return CompletableFuture.failedFuture(exception);
        }

        var location = group.getSpawnLocation(this).orElse(null);
        if (location == null) return CompletableFuture.completedFuture(false);
        return player.teleportAsync(location).thenApply(success -> {
            if (!success) return false;
            player.setFallDistance(settings.fallDistance() ? fallDistance : DEFAULT_FALL_DISTANCE);
            player.setVelocity(settings.velocity() ? velocity : DEFAULT_VELOCITY);
            load(player, group);
            return true;
        });
    }

    private void load(Player player, WorldGroup group) {
        var settings = group.getSettings();

        player.setGameMode(settings.gameMode() && previousGameMode != null ? previousGameMode : defaultGameMode());
        player.setGameMode(settings.gameMode() && gameMode != null ? gameMode : defaultGameMode());

        player.setAllowFlight(settings.flyState() ? mayFly : DEFAULT_MAY_FLY);
        player.setFlying(settings.flyState() ? flying : DEFAULT_FLYING);

        player.setGliding(settings.gliding() ? gliding : DEFAULT_GLIDING);

        player.setPortalCooldown(settings.portalCooldown() ? portalCooldown : DEFAULT_PORTAL_COOLDOWN);

        player.getInventory().setHeldItemSlot(settings.hotbarSlot() ? heldItemSlot : DEFAULT_HELD_ITEM_SLOT);
        player.getInventory().setContents(settings.inventory() ? inventory : DEFAULT_INVENTORY);
        player.getEnderChest().setContents(settings.enderChest() ? enderChest : DEFAULT_ENDERCHEST);

        player.setHasSeenWinScreen(settings.endCredits() ? seenCredits : DEFAULT_SEEN_CREDITS);

        player.setArrowsInBody(settings.arrowsInBody() ? arrowsInBody : DEFAULT_ARROWS_IN_BODY);
        player.setBeeStingersInBody(settings.beeStingersInBody() ? beeStingersInBody : DEFAULT_BEE_STINGERS_IN_BODY);

        player.setDeathScreenScore(settings.score() ? score : DEFAULT_SCORE);
        player.setExp(settings.experience() ? experience : DEFAULT_EXPERIENCE);
        player.setLevel(settings.experience() ? level : DEFAULT_LEVEL);

        player.setInvulnerable(settings.invulnerable() ? invulnerable : DEFAULT_INVULNERABLE);
        player.setHealth(settings.health() ? health : DEFAULT_HEALTH);

        player.setAbsorptionAmount(settings.absorption() ? absorption : DEFAULT_ABSORPTION);
        player.setExhaustion(settings.exhaustion() ? exhaustion : DEFAULT_EXHAUSTION);
        player.setFoodLevel(settings.foodLevel() ? foodLevel : DEFAULT_FOOD_LEVEL);
        player.setSaturation(settings.saturation() ? saturation : DEFAULT_SATURATION);

        player.setFireTicks(settings.fireTicks() ? fireTicks : DEFAULT_FIRE_TICKS);
        player.setFreezeTicks(settings.freezeTicks() ? freezeTicks : DEFAULT_FREEZE_TICKS);
        player.lockFreezeTicks(settings.lockFreezeTicks() ? lockFreezeTicks : DEFAULT_LOCK_FREEZE_TICKS);
        player.setRemainingAir(settings.remainingAir() ? remainingAir : DEFAULT_REMAINING_AIR);

        player.setVisualFire(settings.visualFire() ? visualFire : DEFAULT_VISUAL_FIRE);

        player.setLastDeathLocation(settings.lastDeathLocation() ? lastDeathLocation : DEFAULT_LAST_DEATH_LOCATION);
        player.setRespawnLocation(settings.respawnLocation() ? respawnLocation : DEFAULT_RESPAWN_LOCATION, false);

        player.clearActivePotionEffects();
        if (settings.potionEffects()) player.addPotionEffects(potionEffects);

        if (settings.attributes()) attributes.forEach(data -> {
            var attribute = player.getAttribute(data.attribute());
            if (attribute != null) attribute.setBaseValue(data.baseValue());
        });
        // todo: restore real default value
        // else Registry.ATTRIBUTE.forEach(type -> {
        //     var attribute = player.getAttribute(type);
        //     if (attribute != null) attribute.setBaseValue(attribute.getBaseValue());
        // });

        var tracker = settings.wardenSpawnTracker() ? wardenSpawnTracker : DEFAULT_WARDEN_SPAWN_TRACKER;
        player.setWardenTimeSinceLastWarning(tracker.ticksSinceLastWarning());
        player.setWardenWarningCooldown(tracker.cooldownTicks());
        player.setWardenWarningLevel(tracker.warningLevel());

        if (settings.statistics()) stats.apply(player);
        else stats.clear(player);

        applyAdvancements(player, settings);
        applyRecipes(player, settings);
    }

    private void applyRecipes(Player player, GroupSettings settings) {
        // todo: only (un)discover recipes internally
        if (settings.recipes()) {
            var toAdd = new HashSet<>(recipes);
            toAdd.removeAll(player.getDiscoveredRecipes());
            player.discoverRecipes(toAdd);

            var toRemove = new HashSet<>(player.getDiscoveredRecipes());
            toRemove.removeAll(recipes);
            player.undiscoverRecipes(toRemove);
        } else player.undiscoverRecipes(player.getDiscoveredRecipes());
    }

    private void applyAdvancements(Player player, GroupSettings settings) {
        // todo: only grant advancements internally
        var toRemove = StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                player.getServer().advancementIterator(), 0
        ), false).collect(Collectors.toSet());

        if (settings.advancements()) toRemove.removeAll(advancements.stream()
                .map(AdvancementData::getAdvancement)
                .collect(Collectors.toSet()));

        toRemove.forEach(advancement -> {
            var progress = player.getAdvancementProgress(advancement);
            progress.getAwardedCriteria().forEach(progress::revokeCriteria);
        });

        if (settings.advancements()) advancements.forEach(data -> {
            var progress = player.getAdvancementProgress(data.getAdvancement());
            data.getAwardedCriteria().forEach(progress::awardCriteria);
            data.getRemainingCriteria().forEach(progress::revokeCriteria);
        });
    }

    @Override
    public @Nullable ItemStack[] enderChest() {
        return enderChest;
    }

    @Override
    public @Nullable ItemStack[] inventory() {
        return inventory;
    }

    @Override
    public @Unmodifiable List<PotionEffect> potionEffects() {
        return potionEffects;
    }

    @Override
    public GameMode defaultGameMode() {
        return defaultGameMode;
    }

    @Override
    public @Nullable GameMode gameMode() {
        return gameMode;
    }

    @Override
    public @Nullable GameMode previousGameMode() {
        return previousGameMode;
    }

    @Override
    public @Nullable Location lastDeathLocation() {
        return lastDeathLocation;
    }

    @Override
    public @Nullable Location lastLocation() {
        return lastLocation;
    }

    @Override
    public @Nullable Location respawnLocation() {
        return respawnLocation;
    }

    @Override
    public PaperPlayerData absorption(double absorption) {
        this.absorption = absorption;
        return this;
    }

    @Override
    public PaperPlayerData advancements(Collection<AdvancementData> advancements) {
        this.advancements = Set.copyOf(advancements);
        return this;
    }

    @Override
    public PaperPlayerData arrowsInBody(int arrowsInBody) {
        this.arrowsInBody = arrowsInBody;
        return this;
    }

    @Override
    public PaperPlayerData attributes(Collection<AttributeData> attributes) {
        this.attributes = Set.copyOf(attributes);
        return this;
    }

    @Override
    public PaperPlayerData beeStingersInBody(int beeStingersInBody) {
        this.beeStingersInBody = beeStingersInBody;
        return this;
    }

    @Override
    public PaperPlayerData defaultGameMode(GameMode gameMode) {
        this.defaultGameMode = gameMode;
        return this;
    }

    @Override
    public PaperPlayerData discoveredRecipes(Collection<NamespacedKey> recipes) {
        this.recipes = Set.copyOf(recipes);
        return this;
    }

    @Override
    public PaperPlayerData enderChest(@Nullable ItemStack[] contents) {
        this.enderChest = contents;
        return this;
    }

    @Override
    public PaperPlayerData exhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
        return this;
    }

    @Override
    public PaperPlayerData experience(float experience) {
        this.experience = experience;
        return this;
    }

    @Override
    public PaperPlayerData fallDistance(float fallDistance) {
        this.fallDistance = fallDistance;
        return this;
    }

    @Override
    public PaperPlayerData fireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
        return this;
    }

    @Override
    public PaperPlayerData flying(boolean flying) {
        this.flying = flying;
        return this;
    }

    @Override
    public PaperPlayerData foodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
        return this;
    }

    @Override
    public PaperPlayerData freezeTicks(int freezeTicks) {
        this.freezeTicks = freezeTicks;
        return this;
    }

    @Override
    public PaperPlayerData gameMode(@Nullable GameMode gameMode) {
        this.gameMode = gameMode;
        return this;
    }

    @Override
    public PaperPlayerData gliding(boolean gliding) {
        this.gliding = gliding;
        return this;
    }

    @Override
    public PaperPlayerData health(double health) {
        this.health = health;
        return this;
    }

    @Override
    public PaperPlayerData heldItemSlot(int heldItemSlot) {
        this.heldItemSlot = heldItemSlot;
        return this;
    }

    @Override
    public PaperPlayerData inventory(@Nullable ItemStack[] contents) {
        this.inventory = contents;
        return this;
    }

    @Override
    public PaperPlayerData invulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
        return this;
    }

    @Override
    public PaperPlayerData lastDeathLocation(@Nullable Location location) {
        this.lastDeathLocation = location;
        return this;
    }

    @Override
    public PaperPlayerData lastLocation(@Nullable Location location) {
        this.lastLocation = location;
        return this;
    }

    @Override
    public PaperPlayerData seenCredits(boolean seenCredits) {
        this.seenCredits = seenCredits;
        return this;
    }

    @Override
    public PaperPlayerData stats(Stats stats) {
        this.stats = stats;
        return this;
    }

    @Override
    public PaperPlayerData velocity(Vector velocity) {
        this.velocity = velocity;
        return this;
    }

    @Override
    public PaperPlayerData visualFire(boolean visualFire) {
        this.visualFire = visualFire;
        return this;
    }

    @Override
    public PaperPlayerData wardenSpawnTracker(WardenSpawnTracker tracker) {
        this.wardenSpawnTracker = tracker;
        return this;
    }

    @Override
    public @Unmodifiable Set<AdvancementData> advancements() {
        return Set.copyOf(advancements);
    }

    @Override
    public PaperPlayerData level(int level) {
        this.level = level;
        return this;
    }

    @Override
    public PaperPlayerData lockFreezeTicks(boolean lockFreezeTicks) {
        this.lockFreezeTicks = lockFreezeTicks;
        return this;
    }

    @Override
    public PaperPlayerData mayFly(boolean mayFly) {
        this.mayFly = mayFly;
        return this;
    }

    @Override
    public PaperPlayerData portalCooldown(int cooldown) {
        this.portalCooldown = cooldown;
        return this;
    }

    @Override
    public PaperPlayerData potionEffects(Collection<PotionEffect> effects) {
        this.potionEffects = List.copyOf(effects);
        return this;
    }

    @Override
    public PaperPlayerData previousGameMode(@Nullable GameMode gameMode) {
        this.previousGameMode = gameMode;
        return this;
    }

    @Override
    public PaperPlayerData remainingAir(int remainingAir) {
        this.remainingAir = remainingAir;
        return this;
    }

    @Override
    public PaperPlayerData respawnLocation(@Nullable Location location) {
        this.respawnLocation = location;
        return this;
    }

    @Override
    public PaperPlayerData saturation(float saturation) {
        this.saturation = saturation;
        return this;
    }

    @Override
    public PaperPlayerData score(int score) {
        this.score = score;
        return this;
    }

    @Override
    public @Unmodifiable Set<AttributeData> attributes() {
        return attributes;
    }

    @Override
    public @Unmodifiable Set<NamespacedKey> discoveredRecipes() {
        return recipes;
    }

    @Override
    public Stats stats() {
        return stats;
    }

    @Override
    public Vector velocity() {
        return velocity;
    }

    @Override
    public WardenSpawnTracker wardenSpawnTracker() {
        return wardenSpawnTracker;
    }

    @Override
    public boolean flying() {
        return flying;
    }

    @Override
    public boolean gliding() {
        return gliding;
    }

    @Override
    public boolean invulnerable() {
        return invulnerable;
    }

    @Override
    public boolean lockFreezeTicks() {
        return lockFreezeTicks;
    }

    @Override
    public boolean mayFly() {
        return mayFly;
    }

    @Override
    public boolean seenCredits() {
        return seenCredits;
    }

    @Override
    public boolean visualFire() {
        return visualFire;
    }

    @Override
    public double absorption() {
        return absorption;
    }

    @Override
    public double health() {
        return health;
    }

    @Override
    public float exhaustion() {
        return exhaustion;
    }

    @Override
    public float experience() {
        return experience;
    }

    @Override
    public float fallDistance() {
        return fallDistance;
    }

    @Override
    public float saturation() {
        return saturation;
    }

    @Override
    public int arrowsInBody() {
        return arrowsInBody;
    }

    @Override
    public int beeStingersInBody() {
        return beeStingersInBody;
    }

    @Override
    public int fireTicks() {
        return fireTicks;
    }

    @Override
    public int foodLevel() {
        return foodLevel;
    }

    @Override
    public int freezeTicks() {
        return freezeTicks;
    }

    @Override
    public int heldItemSlot() {
        return heldItemSlot;
    }

    @Override
    public int level() {
        return level;
    }

    @Override
    public int portalCooldown() {
        return portalCooldown;
    }

    @Override
    public int remainingAir() {
        return remainingAir;
    }

    @Override
    public int score() {
        return score;
    }
}
