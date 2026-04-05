package net.thenextlvl.worlds.command.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public abstract class OptionCommand extends SimpleCommand {
    protected OptionCommand(final WorldsPlugin plugin, final String name, final String permission) {
        super(plugin, name, permission);
    }

    public record Option(String name, ArgumentType<?> type, @Nullable String incompatible) {
        public Option(final String name, final ArgumentType<?> type) {
            this(name, type, null);
        }
    }

    protected abstract RequiredArgumentBuilder<CommandSourceStack, ?> createCommand();

    protected void addOptions(final ArgumentBuilder<CommandSourceStack, ?> parent, final boolean fixed, final Set<Option> options, @Nullable final Consumer<ArgumentBuilder<CommandSourceStack, ?>> consumer) {
        options.forEach(option -> {
            final var argument = Commands.argument(option.name, option.type).executes(this);
            if (consumer != null) consumer.accept(argument);

            if (!fixed) {
                final var nextRemaining = new HashSet<>(options);
                nextRemaining.remove(option);
                if (option.incompatible() != null) nextRemaining.removeIf(o -> {
                    return o.name().equals(option.incompatible());
                });
                addOptions(argument, false, nextRemaining, consumer);
            }

            parent.then(Commands.literal(option.name).then(argument));
        });
    }
}
