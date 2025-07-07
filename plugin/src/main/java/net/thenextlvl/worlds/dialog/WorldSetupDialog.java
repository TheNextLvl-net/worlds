package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.List;

public class WorldSetupDialog {
    public static final Dialog INSTANCE = Dialog.create(builder -> builder.empty()
            .type(DialogType.confirmation(
                    ActionButton.builder(Component.text("Create New World"))
                            .action(DialogAction.customClick((response, audience) -> {
                                var name = response.getText("name");
                                var validatedName = name != null && !name.isBlank() ? name : "New World";
                                
                                var gameMode = response.getText("game_mode");
                                var hardcore = gameMode != null && gameMode.equals("hardcore");

                                var plugin = JavaPlugin.getPlugin(WorldsPlugin.class);
                                
                                plugin.levelBuilder(Path.of(validatedName))
                                        .name(validatedName)
                                        .hardcore(hardcore)
                                        // .difficulty(difficulty)
                                        // .cheats(cheats)
                                        .build()
                                        .createAsync()
                                        .thenAccept(world -> {
                                            if (!(audience instanceof Player player)) return;
                                            player.teleportAsync(world.getSpawnLocation());
                                        });

                            }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build())).build(),
                    ActionButton.builder(Component.text("Cancel")).build()
            )).base(DialogBase.builder(Component.text("Create a new world"))
                    .canCloseWithEscape(true)
                    .externalTitle(Component.text("external?"))
                    .inputs(List.of(
                            DialogInput.text("name", 200, Component.text("World name"), true, "New World", 32, null),
                            DialogInput.singleOption("game_mode", 200, List.of(
                                    SingleOptionDialogInput.OptionEntry.create("survival", Component.text("Survival"), true),
                                    SingleOptionDialogInput.OptionEntry.create("hardcore", Component.text("Hardcore"), false),
                                    SingleOptionDialogInput.OptionEntry.create("creative", Component.text("Creative"), false)
                            ), Component.text("Game Mode"), true),
                            DialogInput.singleOption("difficulty", 200, List.of(
                                    SingleOptionDialogInput.OptionEntry.create("normal", Component.text("Normal"), true),
                                    SingleOptionDialogInput.OptionEntry.create("hard", Component.text("Hard"), false),
                                    SingleOptionDialogInput.OptionEntry.create("peaceful", Component.text("Peaceful"), false),
                                    SingleOptionDialogInput.OptionEntry.create("easy", Component.text("Easy"), false)
                            ), Component.text("Difficulty"), true),
                            DialogInput.singleOption("cheats", 200, List.of(
                                    SingleOptionDialogInput.OptionEntry.create("off", Component.text("Off"), true),
                                    SingleOptionDialogInput.OptionEntry.create("on", Component.text("On"), false)
                            ), Component.text("Allow Commands"), true)
                    )).build()
            ));
}
