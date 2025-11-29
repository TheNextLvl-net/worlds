package net.thenextlvl.worlds.command.brigadier;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ComponentCommandExceptionType extends SimpleCommandExceptionType {
    public ComponentCommandExceptionType(Component component) {
        super(MessageComponentSerializer.message().serialize(component));
    }
}
