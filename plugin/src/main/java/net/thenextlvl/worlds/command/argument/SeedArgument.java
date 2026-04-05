package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SeedArgument implements SimpleArgumentType<Long, String> {
    @Override
    public Long convert(final StringReader reader, final String type) {
        try {
            return Long.parseLong(type);
        } catch (final NumberFormatException ignored) {
            return (long) type.hashCode();
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
