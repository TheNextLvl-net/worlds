package net.thenextlvl.perworlds.data;

import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.statistics.Stats;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PlayerData {
    @Nullable
    ItemStack[] enderChest();

    @Nullable
    ItemStack[] inventory();

    @Unmodifiable
    List<PotionEffect> potionEffects();

    GameMode gameMode();

    @Nullable
    GameMode previousGameMode();

    @Nullable
    Location lastDeathLocation();

    @Nullable
    Location lastLocation();

    @Nullable
    Location respawnLocation();

    PlayerData absorption(double absorption);

    PlayerData advancements(Collection<AdvancementData> advancements);

    PlayerData arrowsInBody(int arrowsInBody);

    PlayerData attributes(Collection<AttributeData> attributes);

    PlayerData beeStingersInBody(int beeStingers);

    PlayerData discoveredRecipes(Collection<NamespacedKey> recipes);

    PlayerData enderChest(@Nullable ItemStack[] contents);

    PlayerData exhaustion(float exhaustion);

    PlayerData experience(float experience);

    PlayerData fallDistance(float fallDistance);

    PlayerData fireTicks(int fireTicks);

    PlayerData flying(boolean flying);

    PlayerData foodLevel(int foodLevel);

    PlayerData freezeTicks(int freezeTicks);

    PlayerData gameMode(GameMode gameMode);

    PlayerData gliding(boolean gliding);

    PlayerData health(double health);

    PlayerData heldItemSlot(int heldItemSlot);

    PlayerData inventory(@Nullable ItemStack[] contents);

    PlayerData invulnerable(boolean invulnerable);

    PlayerData lastDeathLocation(@Nullable Location location);

    PlayerData lastLocation(@Nullable Location location);

    PlayerData level(int level);

    PlayerData lockFreezeTicks(boolean lockFreezeTicks);

    PlayerData mayFly(boolean mayFly);

    PlayerData portalCooldown(int cooldown);

    PlayerData potionEffects(Collection<PotionEffect> effects);

    PlayerData previousGameMode(@Nullable GameMode gameMode);

    PlayerData remainingAir(int remainingAir);

    PlayerData respawnLocation(@Nullable Location location);

    PlayerData saturation(float saturation);

    PlayerData score(int score);

    PlayerData seenCredits(boolean seenCredits);

    PlayerData stats(Stats stats);

    PlayerData velocity(Vector velocity);

    PlayerData visualFire(boolean visualFire);

    PlayerData wardenSpawnTracker(WardenSpawnTracker tracker);

    @Unmodifiable
    Set<AdvancementData> advancements();

    @Unmodifiable
    Set<AttributeData> attributes();

    @Unmodifiable
    Set<NamespacedKey> discoveredRecipes();

    Stats stats();

    Vector velocity();

    WardenSpawnTracker wardenSpawnTracker();

    boolean flying();

    boolean gliding();

    boolean invulnerable();

    boolean lockFreezeTicks();

    boolean mayFly();

    boolean seenCredits();

    boolean visualFire();

    double absorption();

    double health();

    float exhaustion();

    float experience();

    float fallDistance();

    float saturation();

    int arrowsInBody();

    int beeStingersInBody();

    int fireTicks();

    int foodLevel();

    int freezeTicks();

    int heldItemSlot();

    int level();

    int portalCooldown();

    int remainingAir();

    int score();

    void apply(GroupSettings settings, Player player, boolean position);
}
