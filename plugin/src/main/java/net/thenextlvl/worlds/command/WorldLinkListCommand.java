package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.event.player.PlayerBedFailEnterEvent;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

@RequiredArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class WorldLinkListCommand {
    private final WorldsPlugin plugin;

    ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("list")
                .requires(source -> source.getSender().hasPermission("worlds.command.link.list"))
                .executes(this::list);
    }

    private int list(CommandContext<CommandSourceStack> context) {
        return Command.SINGLE_SUCCESS;
    }
}
