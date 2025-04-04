package net.thenextlvl.perworlds.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.key.Key;
import net.thenextlvl.perworlds.WorldGroup;
import org.bukkit.Keyed;

import java.util.concurrent.CompletableFuture;

public class GroupMemberSuggestionProvider<S> implements SuggestionProvider<S> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var group = context.getLastChild().getArgument("group", WorldGroup.class);
        group.getWorlds().stream().map(Keyed::key).map(Key::asString).forEach(builder::suggest);
        return builder.buildFuture();
    }
}
