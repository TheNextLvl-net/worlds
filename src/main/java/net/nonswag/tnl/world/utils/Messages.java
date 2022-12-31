package net.nonswag.tnl.world.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.nonswag.core.api.annotation.FieldsAreNonnullByDefault;
import net.nonswag.core.api.file.formats.MessageFile;
import net.nonswag.core.api.language.Language;
import net.nonswag.core.api.message.key.MessageKey;

@FieldsAreNonnullByDefault
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Messages {
    public static final MessageKey WORLD_SAVED = new MessageKey("world-saved").register();

    public static void init() {
        initEnglish();
        initGerman();
    }

    private static void initEnglish() {
        MessageFile english = MessageFile.getOrCreate(Language.AMERICAN_ENGLISH);
        english.setDefault(WORLD_SAVED, "%prefix%§a Saved the world §6%world%");
        english.save();
    }

    private static void initGerman() {
        MessageFile german = MessageFile.getOrCreate(Language.GERMAN);
        german.setDefault(WORLD_SAVED, "%prefix%§a Die welt §6%world%§a wurde gespeichert");
        german.save();
    }
}
