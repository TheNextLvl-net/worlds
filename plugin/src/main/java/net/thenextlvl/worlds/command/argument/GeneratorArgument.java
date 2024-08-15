package net.thenextlvl.worlds.command.argument;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.worlds.WorldsPlugin;
import net.thenextlvl.worlds.command.suggestion.GeneratorSuggestionProvider;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class GeneratorArgument extends WrappedArgumentType<String, GeneratorArgument.Generator> {
    public GeneratorArgument(WorldsPlugin plugin) {
        super(StringArgumentType.string(), (reader, type) -> {
            var split = type.split(":", 2);
            var generator = plugin.getServer().getPluginManager().getPlugin(split[0]);
            Preconditions.checkNotNull(generator, "Unknown plugin");
            Preconditions.checkState(generator.isEnabled(), "Plugin is not enabled");
            Preconditions.checkState(plugin.generatorView().hasGenerator(generator), "Plugin has no generator");
            return new Generator(generator, split.length > 1 ? split[1] : null);
        }, new GeneratorSuggestionProvider(plugin));
    }

    public record Generator(Plugin plugin, @Nullable String id) {
    }
}
