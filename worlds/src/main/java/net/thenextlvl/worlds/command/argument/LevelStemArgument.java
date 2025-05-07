package net.thenextlvl.worlds.command.argument;

import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public class LevelStemArgument extends WrappedArgumentType<Key, LevelStem> {
    public LevelStemArgument(WorldsPlugin plugin) {
        super(ArgumentTypes.key(), (reader, type) -> switch (type.asString()) {
            case "minecraft:overworld" -> LevelStem.OVERWORLD;
            case "minecraft:nether" -> LevelStem.NETHER;
            case "minecraft:end" -> LevelStem.END;
            default -> throw new IllegalArgumentException("Custom dimensions are not yet supported");
        }, new DimensionSuggestionProvider(plugin, Map.of(
                "minecraft:overworld", "environment.normal",
                "minecraft:nether", "environment.nether",
                "minecraft:end", "environment.end"
        )));
    }
}
