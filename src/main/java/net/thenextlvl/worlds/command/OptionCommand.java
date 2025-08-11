package net.thenextlvl.worlds.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

abstract class OptionCommand {
    protected final WorldsPlugin plugin;

    OptionCommand(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    protected record Option(String name, String argument, ArgumentType<?> type) {
        public Option(String name, ArgumentType<?> type) {
            this(name, name, type);
        }
    }

    protected void addOptions(ArgumentBuilder<CommandSourceStack, ?> parent, Set<Option> options) {
        for (var option : options) {
            var nextRemaining = new HashSet<>(options);
            nextRemaining.remove(option);

            var argument = Commands.argument(option.argument, option.type).executes(this::execute);
            addOptions(argument, nextRemaining);
            parent.then(Commands.literal(option.name).then(argument));
        }
    }

    protected <T> @Nullable T tryGetArgument(CommandContext<CommandSourceStack> context, String name, Class<T> type) {
        try {
            return context.getArgument(name, type);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("No such argument '" + name + "' exists on this command")) return null;
            throw e;
        }
    }

    protected abstract int execute(CommandContext<CommandSourceStack> context);
}
