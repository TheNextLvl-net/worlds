package net.thenextlvl.worlds.view;

import net.thenextlvl.worlds.api.view.GeneratorView;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@NullMarked
public class PluginGeneratorView implements GeneratorView {
    @Override
    public boolean hasGenerator(Plugin plugin) {
        return hasChunkGenerator(plugin.getClass()) || hasBiomeProvider(plugin.getClass());
    }

    @Override
    public boolean hasChunkGenerator(Class<? extends Plugin> clazz) {
        return overridesMethod(clazz, "getDefaultWorldGenerator", ChunkGenerator.class);
    }

    @Override
    public boolean hasBiomeProvider(Class<? extends Plugin> clazz) {
        return overridesMethod(clazz, "getDefaultBiomeProvider", BiomeProvider.class);
    }

    private boolean overridesMethod(Class<?> clazz, String methodName, Class<?> returnType) {
        try {
            var lookup = MethodHandles.lookup();
            var methodType = MethodType.methodType(returnType, String.class, String.class);
            var virtual = lookup.findVirtual(clazz, methodName, methodType);
            return lookup.revealDirect(virtual).getDeclaringClass().equals(clazz);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return false;
        }
    }
}
