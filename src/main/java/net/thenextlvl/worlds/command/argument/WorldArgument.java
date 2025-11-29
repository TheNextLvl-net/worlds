package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.brigadier.ComponentCommandExceptionType;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldArgument implements SimpleArgumentType<World, Key> {
    private final WorldsPlugin plugin;

    public WorldArgument(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public World convert(StringReader reader, Key type) throws CommandSyntaxException {
        var world = plugin.getServer().getWorld(type);
        if (world != null) return world;
        throw new ComponentCommandExceptionType(
                Component.text("Unknown dimension: '" + type + "'")
        ).createWithContext(reader);
    }

    @Override
    public ArgumentType<Key> getNativeType() {
        return ArgumentTypes.key();
    }
}
