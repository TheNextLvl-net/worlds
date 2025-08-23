package net.thenextlvl.worlds.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

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

    protected void addOptions(ArgumentBuilder<CommandSourceStack, ?> parent, boolean fixed, Set<Option> options, @Nullable Consumer<ArgumentBuilder<CommandSourceStack, ?>> consumer) {
        options.forEach(option -> {
            var argument = Commands.argument(option.argument, option.type).executes(this::execute);
            if (consumer != null) consumer.accept(argument);

            if (!fixed) {
                var nextRemaining = new HashSet<>(options);
                nextRemaining.remove(option);
                addOptions(argument, false, nextRemaining, consumer);
            }

            parent.then(Commands.literal(option.name).then(argument));
        });
    }

    protected <T> Optional<T> tryGetArgument(CommandContext<CommandSourceStack> context, String name, Class<T> type) {
        try {
            return Optional.ofNullable(context.getArgument(name, type));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("No such argument '" + name + "' exists on this command")) return Optional.empty();
            throw e;
        }
    }

    protected abstract int execute(CommandContext<CommandSourceStack> context);
}
