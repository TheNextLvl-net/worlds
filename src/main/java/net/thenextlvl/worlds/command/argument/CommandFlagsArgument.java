package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class CommandFlagsArgument implements SimpleArgumentType<CommandFlagsArgument.Flags, String> {
    private final Set<String> flags;
    
    public CommandFlagsArgument(Set<String> flags) {
        this.flags = flags;
    }

    @Override
    public Flags convert(StringReader reader, String type) {
        var split = type.split(" ");
        if (Arrays.stream(split).anyMatch(s -> !flags.contains(s)))
            throw new IllegalArgumentException("unrecognized flag");
        return new Flags(split);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var index = builder.getRemaining().lastIndexOf(' ') + 1;
        var substring = builder.getRemaining().substring(index);
        flags.stream()
                .filter(flag -> !builder.getRemaining().contains(flag))
                .filter(flag -> flag.startsWith(substring))
                .forEach(s -> builder.suggest(builder.getRemaining() + s.substring(substring.length())));
        return builder.buildFuture();
    }

    public static final class Flags extends HashSet<String> {
        private Flags(String... flags) {
            super(Set.of(flags));
        }
    }
}
