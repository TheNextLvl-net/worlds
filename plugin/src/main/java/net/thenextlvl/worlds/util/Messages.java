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

    public static final MessageKey<Audience> WORLD_SAVED = new MessageKey<>("world.saved", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_CREATE_SUCCEEDED = new MessageKey<>("world.create.success", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_CREATE_FAILED = new MessageKey<>("world.create.failed", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_IMPORT_SUCCEEDED = new MessageKey<>("world.import.success", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_IMPORT_FAILED = new MessageKey<>("world.import.failed", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_LIST = new MessageKey<>("world.list", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_EXISTS = new MessageKey<>("world.exists", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_NOT_FOUND = new MessageKey<>("world.exists.not", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_PRESET_INVALID = new MessageKey<>("world.preset.invalid", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_PRESET_FLAT = new MessageKey<>("world.preset.flat", plugin.formatter()).register();

    public static final MessageKey<Audience> WORLD_INFO_NAME = new MessageKey<>("world.info.name", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_INFO_PLAYERS = new MessageKey<>("world.info.players", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_INFO_TYPE = new MessageKey<>("world.info.type", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_INFO_ENVIRONMENT = new MessageKey<>("world.info.environment", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_INFO_GENERATOR = new MessageKey<>("world.info.generator", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_INFO_SEED = new MessageKey<>("world.info.seed", plugin.formatter()).register();

    public static final MessageKey<Audience> WORLD_DELETE_DISALLOWED = new MessageKey<>("world.delete.disallowed", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_DELETE_SUCCEEDED = new MessageKey<>("world.delete.success", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_DELETE_FAILED = new MessageKey<>("world.delete.failed", plugin.formatter()).register();
    public static final MessageKey<Audience> WORLD_UNLOAD_FAILED = new MessageKey<>("world.unload.failed", plugin.formatter()).register();

    public static final MessageKey<Audience> LINK_EXISTS = new MessageKey<>("link.exists", plugin.formatter()).register();
    public static final MessageKey<Audience> LINK_NOT_FOUND = new MessageKey<>("link.exists.not", plugin.formatter()).register();

    public static final MessageKey<Audience> LINK_DELETED = new MessageKey<>("link.deleted", plugin.formatter()).register();
    public static final MessageKey<Audience> LINK_CREATED = new MessageKey<>("link.created", plugin.formatter()).register();
    public static final MessageKey<Audience> LINK_LIST_EMPTY = new MessageKey<>("link.list.empty", plugin.formatter()).register();
    public static final MessageKey<Audience> LINK_LIST = new MessageKey<>("link.list", plugin.formatter()).register();

    public static final MessageKey<Audience> IMAGE_DELETE_FAILED = new MessageKey<>("image.delete.failed", plugin.formatter()).register();
    public static final MessageKey<Audience> IMAGE_NOT_FOUND = new MessageKey<>("image.exists.not", plugin.formatter()).register();
    public static final MessageKey<Audience> ENTER_WORLD_NAME = new MessageKey<>("world.name.absent", plugin.formatter()).register();
    public static final MessageKey<Audience> PLAYER_NOT_ONLINE = new MessageKey<>("player.not.online", plugin.formatter());
    public static final MessageKey<Audience> KICK_WORLD_DELETED = new MessageKey<>("kick.world.deleted", plugin.formatter()).register();

    public static final MessageKey<Audience> NO_PERMISSION = new MessageKey<>("command.permission", plugin.formatter()).register();
    public static final MessageKey<Audience> INVALID_SENDER = new MessageKey<>("command.sender", plugin.formatter()).register();
    public static final MessageKey<Audience> INVALID_ARGUMENT = new MessageKey<>("command.argument.invalid", plugin.formatter()).register();
    public static final MessageKey<Audience> FLAG_COMBINATION = new MessageKey<>("command.flag.combination", plugin.formatter()).register();

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
        file.setDefault(WORLD_CREATE_SUCCEEDED, "%prefix% <white>Successfully created the world <green>%world%");
        file.setDefault(WORLD_CREATE_FAILED, "%prefix% <red>Failed to create the world <dark_red>%world%");
        file.setDefault(WORLD_IMPORT_SUCCEEDED, "%prefix% <white>Successfully imported the world <green>%world%");
        file.setDefault(WORLD_IMPORT_FAILED, "%prefix% <red>Failed to import the world <dark_red>%world%");
        file.setDefault(WORLD_LIST, "%prefix% <gray>Worlds <dark_gray>(<green>%amount%<dark_gray>): <white>%worlds%");
        file.setDefault(WORLD_EXISTS, "%prefix% <red>A world called <dark_red>%world%<red> does already exist");
        file.setDefault(WORLD_NOT_FOUND, "%prefix% <red>A world called <dark_red>%world%<red> does not exist");
        file.setDefault(WORLD_PRESET_INVALID, "%prefix% <red>The world preset is not a valid json string");
        file.setDefault(WORLD_PRESET_FLAT, "%prefix% <red>Presets are only applicable on flat maps");
        file.setDefault(WORLD_INFO_NAME, "%prefix% <gray>Name<dark_gray>: <white>%world%");
        file.setDefault(WORLD_INFO_PLAYERS, "%prefix% <gray>Players<dark_gray>: <white>%players%");
        file.setDefault(WORLD_INFO_TYPE, "%prefix% <gray>Type<dark_gray>: <white>%type%");
        file.setDefault(WORLD_INFO_ENVIRONMENT, "%prefix% <gray>Environment<dark_gray>: <white>%environment%");
        file.setDefault(WORLD_INFO_GENERATOR, "%prefix% <gray>Generator<dark_gray>: <white>%generator%");
        file.setDefault(WORLD_INFO_SEED, "%prefix% <gray>Seed<dark_gray>: <white>" +
                "<hover:show_text:\"<white>Click to Copy to Clipboard\"><click:copy_to_clipboard:%seed%>%seed%");
        file.setDefault(WORLD_DELETE_DISALLOWED, "%prefix% <red>The world <dark_red>%world% <red>is not deletable");
        file.setDefault(WORLD_DELETE_SUCCEEDED, "%prefix% <white>Successfully deleted the world <green>%world%");
        file.setDefault(WORLD_UNLOAD_FAILED, "%prefix% <red>Failed to unload the world <dark_red>%world%");
        file.setDefault(WORLD_DELETE_FAILED, "%prefix% <red>Failed to deleted the world <dark_red>%world%");
        file.setDefault(LINK_EXISTS, "%prefix% <red>The link <dark_red>%link% <red>does already exists");
        file.setDefault(LINK_NOT_FOUND, "%prefix% <red>The link <dark_red>%link% <red>does not exist");
        file.setDefault(LINK_DELETED, "%prefix% <white>Deleted the link <green>%link%");
        file.setDefault(LINK_CREATED, "%prefix% <white>Created a new link <green>%link%");
        file.setDefault(LINK_LIST_EMPTY, "%prefix% <red>There are no links yet");
        file.setDefault(LINK_LIST, "%prefix% <gray>Links <dark_gray>(<green>%amount%<dark_gray>): <white>%links%");
        file.setDefault(IMAGE_DELETE_FAILED, "%prefix% <red>Failed to deleted the image <dark_red>%image%");
        file.setDefault(IMAGE_NOT_FOUND, "%prefix% <red>An image called <dark_red>%image% <red>does not exist");
        file.setDefault(ENTER_WORLD_NAME, "%prefix% <red>You have to provide a world");
        file.setDefault(PLAYER_NOT_ONLINE, "%prefix% <red>The player <dark_red>%player%<red> is not online");
        file.setDefault(KICK_WORLD_DELETED, "<red>The world you where currently in, got deleted");
        file.setDefault(NO_PERMISSION, "%prefix% <red>You have no rights <dark_gray>(<dark_red>%permission%<dark_gray>)");
        file.setDefault(INVALID_SENDER, "%prefix% <red>You cannot use this command");
        file.setDefault(INVALID_ARGUMENT, "%prefix% <red>Invalid command argument");
        file.setDefault(FLAG_COMBINATION, "%prefix% <red>You can't combine the flag <dark_red>%flag-1% <red>with <dark_red>%flag-2%");
        file.save();
    }

    private static void initGerman() {
        var file = MessageFile.getOrCreate(Locale.forLanguageTag("de-DE"));
        file.setDefault(WORLD_SAVED, "%prefix% <white>Die Welt <green>%world% <white>wurde gespeichert");
        file.setDefault(WORLD_CREATE_SUCCEEDED, "%prefix% <white>Die Welt <green>%world% <white>wurde erfolgreich erstellt");
        file.setDefault(WORLD_CREATE_FAILED, "%prefix% <red>Die Welt <dark_red>%world% <red>konnte nicht erstellt werden");
        file.setDefault(WORLD_IMPORT_SUCCEEDED, "%prefix% <white>Die Welt <green>%world% <white>wurde erfolgreich importiert");
        file.setDefault(WORLD_IMPORT_FAILED, "%prefix% <red>Die Welt <dark_red>%world% <red>konnte nicht importiert werden");
        file.setDefault(WORLD_LIST, "%prefix% <gray>Welten <dark_gray>(<green>%amount%<dark_gray>): <white>%worlds%");
        file.setDefault(WORLD_EXISTS, "%prefix% <red>Eine Welt mit dem namen <dark_red>%world%<red> existiert bereits");
        file.setDefault(WORLD_NOT_FOUND, "%prefix% <red>Eine Welt mit dem namen <dark_red>%world%<red> existiert nicht");
        file.setDefault(WORLD_PRESET_INVALID, "%prefix% <red>Die Welten Voreinstellung ist kein gültiger json Text");
        file.setDefault(WORLD_PRESET_FLAT, "%prefix% <red>Voreinstellungen sind nur auf flache Welten anwendbar");
        file.setDefault(WORLD_INFO_NAME, "%prefix% <gray>Name<dark_gray>: <white>%world%");
        file.setDefault(WORLD_INFO_PLAYERS, "%prefix% <gray>Spieler<dark_gray>: <white>%players%");
        file.setDefault(WORLD_INFO_TYPE, "%prefix% <gray>Typ<dark_gray>: <white>%type%");
        file.setDefault(WORLD_INFO_ENVIRONMENT, "%prefix% <gray>Umfeld<dark_gray>: <white>%environment%");
        file.setDefault(WORLD_INFO_GENERATOR, "%prefix% <gray>Generator<dark_gray>: <white>%generator%");
        file.setDefault(WORLD_INFO_SEED, "%prefix% <gray>Startwert<dark_gray>: <white>" +
                "<hover:show_text:\"<white>Klicken, um in die Zwischenablage zu kopieren\"><click:copy_to_clipboard:%seed%>%seed%");
        file.setDefault(WORLD_DELETE_DISALLOWED, "%prefix% <red>Die Welt <dark_red>%world% <red>ist nicht löschbar");
        file.setDefault(WORLD_DELETE_SUCCEEDED, "%prefix% <white>Die Welt <green>%world%<white> wurde erfolgreich gelöscht");
        file.setDefault(WORLD_UNLOAD_FAILED, "%prefix% <red>Die Welt <green>%world%<red> konnte nicht entladen werden");
        file.setDefault(WORLD_DELETE_FAILED, "%prefix% <red>Die Welt <dark_red>%world%<red> konnte nicht gelöscht werden");
        file.setDefault(IMAGE_DELETE_FAILED, "%prefix% <red>Das Abbild <dark_red>%image%<red> konnte nicht gelöscht werden");
        file.setDefault(LINK_EXISTS, "%prefix% <red>Der Link <dark_red>%link% <red>existiert bereits");
        file.setDefault(LINK_NOT_FOUND, "%prefix% <red>Der Link <dark_red>%link% <red>existiert nicht");
        file.setDefault(LINK_DELETED, "%prefix% <white>Der Link <green>%link% <white>wurde gelöscht");
        file.setDefault(LINK_CREATED, "%prefix% <white>Der Link <green>%link% <white>wurde erstellt");
        file.setDefault(LINK_LIST_EMPTY, "%prefix% <red>Es existieren noch keine links");
        file.setDefault(LINK_LIST, "%prefix% <gray>Links <dark_gray>(<green>%amount%<dark_gray>): <white>%links%");
        file.setDefault(IMAGE_NOT_FOUND, "%prefix% <red>Ein Abbild mit dem namen <dark_red>%image% <red>existiert nicht");
        file.setDefault(ENTER_WORLD_NAME, "%prefix% <red>Du musst einen Welt angeben");
        file.setDefault(PLAYER_NOT_ONLINE, "%prefix% <red>Der Spieler <dark_red>%player%<red> ist nicht online");
        file.setDefault(KICK_WORLD_DELETED, "<red>Die Welt in der du eben warst, wurde gelöscht");
        file.setDefault(NO_PERMISSION, "%prefix%<red> Darauf hast du keine rechte <dark_gray>(<dark_red>%permission%<dark_gray>)");
        file.setDefault(INVALID_SENDER, "%prefix%<red> Du kannst diesen command nicht nutzen");
        file.setDefault(INVALID_ARGUMENT, "%prefix% <red>Ungültiges command Argument");
        file.setDefault(FLAG_COMBINATION, "%prefix% <red>Du kannst <dark_red>%flag-1% <red>und <dark_red>%flag-2% <red>nicht kombinieren");
        file.save();
    }
}
