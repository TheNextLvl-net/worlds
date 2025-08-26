package net.thenextlvl.worlds.command.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public class BrigadierCommand {
    protected final WorldsPlugin plugin;

    private final String permission;
    private final String name;

    protected BrigadierCommand(WorldsPlugin plugin, String name, String permission) {
        this.plugin = plugin;
        this.permission = permission;
        this.name = name;
    }

    protected LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal(name).requires(this::canUse);
    }

    protected boolean canUse(CommandSourceStack source) {
        return source.getSender().hasPermission(permission);
    }

    protected <T> Optional<T> tryGetArgument(CommandContext<CommandSourceStack> context, String name, Class<T> type) {
        try {
            return Optional.of(context.getArgument(name, type));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("No such argument '" + name + "' exists on this command"))
                return Optional.empty();
            throw e;
        }
    }
}
