package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface SimpleArgumentType<T, N> extends CustomArgumentType<T, N> {
    @Override
    default T parse(final StringReader reader) throws CommandSyntaxException {
        return this.convert(reader, this.getNativeType().parse(reader));
    }

    @Override
    default <S> T parse(final StringReader reader, final S source) throws CommandSyntaxException {
        return this.convert(reader, this.getNativeType().parse(reader, source), source);
    }

    T convert(StringReader reader, N type) throws CommandSyntaxException;

    default <S> T convert(final StringReader reader, final N type, final S source) throws CommandSyntaxException {
        return this.convert(reader, type);
    }
}
