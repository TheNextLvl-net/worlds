package worlds.util;

import core.annotation.FieldsAreNonnullByDefault;
import core.api.file.format.MessageFile;
import core.api.placeholder.MessageKey;
import core.api.placeholder.SystemMessageKey;
import net.kyori.adventure.audience.Audience;
import worlds.Worlds;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

@FieldsAreNonnullByDefault
public class Messages {
    public static final Locale ENGLISH = Locale.forLanguageTag("en-US");
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    public static final SystemMessageKey<Audience> PREFIX = new SystemMessageKey<>("worlds.prefix", plugin.formatter()).register();

    public static final MessageKey<Audience> WORLD_SAVED = new MessageKey<>("world-saved", plugin.formatter()).register();

    static {
        initRoot();
        initEnglish();
        initGerman();
    }

    private static void initRoot() {
        var file = MessageFile.ROOT;
        file.setDefault(PREFIX, "<white>Worlds <dark_gray>»<reset>");
        file.save();
    }

    private static void initEnglish() {
        var file = MessageFile.getOrCreate(ENGLISH);
        file.setDefault(WORLD_SAVED, "%prefix% <white>Saved the world <green>%world%");
        file.save();
    }

    private static void initGerman() {
        MessageFile german = MessageFile.getOrCreate(Locale.forLanguageTag("de-DE"));
        german.setDefault(WORLD_SAVED, "%prefix% <white>Die welt <green>%world% <white>wurde gespeichert");
        german.save();
    }
}
