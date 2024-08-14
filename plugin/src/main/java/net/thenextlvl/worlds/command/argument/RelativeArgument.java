package net.thenextlvl.worlds.command.argument;

import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.command.suggestion.RelativeSuggestionProvider;
import net.thenextlvl.worlds.link.Relative;

@SuppressWarnings("UnstableApiUsage")
public class RelativeArgument extends WrappedArgumentType<Key, Relative> {
    public RelativeArgument() {
        super(ArgumentTypes.key(), (reader, type) -> Relative.valueOf(type).orElseThrow(() ->
                        new IllegalArgumentException("Unknown relative: " + type.asString())),
                new RelativeSuggestionProvider());
    }
}
