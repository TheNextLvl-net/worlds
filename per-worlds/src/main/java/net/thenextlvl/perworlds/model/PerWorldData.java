package net.thenextlvl.perworlds.model;

import net.thenextlvl.perworlds.GroupSettings;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@NullMarked
public record PerWorldData(
        @Nullable ItemStack[] enderChestContents,
        @Nullable ItemStack[] inventoryContents,
        @Nullable Location respawnLocation,
        Collection<PotionEffect> potionEffects,
        // todo: save attributes
        GameMode gameMode,
        double absorption,
        double health,
        float exhaustion,
        float experience,
        float saturation,
        int foodLevel,
        int level,
        int score
) {
    public static final PerWorldData DEFAULT = new PerWorldData(
            new ItemStack[27], new ItemStack[40], null, List.of(),
            GameMode.SURVIVAL, 0, 20, 0, 0, 10, 20, 0, 0
    );

    public void apply(GroupSettings settings, Player player) {
        if (settings.absorption()) player.setAbsorptionAmount(absorption);
        if (settings.attributes()) player.setExhaustion(exhaustion);
        if (settings.foodLevel()) player.setFoodLevel(foodLevel);
        if (settings.gameMode()) player.setGameMode(gameMode);
        if (settings.health()) player.setHealth(health);
        if (settings.respawnLocation()) player.setRespawnLocation(respawnLocation, true);
        if (settings.saturation()) player.setSaturation(saturation);
        if (settings.score()) player.setDeathScreenScore(score);

        if (settings.inventory()) {
            player.getEnderChest().setContents(enderChestContents);
            player.getInventory().setContents(inventoryContents);
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

    public static PerWorldData of(Player player) {
        return new PerWorldData(
                player.getEnderChest().getContents(),
                player.getInventory().getContents(),
                player.getPotentialRespawnLocation(),
                player.getActivePotionEffects(),
                player.getGameMode(),
                player.getAbsorptionAmount(),
                player.getHealth(),
                player.getExhaustion(),
                player.getSaturation(),
                player.getExp(),
                player.getFoodLevel(),
                player.getLevel(),
                player.getDeathScreenScore()
        );
    }
}
