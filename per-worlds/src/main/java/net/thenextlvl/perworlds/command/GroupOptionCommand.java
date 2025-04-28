package net.thenextlvl.perworlds.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import org.jspecify.annotations.NullMarked;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.thenextlvl.perworlds.command.GroupCommand.groupArgument;

@NullMarked
class GroupOptionCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> create(SharedWorlds commons) {
        return Commands.literal("option")
                .requires(source -> source.getSender().hasPermission("perworlds.command.group.option"))
                .then(option("absorption", GroupSettings::absorption, GroupSettings::absorption, commons))
                .then(option("advancements", GroupSettings::advancements, GroupSettings::advancements, commons))
                .then(option("arrowsInBody", GroupSettings::arrowsInBody, GroupSettings::arrowsInBody, commons))
                .then(option("attributes", GroupSettings::attributes, GroupSettings::attributes, commons))
                .then(option("beeStingersInBody", GroupSettings::beeStingersInBody, GroupSettings::beeStingersInBody, commons))
                .then(option("chat", GroupSettings::chat, GroupSettings::chat, commons))
                .then(option("difficulty", GroupSettings::difficulty, GroupSettings::difficulty, commons))
                .then(option("enabled", GroupSettings::enabled, GroupSettings::enabled, commons))
                .then(option("endCredits", GroupSettings::endCredits, GroupSettings::endCredits, commons))
                .then(option("enderChest", GroupSettings::enderChest, GroupSettings::enderChest, commons))
                .then(option("exhaustion", GroupSettings::exhaustion, GroupSettings::exhaustion, commons))
                .then(option("experience", GroupSettings::experience, GroupSettings::experience, commons))
                .then(option("fallDistance", GroupSettings::fallDistance, GroupSettings::fallDistance, commons))
                .then(option("fireTicks", GroupSettings::fireTicks, GroupSettings::fireTicks, commons))
                .then(option("flyState", GroupSettings::flyState, GroupSettings::flyState, commons))
                .then(option("foodLevel", GroupSettings::foodLevel, GroupSettings::foodLevel, commons))
                .then(option("freezeTicks", GroupSettings::freezeTicks, GroupSettings::freezeTicks, commons))
                .then(option("gameMode", GroupSettings::gameMode, GroupSettings::gameMode, commons))
                .then(option("gameRules", GroupSettings::gameRules, GroupSettings::gameRules, commons))
                .then(option("gliding", GroupSettings::gliding, GroupSettings::gliding, commons))
                .then(option("health", GroupSettings::health, GroupSettings::health, commons))
                .then(option("hotbarSlot", GroupSettings::hotbarSlot, GroupSettings::hotbarSlot, commons))
                .then(option("inventory", GroupSettings::inventory, GroupSettings::inventory, commons))
                .then(option("invulnerable", GroupSettings::invulnerable, GroupSettings::invulnerable, commons))
                .then(option("lastDeathLocation", GroupSettings::lastDeathLocation, GroupSettings::lastDeathLocation, commons))
                .then(option("lastLocation", GroupSettings::lastLocation, GroupSettings::lastLocation, commons))
                .then(option("lockFreezeTicks", GroupSettings::lockFreezeTicks, GroupSettings::lockFreezeTicks, commons))
                .then(option("portalCooldown", GroupSettings::portalCooldown, GroupSettings::portalCooldown, commons))
                .then(option("potionEffects", GroupSettings::potionEffects, GroupSettings::potionEffects, commons))
                .then(option("recipes", GroupSettings::recipes, GroupSettings::recipes, commons))
                .then(option("remainingAir", GroupSettings::remainingAir, GroupSettings::remainingAir, commons))
                .then(option("respawnLocation", GroupSettings::respawnLocation, GroupSettings::respawnLocation, commons))
                .then(option("saturation", GroupSettings::saturation, GroupSettings::saturation, commons))
                .then(option("score", GroupSettings::score, GroupSettings::score, commons))
                .then(option("statistics", GroupSettings::statistics, GroupSettings::statistics, commons))
                .then(option("tabList", GroupSettings::tabList, GroupSettings::tabList, commons))
                .then(option("time", GroupSettings::time, GroupSettings::time, commons))
                .then(option("velocity", GroupSettings::velocity, GroupSettings::velocity, commons))
                .then(option("visualFire", GroupSettings::visualFire, GroupSettings::visualFire, commons))
                .then(option("wardenSpawnTracker", GroupSettings::wardenSpawnTracker, GroupSettings::wardenSpawnTracker, commons))
                .then(option("weather", GroupSettings::weather, GroupSettings::weather, commons))
                .then(option("worldBorder", GroupSettings::worldBorder, GroupSettings::worldBorder, commons));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> option(String name, Function<GroupSettings, Boolean> getter,
                                                                 BiConsumer<GroupSettings, Boolean> setter,
                                                                 SharedWorlds commons) {
        return Commands.literal(name).then(groupArgument(commons)
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(context -> set(context, name, getter, setter, commons)))
                .executes(context -> query(context, name, getter, commons)));
    }

    private static int set(CommandContext<CommandSourceStack> context, String option, Function<GroupSettings, Boolean> getter,
                           BiConsumer<GroupSettings, Boolean> setter, SharedWorlds commons) {
        var value = context.getArgument("value", boolean.class);
        var group = context.getArgument("group", WorldGroup.class);
        var success = !getter.apply(group.getSettings()).equals(value);
        if (success) setter.accept(group.getSettings(), value);
        var message = success ? "group.option.set" : "nothing.changed";
        commons.bundle().sendMessage(context.getSource().getSender(), message,
                Formatter.booleanChoice("value", value),
                Placeholder.unparsed("group", group.getName()),
                Placeholder.unparsed("option", option));
        return Command.SINGLE_SUCCESS;
    }

    private static int query(CommandContext<CommandSourceStack> context, String option, Function<GroupSettings, Boolean> getter, SharedWorlds commons) {
        var group = context.getArgument("group", WorldGroup.class);
        var value = getter.apply(group.getSettings());
        commons.bundle().sendMessage(context.getSource().getSender(), "group.option",
                Formatter.booleanChoice("value", value),
                Placeholder.unparsed("group", group.getName()),
                Placeholder.unparsed("option", option));
        return Command.SINGLE_SUCCESS;
    }
}
