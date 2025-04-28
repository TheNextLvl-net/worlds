package net.thenextlvl.perworlds.data;

import org.bukkit.attribute.Attribute;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface AttributeData {
    Attribute attribute();

    AttributeData baseValue(double value);

    double baseValue();
}
