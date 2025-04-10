package net.thenextlvl.perworlds.model;

import net.thenextlvl.perworlds.GroupSettings;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@NullMarked
public class PaperPlayerData implements PlayerData {
    private @Nullable GameMode previousGameMode = null;
    private @Nullable ItemStack[] enderChest = new ItemStack[27];
    private @Nullable ItemStack[] inventory = new ItemStack[40];
    private @Nullable Location lastDeathLocation = null;
    private @Nullable Location lastLocation = null;
    private @Nullable Location respawnLocation = null;
    private GameMode gameMode = GameMode.SURVIVAL;
    private List<PotionEffect> potionEffects = List.of();
    private Set<AdvancementData> advancements = Set.of();
    private Set<AttributeData> attributes = Set.of();
    private Set<NamespacedKey> recipes = Set.of();
    private Stats stats = new PaperStats();
    private Vector velocity = new Vector(0, 0, 0);
    private WardenSpawnTracker wardenSpawnTracker = new PaperWardenSpawnTracker();
    private boolean flying = false;
    private boolean gliding = false;
    private boolean invulnerable = false;
    private boolean lockFreezeTicks = false;
    private boolean mayFly = false;
    private boolean seenCredits = false;
    private boolean visualFire = false;
    private double absorption = 0;
    private double health = 20;
    private float exhaustion = 0;
    private float experience = 0;
    private float fallDistance = 0;
    private float saturation = 10;
    private int arrowsInBody = 0;
    private int beeStingersInBody = 0;
    private int fireTicks = 0;
    private int foodLevel = 20;
    private int freezeTicks = 0;
    private int heldItemSlot = 0;
    private int level = 0;
    private int portalCooldown = 0;
    private int remainingAir = 300;
    private int score = 0;

    public static PaperPlayerData of(Player player) {
        return new PaperPlayerData()
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
    public void apply(GroupSettings settings, Player player, boolean position) {
        if (settings.lastLocation() && position && lastLocation != null) player.teleport(lastLocation); // todo: make async properly
        if (settings.fallDistance() && position) player.setFallDistance(fallDistance);
        if (settings.velocity() && position) player.setVelocity(velocity);

        if (settings.gameMode()) {
            if (previousGameMode != null) player.setGameMode(previousGameMode);
            player.setGameMode(gameMode);
        }

        if (settings.flyState()) {
            player.setAllowFlight(mayFly);
            player.setFlying(flying);
        }

        if (settings.gliding()) player.setGliding(gliding);

        if (settings.portalCooldown()) player.setPortalCooldown(portalCooldown);

        if (settings.hotbarSlot()) player.getInventory().setHeldItemSlot(heldItemSlot);
        if (settings.inventory()) player.getInventory().setContents(inventory);
        if (settings.enderChest()) player.getEnderChest().setContents(enderChest);

        if (settings.endCredits()) player.setHasSeenWinScreen(seenCredits);

        if (settings.arrowsInBody()) player.setArrowsInBody(arrowsInBody);
        if (settings.beeStingersInBody()) player.setBeeStingersInBody(beeStingersInBody);

        if (settings.score()) player.setDeathScreenScore(score);
        if (settings.experience()) {
            player.setExp(experience);
            player.setLevel(level);
        }

        if (settings.invulnerable()) player.setInvulnerable(invulnerable);
        if (settings.health()) player.setHealth(health);

        if (settings.absorption()) player.setAbsorptionAmount(absorption);
        if (settings.exhaustion()) player.setExhaustion(exhaustion);
        if (settings.foodLevel()) player.setFoodLevel(foodLevel);
        if (settings.saturation()) player.setSaturation(saturation);

        if (settings.fireTicks()) player.setFireTicks(fireTicks);
        if (settings.freezeTicks()) player.setFreezeTicks(freezeTicks);
        if (settings.lockFreezeTicks()) player.lockFreezeTicks(lockFreezeTicks);
        if (settings.remainingAir()) player.setRemainingAir(remainingAir);

        if (settings.visualFire()) player.setVisualFire(visualFire);

        if (settings.lastDeathLocation()) player.setLastDeathLocation(lastDeathLocation);
        if (settings.respawnLocation()) player.setRespawnLocation(respawnLocation, true);

        if (settings.potionEffects()) {
            player.clearActivePotionEffects();
            player.addPotionEffects(potionEffects);
        }

        if (settings.attributes()) attributes.forEach(data -> {
            var attribute = player.getAttribute(data.attribute());
            if (attribute != null) attribute.setBaseValue(data.baseValue());
        });

        if (settings.wardenSpawnTracker()) {
            player.setWardenTimeSinceLastWarning(wardenSpawnTracker.ticksSinceLastWarning());
            player.setWardenWarningCooldown(wardenSpawnTracker.cooldownTicks());
            player.setWardenWarningLevel(wardenSpawnTracker.warningLevel());
        }

        if (settings.statistics()) stats.apply(player);

        // todo: only grant advancements internally
        if (settings.advancements()) {
            var toRemove = StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                    player.getServer().advancementIterator(), 0
            ), false).collect(Collectors.toSet());
            toRemove.removeAll(this.advancements.stream()
                    .map(AdvancementData::getAdvancement)
                    .collect(Collectors.toSet()));
            toRemove.forEach(advancement -> {
                var progress = player.getAdvancementProgress(advancement);
                progress.getAwardedCriteria().forEach(progress::revokeCriteria);
            });

            this.advancements.forEach(data -> {
                var progress = player.getAdvancementProgress(data.getAdvancement());
                data.getAwardedCriteria().forEach(progress::awardCriteria);
                data.getRemainingCriteria().forEach(progress::revokeCriteria);
            });
        }

        // todo: only (un)discover recipes internally
        if (settings.recipes()) {
            var toAdd = new HashSet<>(recipes);
            toAdd.removeAll(player.getDiscoveredRecipes());
            player.discoverRecipes(toAdd);

            var toRemove = new HashSet<>(player.getDiscoveredRecipes());
            toRemove.removeAll(recipes);
            player.undiscoverRecipes(toRemove);
        }
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
    public GameMode gameMode() {
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
    public PaperPlayerData gameMode(GameMode gameMode) {
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
