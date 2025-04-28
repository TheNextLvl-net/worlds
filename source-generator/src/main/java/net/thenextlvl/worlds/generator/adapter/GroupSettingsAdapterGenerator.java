package net.thenextlvl.worlds.generator.adapter;

import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import core.nbt.serialization.TagAdapter;
import core.nbt.serialization.TagDeserializationContext;
import core.nbt.serialization.TagSerializationContext;
import core.nbt.tag.CompoundTag;
import core.nbt.tag.Tag;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.worlds.generator.Generator;
import org.jspecify.annotations.NullMarked;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.util.Arrays;

@NullMarked
public class GroupSettingsAdapterGenerator extends Generator {
    public GroupSettingsAdapterGenerator() {
        super("net.thenextlvl.perworlds.adapter", "GroupSettingsAdapter");
    }

    @Override
    protected TypeSpec generate() {
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(
                        TagAdapter.class,
                        GroupSettings.class
                ))
                .addMethod(getDeserializeMethodSpec())
                .addMethod(getSerializeMethodSpec())
                .addAnnotation(NullMarked.class)
                .build();
    }

    private static MethodSpec getDeserializeMethodSpec() {
        var builder = MethodSpec.methodBuilder("deserialize")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(GroupSettings.class)
                .addParameter(Tag.class, "tag")
                .addParameter(TagDeserializationContext.class, "context")
                .addStatement("var root = tag.getAsCompound()")
                .addStatement("var settings = new net.thenextlvl.perworlds.group.PaperGroupSettings()");
        Arrays.stream(GroupSettings.class.getDeclaredMethods())
                .map(Method::getName)
                .distinct()
                .forEach(s -> builder.addStatement("root.optional($S).map($T::getAsBoolean).ifPresent(settings::$L);", s, Tag.class, s));
        return builder
                .addStatement("return settings")
                .build();
    }

    private static MethodSpec getSerializeMethodSpec() {
        var builder = MethodSpec.methodBuilder("serialize")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(CompoundTag.class)
                .addParameter(GroupSettings.class, "settings")
                .addParameter(TagSerializationContext.class, "context")
                .addStatement("var tag = new $T()", CompoundTag.class);
        Arrays.stream(GroupSettings.class.getDeclaredMethods())
                .map(Method::getName)
                .distinct()
                .forEach(s -> builder.addStatement("tag.add($S, settings.$L())", s, s));
        return builder
                .addStatement("return tag")
                .build();
    }
}
