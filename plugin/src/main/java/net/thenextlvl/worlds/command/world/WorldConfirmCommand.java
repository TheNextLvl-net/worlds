package net.thenextlvl.worlds.command.world;

import cloud.commandframework.Command;
import org.bukkit.command.CommandSender;

import static net.thenextlvl.worlds.command.world.WorldCommand.confirmationManager;

class WorldConfirmCommand {
    static Command.Builder<CommandSender> create(Command.Builder<CommandSender> builder) {
        return builder.literal("confirm")
                .permission(sender -> confirmationManager.getPending(sender).isPresent())
                .handler(confirmationManager.createConfirmationExecutionHandler());
    }
}
