package net.thenextlvl.perworlds.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import core.paper.command.WrappedArgumentType;
import net.thenextlvl.perworlds.SharedWorlds;
import net.thenextlvl.perworlds.WorldGroup;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GroupArgument extends WrappedArgumentType<String, WorldGroup> {
    public GroupArgument(SharedWorlds commons) {
        super(StringArgumentType.string(), (reader, type) -> commons.groupProvider().getGroup(type)
                .orElseThrow(() -> new RuntimeException("Group not found")), (context, builder) -> {
            commons.groupProvider().getGroups().stream()
                    .map(WorldGroup::getName)
                    .filter(name -> name.contains(builder.getRemaining()))
                    .map(StringArgumentType::escapeIfRequired)
                    .forEach(builder::suggest);
            return builder.buildFuture();
        });
    }
}
