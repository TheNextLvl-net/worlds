package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandFlagsArgument extends WrappedArgumentType<String, CommandFlagsArgument.Flags> {
    public CommandFlagsArgument(Set<String> flags) {
        super(StringArgumentType.greedyString(), (reader, type) -> {
            var split = type.split(" ");
            if (Arrays.stream(split).anyMatch(s -> !flags.contains(s)))
                throw new IllegalArgumentException("unrecognized flag");
            return new Flags(split);
        }, (context, builder) -> {
            var index = builder.getRemaining().lastIndexOf(' ') + 1;
            var substring = builder.getRemaining().substring(index);
            flags.stream()
                    .dropWhile(builder.getRemaining()::contains)
                    .filter(flag -> flag.startsWith(substring))
                    .forEach(s -> builder.suggest(builder.getRemaining() + s.substring(substring.length())));
            return builder.buildFuture();
        });
    }

    public static class Flags extends HashSet<String> {
        private Flags(@NotNull String... flags) {
            super(Set.of(flags));
        }
    }
}
