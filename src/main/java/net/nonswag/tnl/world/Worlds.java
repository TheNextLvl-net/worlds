package net.nonswag.tnl.world;

import net.nonswag.tnl.core.api.file.helper.FileHelper;
import net.nonswag.tnl.core.utils.LinuxUtil;
import net.nonswag.tnl.listener.api.plugin.PluginUpdate;
import net.nonswag.tnl.listener.api.plugin.TNLPlugin;
import net.nonswag.tnl.listener.api.settings.Settings;
import net.nonswag.tnl.world.api.WorldUtil;
import net.nonswag.tnl.world.api.errors.WorldCloneException;
import net.nonswag.tnl.world.commands.WorldCommand;
import net.nonswag.tnl.world.listeners.WorldListener;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class Worlds extends TNLPlugin {

    @Nullable
    private static Worlds instance = null;

    @Override
    protected void enable() {
        setInstance(this);
        WorldUtil.getInstance().exportAll();
        getCommandManager().registerCommand(new WorldCommand());
        getEventManager().registerListener(new WorldListener());
        try {
            WorldUtil.getInstance().loadWorlds();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Settings.AUTO_UPDATER.getValue()) new PluginUpdate(this).downloadUpdate();
    }

    @Override
    protected void disable() {
        WorldUtil.getInstance().saveWorlds();
    }

    @Nonnull
    public World clone(@Nonnull World world, @Nonnull String name) throws WorldCloneException {
        try {
            File file = new File(name);
            LinuxUtil.runShellCommand("cp -r " + new File(world.getName()).getAbsolutePath() + " " + file.getAbsolutePath(), null);
            FileHelper.deleteDirectory(new File(file, "uid.dat"));
            FileHelper.deleteDirectory(new File(file, "session.lock"));
        } catch (IOException | InterruptedException e) {
            throw new WorldCloneException("an error has occurred while copying the world", e);
        }
        World clone = new WorldCreator(name).copy(world).createWorld();
        if (clone != null) return clone;
        throw new WorldCloneException();
    }

    @Nonnull
    public static Worlds getInstance() {
        assert instance != null;
        return instance;
    }

    private static void setInstance(@Nonnull Worlds instance) {
        Worlds.instance = instance;
    }
}
