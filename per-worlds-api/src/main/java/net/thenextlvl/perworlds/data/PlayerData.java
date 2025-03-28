package net.thenextlvl.perworlds.data;

import net.thenextlvl.perworlds.GroupSettings;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PlayerData {
    // todo: stats, achievements

    @Nullable
    ItemStack[] enderChestContents();

    @Nullable
    ItemStack[] inventoryContents();

    @Nullable
    Location respawnLocation();

    @Unmodifiable
    List<PotionEffect> potionEffects();

    GameMode gameMode();

    PlayerData absorption(double absorption);

    PlayerData attributes(Collection<AttributeData> attributes);

    PlayerData discoveredRecipes(Collection<NamespacedKey> recipes);

    PlayerData enderChestContents(@Nullable ItemStack[] contents);

    PlayerData exhaustion(float exhaustion);

    PlayerData experience(float experience);

    PlayerData fallDistance(float fallDistance);

    PlayerData fireTicks(int fireTicks);

    PlayerData foodLevel(int foodLevel);

    PlayerData freezeTicks(int freezeTicks);

    PlayerData gameMode(GameMode gameMode);

    PlayerData health(double health);

    PlayerData heldItemSlot(int heldItemSlot);

    PlayerData inventoryContents(@Nullable ItemStack[] contents);

    PlayerData level(int level);

    PlayerData potionEffects(Collection<PotionEffect> effects);

    PlayerData remainingAir(int remainingAir);

    PlayerData respawnLocation(@Nullable Location location);

    PlayerData saturation(float saturation);

    PlayerData score(int score);

    PlayerData seenCredits(boolean seenCredits);

    @Unmodifiable
    Set<AttributeData> attributes();

    @Unmodifiable
    Set<NamespacedKey> discoveredRecipes();

    boolean seenCredits();

    double absorption();

    double health();

    float exhaustion();

    float experience();

    float fallDistance();

    float saturation();

    int fireTicks();

    int foodLevel();

    int freezeTicks();

    int heldItemSlot();

    int level();

    int remainingAir();

    int score();

    void apply(GroupSettings settings, Player player);
}
