package net.thenextlvl.worlds.generator.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.worlds.generator.Generator;
import org.jspecify.annotations.NullMarked;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
                        JsonDeserializer.class,
                        GroupSettings.class
                ))
                .addSuperinterface(ParameterizedTypeName.get(
                        JsonSerializer.class,
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
                .addParameter(JsonElement.class, "element")
                .addParameter(Type.class, "type")
                .addParameter(JsonDeserializationContext.class, "context")
                .addStatement("var root = element.getAsJsonObject()")
                .addStatement("var settings = new net.thenextlvl.perworlds.group.PaperGroupSettings()");
        Arrays.stream(GroupSettings.class.getDeclaredMethods())
                .map(Method::getName)
                .distinct()
                .forEach(s -> builder.addStatement("if (root.has($S)) settings.$L(root.get($S).getAsBoolean())", s, s, s));
        return builder
                .addStatement("return settings")
                .build();
    }

    private static MethodSpec getSerializeMethodSpec() {
        var builder = MethodSpec.methodBuilder("serialize")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(JsonObject.class)
                .addParameter(GroupSettings.class, "settings")
                .addParameter(Type.class, "type")
                .addParameter(JsonSerializationContext.class, "context")
                .addStatement("var object = new $T()", JsonObject.class);
        Arrays.stream(GroupSettings.class.getDeclaredMethods())
                .map(Method::getName)
                .distinct()
                .forEach(s -> builder.addStatement("object.addProperty($S, settings.$L())", s, s));
        return builder
                .addStatement("return object")
                .build();
    }
}
