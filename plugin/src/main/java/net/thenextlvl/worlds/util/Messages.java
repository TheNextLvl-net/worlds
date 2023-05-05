package net.thenextlvl.worlds.util;

import core.annotation.FieldsAreNonnullByDefault;
import core.api.file.format.MessageFile;
import core.api.placeholder.MessageKey;
import core.api.placeholder.SystemMessageKey;
import net.kyori.adventure.audience.Audience;
import net.thenextlvl.worlds.Worlds;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

@FieldsAreNonnullByDefault
public class Messages {
    public static final Locale ENGLISH = Locale.forLanguageTag("en-US");
    private static final Worlds plugin = JavaPlugin.getPlugin(Worlds.class);

    public static final SystemMessageKey<Audience> PREFIX = new SystemMessageKey<>("worlds.prefix", plugin.formatter()).register();

    public static final MessageKey<Audience> WORLD_SAVED = new MessageKey<>("world-saved", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_LIST = new MessageKey<>("world.list", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_EXISTS = new MessageKey<>("world.exists", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_NOT_FOUND = new MessageKey<>("world.exists.not", plugin.formatter()).register();

    public static final MessageKey<Audience> WORLD_INFO_NAME = new MessageKey<>("world.info.name", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_INFO_PLAYERS = new MessageKey<>("world.info.players", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_INFO_TYPE = new MessageKey<>("world.info.type", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_INFO_ENVIRONMENT = new MessageKey<>("world.info.environment", plugin.formatter()).register();

    public static final MessageKey<Audience> WORLD_DELETE_SUCCEEDED = new MessageKey<>("world.delete.success", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_DELETE_FAILED = new MessageKey<>("world.delete.failed", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_UNLOAD_FAILED = new MessageKey<>("world.unload.failed", plugin.formatter()).register();

    public static final MessageKey<Audience> NO_PERMISSION = new MessageKey<>("command.permission", plugin.formatter()).register();
    public static final MessageKey<Audience> INVALID_SENDER = new MessageKey<>("command.sender", plugin.formatter()).register();
    public static final MessageKey<Audience> INCOMPLETE_ARGUMENT = new MessageKey<>("command.argument", plugin.formatter()).register();

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
        file.setDefault(WORLD_LIST, "%prefix% <gray>Worlds <dark_gray>(<green>%amount%<dark_gray>): <white>%worlds%");
        file.setDefault(WORLD_EXISTS, "%prefix% <red>A world called <dark_red>%world%<red> does already exist");
        file.setDefault(WORLD_NOT_FOUND, "%prefix% <red>A world called <dark_red>%world%<red> does not exist");
        file.setDefault(WORLD_INFO_NAME, "%prefix% <gray>Name<dark_gray>: <white>%world%");
        file.setDefault(WORLD_INFO_PLAYERS, "%prefix% <gray>Players<dark_gray>: <white>%players%");
        file.setDefault(WORLD_INFO_TYPE, "%prefix% <gray>Type<dark_gray>: <white>%type%");
        file.setDefault(WORLD_INFO_ENVIRONMENT, "%prefix% <gray>Environment<dark_gray>: <white>%environment%");
        file.setDefault(WORLD_DELETE_SUCCEEDED, "%prefix% <white>Successfully deleted the world <green>%world%");
        file.setDefault(WORLD_UNLOAD_FAILED, "%prefix% <red>Failed to unload the world <dark_red>%world%");
        file.setDefault(WORLD_DELETE_FAILED, "%prefix% <red>Failed to deleted the world <dark_red>%world%");
        file.setDefault(NO_PERMISSION, "%prefix% <red>You have no rights <dark_gray>(<dark_red>%permission%<dark_gray>)");
        file.setDefault(INVALID_SENDER, "%prefix% <red>You cannot use this command");
        file.setDefault(INCOMPLETE_ARGUMENT, "%prefix% <red>Incomplete command argument");
        file.save();
    }

    private static void initGerman() {
        var file = MessageFile.getOrCreate(Locale.forLanguageTag("de-DE"));
        file.setDefault(WORLD_SAVED, "%prefix% <white>Die welt <green>%world% <white>wurde gespeichert");
        file.setDefault(WORLD_LIST, "%prefix% <gray>Welten <dark_gray>(<green>%amount%<dark_gray>): <white>%worlds%");
        file.setDefault(WORLD_EXISTS, "%prefix% <red>Eine Welt mit dem namen <dark_red>%world%<red> existiert bereits");
        file.setDefault(WORLD_NOT_FOUND, "%prefix% <red>Eine Welt mit dem namen <dark_red>%world%<red> existiert nicht");
        file.setDefault(WORLD_INFO_NAME, "%prefix% <gray>Name<dark_gray>: <white>%world%");
        file.setDefault(WORLD_INFO_PLAYERS, "%prefix% <gray>Spieler<dark_gray>: <white>%players%");
        file.setDefault(WORLD_INFO_TYPE, "%prefix% <gray>Typ<dark_gray>: <white>%type%");
        file.setDefault(WORLD_INFO_ENVIRONMENT, "%prefix% <gray>Umfeld<dark_gray>: <white>%environment%");
        file.setDefault(WORLD_DELETE_SUCCEEDED, "%prefix% <white>Die Welt <green>%world%<white> wurde erfolgreich gelöscht");
        file.setDefault(WORLD_UNLOAD_FAILED, "%prefix% <red>Die Welt <green>%world%<red> konnte nicht entladen werden");
        file.setDefault(WORLD_DELETE_FAILED, "%prefix% <red>Die Welt <dark_red>%world%<red> konnte nicht gelöscht werden");
        file.setDefault(NO_PERMISSION, "%prefix%<red> Darauf hast du keine rechte <dark_gray>(<dark_red>%permission%<dark_gray>)");
        file.setDefault(INVALID_SENDER, "%prefix%<red> Du kannst diesen command nicht nutzen");
        file.setDefault(INCOMPLETE_ARGUMENT, "%prefix% <red>Unvollständiges command Argument");
        file.save();
    }
}
