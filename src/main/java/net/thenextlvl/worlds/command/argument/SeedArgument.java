package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;

public class SeedArgument extends WrappedArgumentType<String, Long> {
    public SeedArgument() {
        super(StringArgumentType.string(), (reader, type) -> {
            try {
                return Long.parseLong(type);
            } catch (NumberFormatException ignored) {
                return (long) type.hashCode();
            }
        });
    }
}
