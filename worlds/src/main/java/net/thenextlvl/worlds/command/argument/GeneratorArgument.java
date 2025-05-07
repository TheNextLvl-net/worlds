package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.generator.Generator;
import net.thenextlvl.worlds.command.suggestion.GeneratorSuggestionProvider;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GeneratorArgument extends WrappedArgumentType<String, Generator> {
    public GeneratorArgument(WorldsPlugin plugin) {
        super(StringArgumentType.string(), (reader, type) ->
                        Generator.deserialize(plugin, type),
                new GeneratorSuggestionProvider(plugin));
    }
}
