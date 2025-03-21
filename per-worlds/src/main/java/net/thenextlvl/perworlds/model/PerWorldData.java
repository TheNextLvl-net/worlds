package net.thenextlvl.perworlds.model;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record PerWorldData(
        @Nullable ItemStack[] enderChestContents,
        @Nullable ItemStack[] inventoryContents,
        @Nullable Location respawnLocation,
        GameMode gameMode,
        double health,
        float exhaustion,
        float experience,
        float saturation,
        int foodLevel,
        int level,
        int score
) {
    public void apply(Player player) {
        player.getEnderChest().setContents(enderChestContents);
        player.getInventory().setContents(inventoryContents);
        player.setRespawnLocation(respawnLocation);
        player.setGameMode(gameMode);
        player.setHealth(health);
        player.setExhaustion(exhaustion);
        player.setExp(experience);
        player.setSaturation(saturation);
        player.setFoodLevel(foodLevel);
        player.setLevel(level);
        player.setDeathScreenScore(score);
    }

    public static PerWorldData of(Player player) {
        return new PerWorldData(
                player.getEnderChest().getContents(),
                player.getInventory().getContents(),
                player.getRespawnLocation(),
                player.getGameMode(),
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
