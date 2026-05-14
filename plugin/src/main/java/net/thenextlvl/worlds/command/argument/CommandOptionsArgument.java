package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@NullMarked
public final class CommandOptionsArgument implements CustomArgumentType<CommandOptionsArgument.Options, String> {
    private final Map<String, @Nullable ArgumentType<?>> options;

    public CommandOptionsArgument(final Set<String> options) {
        this(toMap(options));
    }

    public CommandOptionsArgument(final Map<String, @Nullable ArgumentType<?>> options) {
        this.options = options;
    }

    @Override
    public Options parse(final StringReader reader) throws CommandSyntaxException {
        final var result = new Options();
        while (reader.canRead()) {
            reader.skipWhitespace();
            if (!reader.canRead()) break;
            final var token = readUnquotedString(reader);
            final var argument = options.get(token);
            if (!options.containsKey(token)) throw new IllegalArgumentException("unrecognized option");
            if (argument == null) {
                result.add(token);
                continue;
            }

            reader.skipWhitespace();
            if (!reader.canRead()) throw new IllegalArgumentException("missing option argument");
            result.put(token, parseArgument(argument, reader));
            requireWhitespaceOrEnd(reader);
        }
        return result;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final var reader = new StringReader(builder.getRemaining());
        while (reader.canRead()) {
            reader.skipWhitespace();
            final var optionStart = reader.getCursor();
            if (!reader.canRead()) return suggestOptions(builder, optionStart, "");

            final var option = readUnquotedString(reader);
            final var argument = options.get(option);
            if (!options.containsKey(option)) {
                if (!reader.canRead()) return suggestOptions(builder, optionStart, option);
                return Suggestions.empty();
            }
            if (argument == null) continue;

            reader.skipWhitespace();
            final var argumentStart = reader.getCursor();
            if (!reader.canRead())
                return argument.listSuggestions(context, builder.createOffset(builder.getStart() + argumentStart));
            try {
                parseArgument(argument, reader);
            } catch (final Exception ignored) {
                return argument.listSuggestions(context, builder.createOffset(builder.getStart() + argumentStart));
            }
            if (!reader.canRead())
                return argument.listSuggestions(context, builder.createOffset(builder.getStart() + argumentStart));
            if (!isWhitespaceOrEnd(reader)) return Suggestions.empty();
        }

        return builder.getRemaining().isEmpty() || builder.getRemaining().endsWith(" ")
                ? suggestOptions(builder, reader.getCursor(), "")
                : Suggestions.empty();
    }

    public static final class Options extends HashSet<String> {
        private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.ofEntries(
                Map.entry(boolean.class, Boolean.class),
                Map.entry(byte.class, Byte.class),
                Map.entry(short.class, Short.class),
                Map.entry(char.class, Character.class),
                Map.entry(int.class, Integer.class),
                Map.entry(long.class, Long.class),
                Map.entry(float.class, Float.class),
                Map.entry(double.class, Double.class)
        );

        private final Map<String, Object> arguments = new LinkedHashMap<>();

        private void put(final String option, final Object argument) {
            add(option);
            arguments.put(option, argument);
        }

        @SuppressWarnings("unchecked")
        public <T> Optional<T> getArgument(final String option, final Class<T> type) {
            final var argument = arguments.get(option);

            if (argument == null) return Optional.empty();

            if (PRIMITIVE_TO_WRAPPER.getOrDefault(type, type).isAssignableFrom(argument.getClass()))
                return Optional.of((T) argument);
            throw new IllegalArgumentException("Argument '" + option + "' is defined as " + argument.getClass().getSimpleName() + ", not " + type);
        }
    }

    private static Map<String, @Nullable ArgumentType<?>> toMap(final Set<String> options) {
        final var map = new LinkedHashMap<String, @Nullable ArgumentType<?>>();
        options.forEach(option -> map.put(option, null));
        return map;
    }

    private static Map<String, @Nullable ArgumentType<?>> toMap(
            final Set<String> options,
            final Map<String, ArgumentType<?>> arguments
    ) {
        final var map = toMap(options);
        map.putAll(arguments);
        return map;
    }

    private static boolean containsToken(final String remaining, final String token) {
        final var reader = new StringReader(remaining);
        while (reader.canRead()) {
            reader.skipWhitespace();
            final var start = reader.getCursor();
            while (reader.canRead() && reader.peek() != ' ') reader.skip();
            if (reader.getString().substring(start, reader.getCursor()).equals(token)) return true;
        }
        return false;
    }

    private static String readUnquotedString(final StringReader reader) {
        final var start = reader.getCursor();
        while (reader.canRead() && reader.peek() != ' ') reader.skip();
        return reader.getString().substring(start, reader.getCursor());
    }

    private Object parseArgument(final ArgumentType<?> argument, final StringReader reader) throws CommandSyntaxException {
        final var cursor = reader.getCursor();
        final var end = findNextOption(reader.getString(), cursor);
        if (end == -1) return argument.parse(reader);

        final var input = reader.getString().substring(cursor, end);
        final var argumentReader = new StringReader(input);
        final var parsed = argument.parse(argumentReader);
        reader.setCursor(cursor + argumentReader.getCursor());
        return parsed;
    }

    private static void requireWhitespaceOrEnd(final StringReader reader) throws CommandSyntaxException {
        if (!isWhitespaceOrEnd(reader)) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                .readerExpectedSymbol()
                .createWithContext(reader, " ");
    }

    private static boolean isWhitespaceOrEnd(final StringReader reader) {
        return !reader.canRead() || reader.peek() == ' ';
    }

    private int findNextOption(final String input, final int start) {
        var cursor = start;
        while (cursor < input.length()) {
            if (input.charAt(cursor) != ' ') {
                cursor++;
                continue;
            }

            var optionStart = cursor;
            while (optionStart < input.length() && input.charAt(optionStart) == ' ') optionStart++;
            final var optionEnd = readUnquotedStringEnd(input, optionStart);
            if (options.containsKey(input.substring(optionStart, optionEnd))) return cursor;
            cursor = optionEnd;
        }
        return -1;
    }

    private static int readUnquotedStringEnd(final String input, final int start) {
        var cursor = start;
        while (cursor < input.length() && input.charAt(cursor) != ' ') cursor++;
        return cursor;
    }

    private CompletableFuture<Suggestions> suggestOptions(
            final SuggestionsBuilder builder,
            final int start,
            final String value
    ) {
        final var offset = builder.createOffset(builder.getStart() + start);
        for (final var option : options.keySet()) {
            if (containsToken(builder.getRemaining(), option)) continue;
            if (option.startsWith(value)) offset.suggest(option);
        }
        builder.add(offset);
        return builder.buildFuture();
    }
}
