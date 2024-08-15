package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.api.link.Relative;
import org.bukkit.World;

import java.util.Arrays;
import java.util.Objects;

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
        var sender = context.getSource().getSender();
        var links = plugin.getServer().getWorlds().stream()
                .filter(world -> world.getEnvironment().equals(World.Environment.NORMAL))
                .<String>mapMulti((world, consumer) -> Arrays.stream(Relative.values())
                        .filter(relative -> !relative.equals(Relative.OVERWORLD))
                        .map(relative -> plugin.linkController().getTarget(world, relative).orElse(null))
                        .filter(Objects::nonNull)
                        .forEach(key -> consumer.accept(world.key().asString() + " <-> " + key.asString())))
                .toList();
        if (links.isEmpty()) plugin.bundle().sendMessage(sender, "world.link.list.empty");
        else plugin.bundle().sendMessage(sender, "world.link.list",
                Placeholder.parsed("links", String.join(",<newline>", links)),
                Placeholder.parsed("amount", String.valueOf(links.size())));
        return links.isEmpty() ? 0 : Command.SINGLE_SUCCESS;
    }
}
