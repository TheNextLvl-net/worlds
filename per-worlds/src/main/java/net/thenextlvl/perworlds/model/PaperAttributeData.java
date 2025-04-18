package net.thenextlvl.perworlds.model;

import net.thenextlvl.perworlds.data.AttributeData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PaperAttributeData implements AttributeData {
    private final Attribute attribute;
    private double baseValue;

    public PaperAttributeData(Attribute attribute, double baseValue) {
        this.attribute = attribute;
        this.baseValue = baseValue;
    }

    public PaperAttributeData(AttributeInstance instance) {
        this(instance.getAttribute(), instance.getBaseValue());
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
