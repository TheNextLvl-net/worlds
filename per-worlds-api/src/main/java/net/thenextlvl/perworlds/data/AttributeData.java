package net.thenextlvl.perworlds.data;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface AttributeData {
    Attribute attribute();

    AttributeData baseValue(double value);

    double baseValue();

    void apply(Player player);
}
