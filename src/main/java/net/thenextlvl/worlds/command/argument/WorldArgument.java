package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import core.paper.command.ComponentCommandExceptionType;
import core.paper.command.WrappedArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WorldArgument extends WrappedArgumentType<Key, World> {
    public WorldArgument(WorldsPlugin plugin) {
        super(new KeyArgument(), (reader, type) -> {
            var world = plugin.getServer().getWorld(type);
            if (world != null) return world;
            throw new ComponentCommandExceptionType(
                    Component.text("Unknown dimension: '" + type + "'")
            ).createWithContext(reader);
        });
    }

    @Override
    public ArgumentType<Key> getNativeType() {
        return ArgumentTypes.key();
    }
}
