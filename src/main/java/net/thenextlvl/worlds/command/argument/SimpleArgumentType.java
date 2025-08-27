package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class SimpleArgumentType<T, N> implements CustomArgumentType<T, N> {
    private final ArgumentType<N> nativeType;
    protected final WorldsPlugin plugin;

    protected SimpleArgumentType(WorldsPlugin plugin, ArgumentType<N> nativeType) {
        this.nativeType = nativeType;
        this.plugin = plugin;
    }

    protected SimpleArgumentType(WorldsPlugin plugin, CustomArgumentType<T, N> customArgumentType) {
        this(plugin, customArgumentType.getNativeType());
    }

    @Override
    public final T parse(StringReader reader) throws CommandSyntaxException {
        return this.convert(reader, this.getNativeType().parse(reader));
    }

    @Override
    public final <S> T parse(StringReader reader, S source) throws CommandSyntaxException {
        return this.convert(reader, this.getNativeType().parse(reader, source), source);
    }

    public abstract T convert(StringReader reader, N nativeType) throws CommandSyntaxException;

    public <S> T convert(StringReader reader, N nativeType, S source) throws CommandSyntaxException {
        return this.convert(reader, nativeType);
    }

    @Override
    public final ArgumentType<N> getNativeType() {
        return nativeType;
    }
}
