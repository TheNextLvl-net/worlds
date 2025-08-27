package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import core.paper.command.ComponentCommandExceptionType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class KeyArgument implements CustomArgumentType<Key, Key> {
    private static final SimpleCommandExceptionType ERROR_INVALID = new ComponentCommandExceptionType(
            Component.translatable("argument.id.invalid")
    );

    @Override
    @SuppressWarnings("PatternValidation")
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();

        try {
            var greedy = readGreedy(reader);
            if (greedy.contains(":")) return Key.key(greedy);
            else return Key.key("worlds", greedy);
        } catch (InvalidKeyException e) {
            reader.setCursor(cursor);
            throw ERROR_INVALID.createWithContext(reader);
        }
    }

    @Override
    public ArgumentType<Key> getNativeType() {
        return ArgumentTypes.key();
    }

    private String readGreedy(StringReader reader) {
        var cursor = reader.getCursor();

        while (reader.canRead() && isAllowedInKey(reader.peek())) {
            reader.skip();
        }

        return reader.getString().substring(cursor, reader.getCursor());
    }

    private boolean isAllowedInKey(char character) {
        // todo: replace with Key#allowedInKey
        //  https://github.com/KyoriPowered/adventure/pull/1286
        return Key.allowedInNamespace(character) || Key.allowedInValue(character) || character == ':';
    }
}
