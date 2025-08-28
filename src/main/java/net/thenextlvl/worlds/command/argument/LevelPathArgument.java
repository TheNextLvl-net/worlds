package net.thenextlvl.worlds.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.LevelSuggestionProvider;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;

@NullMarked
public final class LevelPathArgument extends LevelSuggestionProvider implements SimpleArgumentType<Path, String> {
    public LevelPathArgument(WorldsPlugin plugin, boolean unknownLevels) {
        super(plugin, unknownLevels);
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
