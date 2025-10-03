package net.thenextlvl.worlds.model;

import core.i18n.file.ResourceMigrator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

@NullMarked
public final class MessageMigrator implements ResourceMigrator {
    private final Set<MigrationRule> rules = Set.of(
            new MigrationRule(Locale.US, "world.list", "<worlds>", "<worlds:'<gray>, </gray>'>"),
            new MigrationRule(Locale.GERMANY, "world.list", "<worlds>", "<worlds:'<gray>, </gray>'>"),

            new MigrationRule(Locale.US, "world.link.list", "<links>", "<links:'<gray>,</gray><newline>'>"),
            new MigrationRule(Locale.GERMANY, "world.link.list", "<links>", "<links:'<gray>,</gray><newline>'>"),

            new MigrationRule(Locale.US, "world.info.type", " <dark_gray>(<green><old></green>)</dark_gray>", ""),
            new MigrationRule(Locale.GERMANY, "world.info.type", " <dark_gray>(<green><old></green>)</dark_gray>", "")
    );

    @Override
    public @Nullable Migration migrate(Locale locale, String key, String message) {
        return rules.stream().filter(rule -> rule.key().equals(key))
                .filter(rule -> rule.locale().equals(locale))
                .filter(rule -> message.contains(rule.match()))
                .findAny()
                .map(rule -> message.replace(rule.match(), rule.replacement()))
                .map(string -> new Migration(key, string))
                .orElse(null);
    }

    private record MigrationRule(Locale locale, String key, String match, String replacement) {
    }
}
