package net.thenextlvl.perworlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.SharedWorlds;
import org.bukkit.Keyed;

import java.util.concurrent.CompletableFuture;

public class UnassignedWorldsSuggestionProvider<S> implements SuggestionProvider<S> {
    private final SharedWorlds commons;

    public UnassignedWorldsSuggestionProvider(SharedWorlds commons) {
        this.commons = commons;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        commons.getServer().getWorlds().stream()
                .filter(world -> !commons.groupProvider().hasGroup(world))
                .map(Keyed::key)
                .map(Key::asString)
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
