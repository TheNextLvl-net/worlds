package net.nonswag.tnl.world.utils;

import net.nonswag.core.api.message.Message;
import net.nonswag.core.api.message.key.MessageKey;

import javax.annotation.Nonnull;

public final class Messages {

    @Nonnull
    public static final MessageKey WORLD_SAVED = new MessageKey("world-saved").register();

    private Messages() {
    }

    public static void loadAll() {
        loadEnglish();
        loadGerman();
    }

    private static void loadEnglish() {
        Message.getEnglish().setDefault(WORLD_SAVED, "%prefix%§a Saved the world §6%world%");
        Message.getEnglish().save();
    }

    private static void loadGerman() {
        Message.getGerman().setDefault(WORLD_SAVED, "%prefix%§a Die welt §6%world%§a wurde gespeichert");
        Message.getGerman().save();
    }
}
