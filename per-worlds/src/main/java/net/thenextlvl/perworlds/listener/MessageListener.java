package net.thenextlvl.perworlds.listener;

import net.kyori.adventure.text.Component;
import net.thenextlvl.perworlds.GroupProvider;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.WorldGroup;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.bukkit.GameRule.ANNOUNCE_ADVANCEMENTS;
import static org.bukkit.GameRule.SHOW_DEATH_MESSAGES;

@NullMarked
public class MessageListener implements Listener {
    private final GroupProvider provider;

    public MessageListener(GroupProvider provider) {
        this.provider = provider;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        handle(event.getPlayer().getWorld(), ANNOUNCE_ADVANCEMENTS, GroupSettings::advancementMessages, event::message, event.message());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        handle(event.getPlayer().getWorld(), SHOW_DEATH_MESSAGES, GroupSettings::deathMessages, event::deathMessage, event.deathMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        handle(event.getPlayer().getWorld(), null, GroupSettings::joinMessages, event::joinMessage, event.joinMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        handle(event.getPlayer().getWorld(), null, GroupSettings::quitMessages, event::quitMessage, event.quitMessage());
    }

    private void handle(World world, @Nullable GameRule<Boolean> gameRule, Predicate<GroupSettings> enabled,
                        Consumer<@Nullable Component> disable, @Nullable Component message) {
        if (message == null) return;
        var receivers = receivers(world, gameRule, enabled);
        if (receivers == null) return;
        receivers.forEach(player -> player.sendMessage(message));
        disable.accept(null);
    }

    private @Nullable List<Player> receivers(World world, @Nullable GameRule<Boolean> gameRule, Predicate<GroupSettings> enabled) {
        if (!canReceive(gameRule, world)) return null;
        var group = provider.getGroup(world).orElse(provider.getUnownedWorldGroup());
        return enabled.test(group.getSettings()) ? group.getPlayers() : provider.getAllGroups().stream()
                .filter(target -> !enabled.test(target.getSettings()))
                .filter(target -> canReceive(gameRule, target))
                .map(WorldGroup::getPlayers)
                .flatMap(Collection::stream)
                .toList();
    }

    private boolean canReceive(@Nullable GameRule<Boolean> gameRule, WorldGroup group) {
        return gameRule == null || Optional.ofNullable(group.getGroupData().gameRule(gameRule))
                .or(() -> group.getWorlds().findAny().map(world -> canReceive(gameRule, world)))
                .orElse(true);
    }

    private boolean canReceive(@Nullable GameRule<Boolean> gameRule, World world) {
        return gameRule == null || Boolean.TRUE.equals(world.getGameRuleValue(gameRule));
    }
}
