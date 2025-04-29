package net.thenextlvl.worlds.command.argument;

import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public class DimensionArgument extends WrappedArgumentType<Key, World.Environment> {
    public DimensionArgument(WorldsPlugin plugin) {
        super(ArgumentTypes.key(), (reader, type) -> switch (type.asString()) {
            case "minecraft:overworld" -> World.Environment.NORMAL;
            case "minecraft:the_end" -> World.Environment.THE_END;
            case "minecraft:the_nether" -> World.Environment.NETHER;
            default -> throw new IllegalArgumentException("Custom dimensions are not yet supported");
        }, new DimensionSuggestionProvider(plugin, Map.of(
                "minecraft:overworld", "environment.normal",
                "minecraft:the_end", "environment.end",
                "minecraft:the_nether", "environment.nether"
        )));
    }
}
