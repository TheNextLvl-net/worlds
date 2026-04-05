package net.thenextlvl.worlds.view;

import net.thenextlvl.worlds.api.view.GeneratorView;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@NullMarked
public final class PluginGeneratorView implements GeneratorView {
    @Override
    public boolean hasGenerator(final Plugin plugin) {
        return hasChunkGenerator(plugin.getClass()) || hasBiomeProvider(plugin.getClass());
    }

    @Override
    public boolean hasChunkGenerator(final Class<? extends Plugin> clazz) {
        return overridesMethod(clazz, "getDefaultWorldGenerator", ChunkGenerator.class);
    }

    @Override
    public boolean hasBiomeProvider(final Class<? extends Plugin> clazz) {
        return overridesMethod(clazz, "getDefaultBiomeProvider", BiomeProvider.class);
    }

    private boolean overridesMethod(final Class<?> clazz, final String methodName, final Class<?> returnType) {
        try {
            final var lookup = MethodHandles.lookup();
            final var methodType = MethodType.methodType(returnType, String.class, String.class);
            final var virtual = lookup.findVirtual(clazz, methodName, methodType);
            return lookup.revealDirect(virtual).getDeclaringClass().equals(clazz);
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            return false;
        }
    }
}
