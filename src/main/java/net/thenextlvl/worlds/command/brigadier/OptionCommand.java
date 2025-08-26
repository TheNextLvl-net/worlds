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
    protected OptionCommand(WorldsPlugin plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    protected record Option(String name, String argument, ArgumentType<?> type) {
        public Option(String name, ArgumentType<?> type) {
            this(name, name, type);
        }
    }

    protected abstract RequiredArgumentBuilder<CommandSourceStack, ?> createCommand();

    protected void addOptions(ArgumentBuilder<CommandSourceStack, ?> parent, boolean fixed, Set<Option> options, @Nullable Consumer<ArgumentBuilder<CommandSourceStack, ?>> consumer) {
        options.forEach(option -> {
            var argument = Commands.argument(option.argument, option.type).executes(this);
            if (consumer != null) consumer.accept(argument);

            if (!fixed) {
                var nextRemaining = new HashSet<>(options);
                nextRemaining.remove(option);
                addOptions(argument, false, nextRemaining, consumer);
            }

            parent.then(Commands.literal(option.name).then(argument));
        });
    }
}
