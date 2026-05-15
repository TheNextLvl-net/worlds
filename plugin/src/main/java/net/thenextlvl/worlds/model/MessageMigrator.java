package net.thenextlvl.worlds.model;

import net.thenextlvl.i18n.ResourceMigrator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

@NullMarked
public final class MessageMigrator implements ResourceMigrator {
    private final Set<MigrationRule> rules = Set.of(
            new MigrationRule(Locale.US, "world.list.unimported",
                    Matcher.equals("<gray><tree></gray> <dark_red><world></dark_red>"),
                    Mutation.append(" <dark_gray>(<dimension>)</dark_gray>")),
            new MigrationRule(Locale.GERMANY, "world.list.unimported",
                    Matcher.equals("<gray><tree></gray> <dark_red><world></dark_red>"),
                    Mutation.append(" <dark_gray>(<dimension>)</dark_gray>"))
    );

    @Override
    public @Nullable Migration migrate(final Locale locale, final String key, final String message) {
        return rules.stream().filter(rule -> rule.key().equals(key))
                .filter(rule -> rule.locale().equals(locale))
                .filter(rule -> rule.matcher().matches(message))
                .findAny()
                .map(rule -> rule.mutation().apply(message))
                .map(string -> new Migration(key, string))
                .orElse(null);
    }

    private record MigrationRule(Locale locale, String key, Matcher matcher, Mutation mutation) {
    }

    @FunctionalInterface
    private interface Matcher {
        boolean matches(String message);

        static Matcher equals(final String match) {
            return message -> message.equals(match);
        }
    }

    @FunctionalInterface
    private interface Mutation {
        String apply(String message);

        static Mutation append(final String replacement) {
            return message -> message + replacement;
        }
    }
}
