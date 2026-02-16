package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.LevelStem;
import net.thenextlvl.worlds.command.suggestion.TooltipSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public final class LevelStemArgument extends TooltipSuggestionProvider implements SimpleArgumentType<LevelStem, String> {
    public LevelStemArgument(final WorldsPlugin plugin) {
        super(plugin, Map.of(
                "normal", "environment.normal",
                "nether", "environment.nether",
                "end", "environment.end"
        ));
    }

    @Override
    public LevelStem convert(final StringReader reader, final String type) {
        return switch (type) {
            case "normal", "overworld" -> LevelStem.OVERWORLD;
            case "nether" -> LevelStem.NETHER;
            case "end" -> LevelStem.END;
            default -> throw new IllegalArgumentException("Custom dimensions are not yet supported");
        };
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
