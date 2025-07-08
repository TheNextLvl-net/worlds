package net.thenextlvl.worlds.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;

public class WorldOverviewDialog {
    public static Dialog create() {
        return Dialog.create(builder -> builder.empty()
                .type(DialogType.notice())
                .base(DialogBase.builder(Component.text("Select World"))
                        .canCloseWithEscape(true)
                        .build()
                ));
    }
}
