package net.thenextlvl.worlds.data;

import lombok.ToString;

import java.util.HashMap;

@ToString(callSuper = true)
public class GameRules extends HashMap<GameRule<?>, Object> {

    public <V> GameRules set(GameRule<V> gameRule, V value) {
        put(gameRule, value);
        return this;
    }

    {
        set(GameRule.DO_WARDEN_SPAWNING, true);
        set(GameRule.GLOBAL_SOUND_EVENTS, true);
        set(GameRule.TNT_EXPLOSION_DROP_DECAY, false);
        set(GameRule.DO_FIRE_TICK, true);
        set(GameRule.MAX_COMMAND_CHAIN_LENGTH, 65536);
        set(GameRule.DO_VINES_SPREAD, true);
        set(GameRule.FIRE_DAMAGE, true);
        set(GameRule.REDUCED_DEBUG_INFO, false);
        set(GameRule.WATER_SOURCE_CONVERSION, true);
        set(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, false);
        set(GameRule.LAVA_SOURCE_CONVERSION, false);
        set(GameRule.ANNOUNCE_ADVANCEMENTS, true);
        set(GameRule.DROWNING_DAMAGE, true);
        set(GameRule.COMMAND_BLOCK_OUTPUT, true);
        set(GameRule.FORGIVE_DEAD_PLAYERS, true);
        set(GameRule.DO_MOB_SPAWNING, true);
        set(GameRule.MAX_ENTITY_CRAMMING, 24);
        set(GameRule.DISABLE_RAIDS, false);
        set(GameRule.DO_WEATHER_CYCLE, true);
        set(GameRule.MOB_EXPLOSION_DROP_DECAY, true);
        set(GameRule.DO_DAYLIGHT_CYCLE, true);
        set(GameRule.SHOW_DEATH_MESSAGES, true);
        set(GameRule.DO_TILE_DROPS, true);
        set(GameRule.UNIVERSAL_ANGER, false);
        set(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 100);
        set(GameRule.SNOW_ACCUMULATION_HEIGHT, 1);
        set(GameRule.DO_INSOMNIA, true);
        set(GameRule.BLOCK_EXPLOSION_DROP_DECAY, true);
        set(GameRule.DO_IMMEDIATE_RESPAWN, false);
        set(GameRule.NATURAL_REGENERATION, true);
        set(GameRule.DO_MOB_LOOT, true);
        set(GameRule.FALL_DAMAGE, true);
        set(GameRule.KEEP_INVENTORY, false);
        set(GameRule.DO_ENTITY_DROPS, true);
        set(GameRule.DO_LIMITED_CRAFTING, false);
        set(GameRule.MOB_GRIEFING, true);
        set(GameRule.RANDOM_TICK_SPEED, 3);
        set(GameRule.SPAWN_RADIUS, 10);
        set(GameRule.COMMAND_MODIFICATION_BLOCK_LIMIT, 32768);
        set(GameRule.DO_TRADER_SPAWNING, true);
        set(GameRule.FREEZE_DAMAGE, true);
        set(GameRule.LOG_ADMIN_COMMANDS, true);
        set(GameRule.SPECTATORS_GENERATE_CHUNKS, true);
        set(GameRule.SEND_COMMAND_FEEDBACK, true);
        set(GameRule.DO_PATROL_SPAWNING, true);
    }
}
