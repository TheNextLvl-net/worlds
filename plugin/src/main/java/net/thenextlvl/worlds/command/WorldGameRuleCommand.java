package net.thenextlvl.worlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.BrigadierCommand;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static net.thenextlvl.worlds.command.WorldCommand.worldArgument;

@NullMarked
final class WorldGameRuleCommand extends BrigadierCommand {
    private WorldGameRuleCommand(final WorldsPlugin plugin) {
        super(plugin, "gamerule", "worlds.command.gamerule");
    }

    public static ArgumentBuilder<CommandSourceStack, ?> create(final WorldsPlugin plugin) {
        final var command = new WorldGameRuleCommand(plugin);
        final var world = worldArgument(plugin);
        final var features = plugin.handler().getEnabledFeatures();
        RegistryAccess.registryAccess().getRegistry(RegistryKey.GAME_RULE).stream()
                .filter(rule -> features.containsAll(rule.requiredFeatures()))
                .forEach(rule -> world.then(command.rule(rule)));
        return command.create().then(world);
    }

    private <T> ArgumentBuilder<CommandSourceStack, ?> rule(final GameRule<T> rule) {
        final var literal = Commands.literal(rule.key().asMinimalString()).executes(context -> query(context, rule));
        final var type = argumentType(rule);
        if (type != null) literal.then(Commands.argument("value", type).executes(context -> set(context, rule)));
        return literal;
    }

    private static @Nullable ArgumentType<?> argumentType(final GameRule<?> rule) {
        if (rule.getType() == Boolean.class) return BoolArgumentType.bool();
        if (rule.getType() == Integer.class) return IntegerArgumentType.integer();
        return null;
    }

    private <T> int query(final CommandContext<CommandSourceStack> context, final GameRule<T> rule) {
        final var world = context.getArgument("world", World.class);
        final var value = world.getGameRuleValue(rule);
        plugin.bundle().sendMessage(context.getSource().getSender(), "world.gamerule.query",
                Placeholder.parsed("rule", rule.key().asMinimalString()),
                Placeholder.parsed("world", world.key().asString()),
                Placeholder.parsed("value", String.valueOf(value)));
        return Command.SINGLE_SUCCESS;
    }

    private <T> int set(final CommandContext<CommandSourceStack> context, final GameRule<T> rule) {
        final var world = context.getArgument("world", World.class);
        final var value = context.getArgument("value", rule.getType());
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            final var success = !value.equals(world.getGameRuleValue(rule)) && world.setGameRule(rule, value);
            final var message = success ? "world.gamerule.set" : "world.gamerule.unchanged";
            plugin.bundle().sendMessage(context.getSource().getSender(), message,
                    Placeholder.parsed("rule", rule.key().asMinimalString()),
                    Placeholder.parsed("world", world.key().asString()),
                    Placeholder.parsed("value", String.valueOf(value)));
        });
        return Command.SINGLE_SUCCESS;
    }

}
