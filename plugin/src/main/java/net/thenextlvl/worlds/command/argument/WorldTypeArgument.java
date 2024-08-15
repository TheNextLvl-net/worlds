package net.thenextlvl.worlds.command.argument;

import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.WorldTypeSuggestionProvider;
import org.bukkit.WorldType;

@SuppressWarnings("UnstableApiUsage")
public class WorldTypeArgument extends WrappedArgumentType<Key, WorldType> {
    public WorldTypeArgument(WorldsPlugin plugin) {
        super(ArgumentTypes.key(), (reader, type) -> switch (type.asString()) {
            case "minecraft:amplified" -> WorldType.AMPLIFIED;
            case "minecraft:flat" -> WorldType.FLAT;
            case "minecraft:large_biomes" -> WorldType.LARGE_BIOMES;
            case "minecraft:normal" -> WorldType.NORMAL;
            // case "minecraft:single_biome" -> ;
            // case "minecraft:debug_world" -> ;
            default -> throw new IllegalArgumentException("Custom dimensions are not yet supported");
        }, new WorldTypeSuggestionProvider(plugin));
    }
}
