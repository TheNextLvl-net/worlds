package net.thenextlvl.worlds.command.argument;

import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.thenextlvl.worlds.command.suggestion.DimensionSuggestionProvider;
import org.bukkit.World;

@SuppressWarnings("UnstableApiUsage")
public class DimensionArgument extends WrappedArgumentType<Key, World.Environment> {
    public DimensionArgument() {
        super(ArgumentTypes.key(), (reader, type) -> {
            return switch (type.asString()) {
                case "minecraft:overworld" -> World.Environment.NORMAL;
                case "minecraft:the_end" -> World.Environment.THE_END;
                case "minecraft:the_nether" -> World.Environment.NETHER;
                default -> World.Environment.CUSTOM;
            };
        }, new DimensionSuggestionProvider());
    }
}
