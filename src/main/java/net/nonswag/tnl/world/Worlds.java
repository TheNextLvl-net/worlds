package net.nonswag.tnl.world;

import net.nonswag.core.api.annotation.FieldsAreNullableByDefault;
import net.nonswag.core.api.annotation.MethodsReturnNonnullByDefault;
import net.nonswag.core.api.file.helper.FileHelper;
import net.nonswag.core.utils.LinuxUtil;
import net.nonswag.tnl.listener.api.packets.outgoing.RespawnPacket;
import net.nonswag.tnl.listener.api.plugin.TNLPlugin;
import net.nonswag.tnl.world.api.WorldUtil;
import net.nonswag.tnl.world.api.errors.WorldCloneException;
import net.nonswag.tnl.world.api.world.TNLWorld;
import net.nonswag.tnl.world.commands.WorldCommand;
import net.nonswag.tnl.world.generators.SimplexOctaveGenerator;
import net.nonswag.tnl.world.generators.SuperFlatGenerator;
import net.nonswag.tnl.world.generators.VoidGenerator;
import net.nonswag.tnl.world.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;

@FieldsAreNullableByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Worlds extends TNLPlugin {
    private static Worlds instance;

    @Override
    public void enable() {
        instance = this;
        VoidGenerator.getInstance().register();
        SuperFlatGenerator.getInstance().register();
        SimplexOctaveGenerator.getInstance().register();
        getCommandManager().registerCommand(new WorldCommand());
        // registerPacketWriters();
        WorldUtil.loadWorlds();
        WorldUtil.exportAll();
        Messages.init();
    }

    @Override
    public void disable() {
        WorldUtil.exportAll();
    }

    private void registerPacketWriters() {
        getEventManager().registerPacketWriter(RespawnPacket.class, (player, packet, cancelled) -> {
            TNLWorld world = TNLWorld.cast(player.worldManager().getWorld());
            // Reflection.Field.set(Objects.requireNonNull(event.getPacketField("a")), "ambientLight", world.fullBright() ? 1 : 0);
            // event.setPacketField("g", world.type().equals(WorldType.FLAT));
        });
    }

    @Nonnull
    public World clone(World world, String name) throws WorldCloneException {
        if (Bukkit.getWorld(name) != null) {
            throw new WorldCloneException("A world named <'" + name + "'> does already exist");
        }
        try {
            File file = new File(Bukkit.getWorldContainer(), name);
            LinuxUtil.runShellCommand("cp -r " + new File(Bukkit.getWorldContainer(), world.getName()).getAbsolutePath() + " " + file.getAbsolutePath());
            FileHelper.delete(new File(file, "uid.dat"));
            FileHelper.delete(new File(file, "session.lock"));
        } catch (IOException | InterruptedException e) {
            throw new WorldCloneException("an error has occurred while copying the world", e);
        }
        TNLWorld original = TNLWorld.cast(world);
        World clone = new WorldCreator(name).copy(world).createWorld();
        if (clone == null) throw new WorldCloneException();
        return new TNLWorld(clone, original.environment(), original.type(), original.generator(), original.fullBright()).register().bukkit();
    }

    public boolean isCorrectLoaded(@Nullable World world) {
        if (world == null) return false;
        if (world.getLoadedChunks().length == 0) return false;
        return new File(Bukkit.getWorldContainer(), world.getName()).exists();
    }

    public static Worlds getInstance() {
        assert instance != null;
        return instance;
    }
}
