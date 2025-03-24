package net.thenextlvl.perworlds.model;

import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.data.AttributeData;
import net.thenextlvl.perworlds.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NullMarked
public class PaperPlayerData implements PlayerData {
    private @Nullable ItemStack[] enderChestContents = new ItemStack[27];
    private @Nullable ItemStack[] inventoryContents = new ItemStack[40];
    private @Nullable Location respawnLocation = null;
    private GameMode gameMode = GameMode.SURVIVAL;
    private List<PotionEffect> potionEffects = List.of();
    private Set<AttributeData> attributes = Set.of();
    private Set<NamespacedKey> recipes = Set.of();
    private boolean seenCredits = false;
    private double absorption = 0;
    private double health = 20;
    private float exhaustion = 0;
    private float experience = 0;
    private float fallDistance = 0;
    private float saturation = 10;
    private int fireTicks = 0;
    private int foodLevel = 20;
    private int freezeTicks = 0;
    private int heldItemSlot = 0;
    private int level = 0;
    private int remainingAir = 300;
    private int score = 0;

    public static PaperPlayerData of(Player player) {
        return new PaperPlayerData()
                .attributes(Registry.ATTRIBUTE.stream()
                        .map(player::getAttribute)
                        .filter(Objects::nonNull)
                        .map(PaperAttributeData::new)
                        .collect(Collectors.toSet()))
                .enderChestContents(player.getEnderChest().getContents())
                .inventoryContents(player.getInventory().getContents())
                .respawnLocation(player.getPotentialRespawnLocation())
                .potionEffects(player.getActivePotionEffects())
                .gameMode(player.getGameMode())
                .discoveredRecipes(player.getDiscoveredRecipes())
                .seenCredits(player.hasSeenWinScreen())
                .absorption(player.getAbsorptionAmount())
                .health(player.getHealth())
                .exhaustion(player.getExhaustion())
                .experience(player.getExp())
                .saturation(player.getSaturation())
                .fireTicks(player.getFireTicks())
                .foodLevel(player.getFoodLevel())
                .freezeTicks(player.getFreezeTicks())
                .heldItemSlot(player.getInventory().getHeldItemSlot())
                .level(player.getLevel())
                .remainingAir(player.getRemainingAir())
                .score(player.getDeathScreenScore());
    }

    @Override
    public void apply(GroupSettings settings, Player player) {
        if (settings.absorption()) player.setAbsorptionAmount(absorption);
        if (settings.endCredits()) player.setHasSeenWinScreen(seenCredits);
        if (settings.exhaustion()) player.setExhaustion(exhaustion);
        if (settings.fallDistance()) player.setFallDistance(fallDistance);
        if (settings.fireTicks()) player.setFireTicks(fireTicks);
        if (settings.foodLevel()) player.setFoodLevel(foodLevel);
        if (settings.freezeTicks()) player.setFreezeTicks(freezeTicks);
        if (settings.gameMode()) player.setGameMode(gameMode);
        if (settings.health()) player.setHealth(health);
        if (settings.remainingAir()) player.setRemainingAir(remainingAir);
        if (settings.respawnLocation()) player.setRespawnLocation(respawnLocation, true);
        if (settings.saturation()) player.setSaturation(saturation);
        if (settings.score()) player.setDeathScreenScore(score);

        if (settings.attributes()) attributes.forEach(data -> {
            var attribute = player.getAttribute(data.attribute());
            if (attribute != null) attribute.setBaseValue(data.baseValue());
        });

        if (settings.recipes()) {
            var toAdd = new HashSet<>(recipes);
            toAdd.removeAll(player.getDiscoveredRecipes());
            player.discoverRecipes(toAdd);

            var toRemove = new HashSet<>(player.getDiscoveredRecipes());
            toRemove.removeAll(recipes);
            player.undiscoverRecipes(toRemove);
        }

        if (settings.inventory()) {
            player.getEnderChest().setContents(enderChestContents);
            player.getInventory().setContents(inventoryContents);
            player.getInventory().setHeldItemSlot(heldItemSlot);
        }
        if (settings.potionEffects()) {
            player.clearActivePotionEffects();
            player.addPotionEffects(potionEffects);
        }
        if (settings.experience()) {
            player.setExp(experience);
            player.setLevel(level);
        }
    }

    @Override
    public @Nullable ItemStack[] enderChestContents() {
        return enderChestContents;
    }

    @Override
    public @Nullable ItemStack[] inventoryContents() {
        return inventoryContents;
    }

    @Override
    public @Nullable Location respawnLocation() {
        return respawnLocation;
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
    public PaperPlayerData absorption(double absorption) {
        this.absorption = absorption;
        return this;
    }

    @Override
    public PaperPlayerData attributes(Collection<AttributeData> attributes) {
        this.attributes = Set.copyOf(attributes);
        return this;
    }

    @Override
    public PaperPlayerData discoveredRecipes(Collection<NamespacedKey> recipes) {
        this.recipes = Set.copyOf(recipes);
        return this;
    }

    @Override
    public PaperPlayerData enderChestContents(@Nullable ItemStack[] contents) {
        this.enderChestContents = contents;
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
    public PaperPlayerData inventoryContents(@Nullable ItemStack[] contents) {
        this.inventoryContents = contents;
        return this;
    }

    @Override
    public PaperPlayerData seenCredits(boolean seenCredits) {
        this.seenCredits = seenCredits;
        return this;
    }

    @Override
    public PaperPlayerData level(int level) {
        this.level = level;
        return this;
    }

    @Override
    public PaperPlayerData potionEffects(Collection<PotionEffect> effects) {
        this.potionEffects = List.copyOf(effects);
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
    public float saturation() {
        return saturation;
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
    public int remainingAir() {
        return remainingAir;
    }

    @Override
    public int score() {
        return score;
    }
}
