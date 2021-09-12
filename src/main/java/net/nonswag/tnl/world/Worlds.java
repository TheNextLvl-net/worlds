package net.nonswag.tnl.world;

import net.nonswag.tnl.listener.api.command.CommandManager;
import net.nonswag.tnl.listener.api.event.EventManager;
import net.nonswag.tnl.listener.api.plugin.PluginUpdate;
import net.nonswag.tnl.listener.api.plugin.TNLPlugin;
import net.nonswag.tnl.listener.api.settings.Settings;
import net.nonswag.tnl.world.api.WorldUtil;
import net.nonswag.tnl.world.api.generator.BuildWorldGenerator;
import net.nonswag.tnl.world.commands.WorldCommand;
import net.nonswag.tnl.world.listeners.WorldListener;
import org.bukkit.generator.ChunkGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Worlds extends TNLPlugin {

    @Nullable
    private static Worlds instance = null;

    @Override
    public void onEnable() {
        setInstance(this);
        WorldUtil.getInstance().exportAll();
        CommandManager.registerCommands(new WorldCommand());
        EventManager eventManager = EventManager.cast(this);
        eventManager.registerListener(new WorldListener());
        try {
            WorldUtil.getInstance().loadWorlds();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Settings.AUTO_UPDATER.getValue()) new PluginUpdate(this).downloadUpdate();
    }

    @Override
    public void onDisable() {
        WorldUtil.getInstance().saveWorlds();
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@Nonnull String worldName, @Nullable String id) {
        return new BuildWorldGenerator();
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
