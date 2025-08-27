package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@NullMarked
public final class CommandFlagsArgument extends WrappedArgumentType<String, CommandFlagsArgument.Flags> {
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
                    .filter(flag -> !builder.getRemaining().contains(flag))
                    .filter(flag -> flag.startsWith(substring))
                    .forEach(s -> builder.suggest(builder.getRemaining() + s.substring(substring.length())));
            return builder.buildFuture();
        });
    }

    public static final class Flags extends HashSet<String> {
        private Flags(String... flags) {
            super(Set.of(flags));
        }
    }
}
