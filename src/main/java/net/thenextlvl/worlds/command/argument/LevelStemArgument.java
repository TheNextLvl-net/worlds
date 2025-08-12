package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public class LevelStemArgument extends WrappedArgumentType<String, LevelStem> {
    public LevelStemArgument(WorldsPlugin plugin) {
        super(StringArgumentType.word(), (reader, type) -> switch (type) {
            case "normal", "overworld" -> LevelStem.OVERWORLD;
            case "nether" -> LevelStem.NETHER;
            case "end" -> LevelStem.END;
            default -> throw new IllegalArgumentException("Custom dimensions are not yet supported");
        }, new DimensionSuggestionProvider(plugin, Map.of(
                "normal", "environment.normal",
                "nether", "environment.nether",
                "end", "environment.end"
        )));
    }
}
