package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
public final class LevelPathArgument implements SimpleArgumentType<Path, String> {
    private final WorldsPlugin plugin;

    public LevelPathArgument(WorldsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Path convert(StringReader reader, String type) {
        return plugin.getServer().getWorldContainer().toPath().resolve(type);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
