package net.thenextlvl.perworlds.model;

import com.google.common.base.Preconditions;
import net.kyori.adventure.util.TriState;
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
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spigotmc.SpigotConfig;

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
    private static final Set<AttributeData> DEFAULT_ATTRIBUTES = defaultAttributes();
    private static final TriState DEFAULT_FLYING = TriState.NOT_SET;
    private static final TriState DEFAULT_MAY_FLY = TriState.NOT_SET;
    private static final TriState DEFAULT_VISUAL_FIRE = TriState.NOT_SET;
    private static final Vector DEFAULT_VELOCITY = new Vector(0, 0, 0);
    private static final WardenSpawnTracker DEFAULT_WARDEN_SPAWN_TRACKER = new PaperWardenSpawnTracker();
    private static final boolean DEFAULT_GLIDING = false;
    private static final boolean DEFAULT_INVULNERABLE = false;
    private static final boolean DEFAULT_LOCK_FREEZE_TICKS = false;
    private static final boolean DEFAULT_SEEN_CREDITS = false;
    private static final double DEFAULT_ABSORPTION = 0;
    private static final double DEFAULT_HEALTH = 20;
    private static final float DEFAULT_EXHAUSTION = 0;
    private static final float DEFAULT_EXPERIENCE = 0;
    private static final float DEFAULT_FALL_DISTANCE = 0;
    private static final float DEFAULT_FLY_SPEED = 0.1F;
    private static final float DEFAULT_SATURATION = 10;
    private static final float DEFAULT_WALK_SPEED = 0.2F;
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
    private List<PotionEffect> potionEffects = List.of();
    private Set<AdvancementData> advancements = Set.of();
    private Set<AttributeData> attributes = DEFAULT_ATTRIBUTES;
    private Set<NamespacedKey> recipes = Set.of();
    private Stats stats = new PaperStats();
    private TriState flying = DEFAULT_FLYING;
    private TriState mayFly = DEFAULT_MAY_FLY;
    private TriState visualFire = DEFAULT_VISUAL_FIRE;
    private Vector velocity = DEFAULT_VELOCITY;
    private WardenSpawnTracker wardenSpawnTracker = DEFAULT_WARDEN_SPAWN_TRACKER;
    private boolean gliding = DEFAULT_GLIDING;
    private boolean invulnerable = DEFAULT_INVULNERABLE;
    private boolean lockFreezeTicks = DEFAULT_LOCK_FREEZE_TICKS;
    private boolean seenCredits = DEFAULT_SEEN_CREDITS;
    private double absorption = DEFAULT_ABSORPTION;
    private double health = DEFAULT_HEALTH;
    private float exhaustion = DEFAULT_EXHAUSTION;
    private float experience = DEFAULT_EXPERIENCE;
    private float fallDistance = DEFAULT_FALL_DISTANCE;
    private float flySpeed = DEFAULT_FLY_SPEED;
    private float saturation = DEFAULT_SATURATION;
    private float walkSpeed = DEFAULT_WALK_SPEED;
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

    private @Nullable WorldGroup group;

    public PaperPlayerData(@Nullable WorldGroup group) {
        this.group = group;
    }

    public static PaperPlayerData of(Player player, WorldGroup group) {
        return new PaperPlayerData(group)
                .attributes(collectAttributes(player))
                .advancements(collectAdvancements(player))
                // todo: replace - https://github.com/PaperMC/Paper/pull/12826
                .invulnerable(((CraftPlayer) player).getHandle().isInvulnerable())
                .portalCooldown(player.getPortalCooldown())
                .gliding(player.isGliding())
                .wardenSpawnTracker(PaperWardenSpawnTracker.of(player))
                .lastDeathLocation(player.getLastDeathLocation())
                .lastLocation(player.getLocation())
                .velocity(player.getVelocity())
                .lockFreezeTicks(player.isFreezeTickingLocked())
                .visualFire(player.getVisualFire())
                .previousGameMode(player.getPreviousGameMode())
                .flying(TriState.byBoolean(player.isFlying()))
                .mayFly(TriState.byBoolean(player.getAllowFlight()))
                .enderChest(player.getEnderChest().getContents())
                .inventory(player.getInventory().getContents())
                .respawnLocation(player.getRespawnLocation(false))
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
                .walkSpeed(player.getWalkSpeed())
                .flySpeed(player.getFlySpeed())
                .beeStingersInBody(player.getBeeStingersInBody())
                .fireTicks(player.getFireTicks())
                .foodLevel(player.getFoodLevel())
                .freezeTicks(player.getFreezeTicks())
                .heldItemSlot(player.getInventory().getHeldItemSlot())
                .level(player.getLevel())
                .remainingAir(player.getRemainingAir())
                .score(player.getDeathScreenScore());
    }

    private static Set<AttributeData> collectAttributes(Player player) {
        return Registry.ATTRIBUTE.stream()
                .map(player::getAttribute)
                .filter(Objects::nonNull)
                .map(PaperAttributeData::new)
                .collect(Collectors.toSet());
    }

    private static Set<AdvancementData> collectAdvancements(Player player) {
        if (SpigotConfig.disableAdvancementSaving) return Set.of();
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(player.getServer().advancementIterator(), 0), false)
                .map(advancement -> new PaperAdvancementData(player.getAdvancementProgress(advancement)))
                .filter(AdvancementData::shouldSerialize)
                .collect(Collectors.toSet());
    }

    @Override
    public CompletableFuture<Boolean> load(Player player, boolean position) {
        if (group == null) return CompletableFuture.failedFuture(new IllegalStateException(
                "Player data has not been finalized yet"
        ));

        var settings = group.getSettings();
        if (!settings.enabled()) return CompletableFuture.completedFuture(false);
        if (!position && group.containsWorld(player.getWorld())) {
            load(player, group);
            return CompletableFuture.completedFuture(true);
        } else if (!position) return CompletableFuture.failedFuture(new IllegalStateException(
                "Failed to load player data: World mismatch between group '%s' and player '%s'. Expected any of %s but got %s"
                        .formatted(group.getName(), player.getName(), group.getPersistedWorlds(), player.getWorld().key())
        ));
        var location = group.getSpawnLocation(this).orElse(null);
        if (location == null) return CompletableFuture.completedFuture(false);
        if (player.isDead()) {
            // this partly prevents a dupe exploit :)
            // Players can't be teleported while dead, so we have to revive them to be able to load the data
            var attribute = player.getAttribute(Attribute.MAX_HEALTH);
            player.setHealth(attribute != null ? attribute.getValue() : 20);
        }
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

        var defaultGameMode = group.getGroupData().getDefaultGameMode().orElseGet(() -> player.getServer().getDefaultGameMode());
        player.setGameMode(settings.gameMode() && previousGameMode != null ? previousGameMode : defaultGameMode);
        player.setGameMode(settings.gameMode() && gameMode != null ? gameMode : defaultGameMode);

        player.setAllowFlight((settings.flyState() ? mayFly : DEFAULT_MAY_FLY)
                .toBooleanOrElseGet(() -> player.getGameMode().isInvulnerable()));
        player.setFlying((settings.flyState() ? flying : DEFAULT_FLYING)
                .toBooleanOrElseGet(() -> player.getGameMode().equals(GameMode.SPECTATOR)));

        applyAttributes(player, settings);

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

        var attribute = player.getAttribute(Attribute.MAX_HEALTH);
        var maxHealth = attribute != null ? attribute.getValue() : 20;
        player.setHealth(Math.clamp(settings.health() ? health : DEFAULT_HEALTH, 0, maxHealth));

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

        player.setFlySpeed(Math.clamp(settings.flySpeed() ? flySpeed : DEFAULT_FLY_SPEED, -1, 1));
        player.setWalkSpeed(Math.clamp(settings.walkSpeed() ? walkSpeed : DEFAULT_WALK_SPEED, -1, 1));

        player.clearActivePotionEffects();
        if (settings.potionEffects()) player.addPotionEffects(potionEffects);

        var tracker = settings.wardenSpawnTracker() ? wardenSpawnTracker : DEFAULT_WARDEN_SPAWN_TRACKER;
        player.setWardenTimeSinceLastWarning(tracker.ticksSinceLastWarning());
        player.setWardenWarningCooldown(tracker.cooldownTicks());
        player.setWardenWarningLevel(tracker.warningLevel());

        if (settings.statistics()) stats.apply(player);
        else stats.clear(player);

        updateTablistVisibility(player, group);

        applyAdvancements(player, settings);
        applyRecipes(player, settings);
    }

    private void applyAttributes(Player player, GroupSettings settings) {
        if (settings.attributes()) attributes.forEach(data -> data.apply(player));
        else DEFAULT_ATTRIBUTES.forEach(data -> data.apply(player));
    }

    private void updateTablistVisibility(Player player, WorldGroup group) {
        player.getServer().getOnlinePlayers().forEach(other -> {
            if (player.equals(other)) return;
            var otherGroup = player.getWorld().equals(other.getWorld()) ? group
                    : group.getGroupProvider().getGroup(other.getWorld())
                    .orElse(group.getGroupProvider().getUnownedWorldGroup());
            if (otherGroup.equals(group)) {
                if (other.canSee(player)) other.listPlayer(player);
                if (player.canSee(other)) player.listPlayer(other);
            } else {
                if (otherGroup.getSettings().tabList()) other.unlistPlayer(player);
                if (group.getSettings().tabList()) player.unlistPlayer(other);
            }
        });
    }

    private void applyRecipes(Player player, GroupSettings settings) {
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

    public PaperPlayerData group(WorldGroup group) {
        Preconditions.checkState(this.group == null, "Player data has already been finalized");
        if (respawnLocation != null && !group.containsWorld(respawnLocation.getWorld())) respawnLocation = null;
        if (lastDeathLocation != null && !group.containsWorld(lastDeathLocation.getWorld())) lastDeathLocation = null;
        if (lastLocation != null && !group.containsWorld(lastLocation.getWorld())) lastLocation = null;
        this.group = group;
        return this;
    }

    @Override
    public WorldGroup group() {
        Preconditions.checkState(group != null, "Player data has not been finalized yet");
        return group;
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
    public PaperPlayerData flySpeed(@Range(from = -1, to = 1) float speed) {
        Preconditions.checkArgument(speed >= -1 && speed <= 1, "Speed must be between -1 and 1");
        this.flySpeed = speed;
        return this;
    }

    @Override
    public PaperPlayerData flying(TriState flying) {
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
        Preconditions.checkArgument(health >= 0, "Health must be greater than or equal to 0");
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
        this.lastDeathLocation = location == null || group == null || group.containsWorld(location.getWorld()) ? location : null;
        return this;
    }

    @Override
    public PaperPlayerData lastLocation(@Nullable Location location) {
        this.lastLocation = location == null || group == null || group.containsWorld(location.getWorld()) ? location : null;
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
    public PaperPlayerData visualFire(TriState visualFire) {
        this.visualFire = visualFire;
        return this;
    }

    @Override
    public PaperPlayerData walkSpeed(@Range(from = -1, to = 1) float speed) {
        Preconditions.checkArgument(speed >= -1 && speed <= 1, "Speed must be between -1 and 1");
        this.walkSpeed = speed;
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
    public PaperPlayerData mayFly(TriState mayFly) {
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
        this.respawnLocation = location == null || group == null || group.containsWorld(location.getWorld()) ? location : null;
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
    public TriState flying() {
        return flying;
    }

    @Override
    public TriState mayFly() {
        return mayFly;
    }

    @Override
    public TriState visualFire() {
        return visualFire;
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
    public boolean seenCredits() {
        return seenCredits;
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
    public float flySpeed() {
        return flySpeed;
    }

    @Override
    public float saturation() {
        return saturation;
    }

    @Override
    public float walkSpeed() {
        return flySpeed;
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

    private static Set<AttributeData> defaultAttributes() {
        var defaults = EntityType.PLAYER.getDefaultAttributes();
        return Registry.ATTRIBUTE.stream()
                .map(defaults::getAttribute)
                .filter(Objects::nonNull)
                .map(PaperAttributeData::new)
                .collect(Collectors.toSet());
    }
}
