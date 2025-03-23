package net.thenextlvl.perworlds.model;

import net.thenextlvl.perworlds.data.AttributeData;
import org.bukkit.attribute.Attribute;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperAttributeData implements AttributeData {
    private final Attribute attribute;
    private double baseValue;

    public PaperAttributeData(Attribute attribute, double baseValue) {
        this.attribute = attribute;
        this.baseValue = baseValue;
    }

    @Override
    public Attribute attribute() {
        return attribute;
    }

    @Override
    public AttributeData baseValue(double value) {
        this.baseValue = value;
        return this;
    }

    @Override
    public double baseValue() {
        return baseValue;
    }
}
