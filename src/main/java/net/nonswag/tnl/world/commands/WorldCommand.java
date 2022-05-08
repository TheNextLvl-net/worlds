package net.nonswag.tnl.world.commands;

import net.nonswag.tnl.core.api.command.CommandSource;
import net.nonswag.tnl.core.api.command.Invocation;
import net.nonswag.tnl.core.api.message.Placeholder;
import net.nonswag.tnl.core.api.message.key.MessageKey;
import net.nonswag.tnl.listener.api.command.TNLCommand;
import net.nonswag.tnl.listener.api.command.exceptions.SourceMismatchException;
import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.listener.api.plugin.PluginManager;
import net.nonswag.tnl.world.api.Environment;
import net.nonswag.tnl.world.api.WorldType;
import net.nonswag.tnl.world.api.WorldUtil;
import net.nonswag.tnl.world.generators.CustomGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldCommand extends TNLCommand {

    public WorldCommand() {
        super("world", "tnl.world");
    }

    @Override
    protected void execute(@Nonnull Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length >= 2 && !args[1].isEmpty()) {
                    if (Bukkit.getWorld(args[1]) == null) {
                        if (args.length >= 3) {
                            WorldType type = WorldType.getByName(args[2]);
                            if (type != null) {
                                if (args.length >= 4) {
                                    Environment environment = Environment.getByName(args[3]);
                                    if (environment != null) {
                                        if (args.length >= 5) {
                                            List<Plugin> plugins = new ArrayList<>();
                                            plugins.addAll(Arrays.asList(PluginManager.getPlugins()));
                                            plugins.addAll(CustomGenerator.getAdditionalGenerators());
                                            Plugin plugin = null;
                                            for (Plugin pl : plugins) {
                                                if (!pl.getName().equalsIgnoreCase(args[4])) continue;
                                                plugin = pl;
                                                break;
                                            }
                                            if (plugin != null && plugin.isEnabled()) {
                                                WorldCreator creator = null;
                                                if (plugin instanceof CustomGenerator generator) {
                                                    creator = generator.getWorldCreator(args[1]);
                                                }
                                                ChunkGenerator generator = null;
                                                if (creator == null) {
                                                    generator = plugin.getDefaultWorldGenerator(args[1], null);
                                                }
                                                if (generator != null || creator != null) {
                                                    source.sendMessage("%prefix% §aGenerating world §6" + args[1]);
                                                    World world;
                                                    if (generator != null) {
                                                        world = new WorldCreator(args[1]).generator(generator).environment(environment.getEnvironment()).type(type.getWorldType()).createWorld();
                                                    } else world = creator.createWorld();
                                                    if (world != null) {
                                                        source.sendMessage("%prefix% §aGenerated world §6" + world.getName());
                                                        WorldUtil.getWorldTypes().put(world, type);
                                                        if (source.isPlayer()) {
                                                            TNLPlayer player = (TNLPlayer) source.player();
                                                            player.worldManager().teleport(world.getSpawnLocation().add(0.5, 0, 0.5));
                                                        }
                                                    } else source.sendMessage("%prefix% §cFailed to generate world");
                                                } else {
                                                    source.sendMessage("%prefix% §4" + plugin.getName() + "§c is not a world generator");
                                                }
                                            } else {
                                                source.sendMessage("%prefix% §c/world create " + args[1] + " " + args[2] + " " + environment.getName() + " §8(§6Plugin§8)");
                                            }
                                        } else {
                                            World world = new WorldCreator(args[1]).type(type.getWorldType()).environment(environment.getEnvironment()).createWorld();
                                            if (world != null) {
                                                source.sendMessage("%prefix% §7Created World§8: §6" + args[1]);
                                                WorldUtil.getWorldTypes().put(world, type);
                                                if (source.isPlayer()) {
                                                    TNLPlayer player = (TNLPlayer) source.player();
                                                    player.worldManager().teleport(world.getSpawnLocation().add(0.5, 0, 0.5));
                                                }
                                            } else source.sendMessage("%prefix% §cFailed to create World §4" + args[1]);
                                        }
                                    } else {
                                        source.sendMessage("%prefix% §c/world create " + args[1] + " " + args[2] + " §8[§6Environment§8] §8(§6Plugin§8)");
                                    }
                                } else {
                                    source.sendMessage("%prefix% §c/world create " + args[1] + " " + args[2] + " §8[§6Environment§8] §8(§6Plugin§8)");
                                }
                            } else {
                                source.sendMessage("%prefix% §c/world create " + args[1] + " §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                            }
                        } else {
                            source.sendMessage("%prefix% §c/world create " + args[1] + " §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                        }
                    } else source.sendMessage("%prefix% §cA world named §4" + args[1] + "§c already exist");
                } else {
                    source.sendMessage("%prefix% §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                }
            } else if (args[0].equalsIgnoreCase("tp")) {
                if (args.length == 2) {
                    if (source.isPlayer()) {
                        World world = Bukkit.getWorld(args[1]);
                        if (world != null) {
                            TNLPlayer player = (TNLPlayer) source.player();
                            player.worldManager().teleport(world.getSpawnLocation().add(0.5, 0, 0.5));
                            source.sendMessage("%prefix% §7Teleported§8: §6" + world.getName());
                        } else source.sendMessage("%prefix% §c/world tp §8[§6World§8]");
                    } else source.sendMessage("%prefix% §c/world tp §8[§6Player§8] §8[§6World§8]");
                } else if (args.length >= 3) {
                    TNLPlayer arg = TNLPlayer.cast(args[1]);
                    if (arg != null) {
                        World world = Bukkit.getWorld(args[2]);
                        if (world != null) {
                            arg.worldManager().teleport(world.getSpawnLocation().add(0.5, 0, 0.5));
                            if (source.equals(arg)) {
                                source.sendMessage("%prefix% §aTeleported§8: §6" + world.getName());
                            } else {
                                source.sendMessage("%prefix% §7Teleported §8(§a" + arg.getName() + "§8): §6" + world.getName());
                            }
                        } else {
                            source.sendMessage("%prefix% §c/world tp " + arg.getName() + " §8[§6World§8]");
                        }
                    } else {
                        source.sendMessage("%prefix% §c/world tp §8[§6Player§8] §8[§6World§8]");
                    }
                } else {
                    source.sendMessage("%prefix% §c/world tp §8[§6Player§8] §8[§6World§8]");
                    source.sendMessage("%prefix% §c/world tp §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        if (WorldUtil.getInstance().deleteWorld(world)) {
                            source.sendMessage("%prefix% §7Deleted World§8: §6" + world.getName());
                        } else {
                            source.sendMessage("%prefix% §cFailed to delete world §4" + world.getName());
                        }
                    } else source.sendMessage("%prefix% §c/world delete §8[§6World§8]");
                } else source.sendMessage("%prefix% §c/world delete §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("import")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world == null) {
                        File file = new File(Bukkit.getWorldContainer(), args[1]);
                        if (file.exists() && file.isDirectory()) {
                            world = WorldUtil.getInstance().loadWorld(args[1]);
                            if (world == null) {
                                WorldCreator creator = new WorldCreator(args[1]);
                                creator.type(org.bukkit.WorldType.NORMAL);
                                creator.environment(World.Environment.NORMAL);
                                world = creator.createWorld();
                            }
                            if (world != null) {
                                source.sendMessage("%prefix% §7Imported world§8: §6" + args[1]);
                                if (source.isPlayer()) {
                                    ((TNLPlayer) source.player()).worldManager().teleport(world.getSpawnLocation().add(0.5, 0, 0.5));
                                }
                            } else source.sendMessage("%prefix% §cFailed to import world §4" + args[1]);
                        } else source.sendMessage("%prefix% §cCan't find the folder §4" + file.getAbsolutePath());
                    } else source.sendMessage("%prefix% §cA world with this name already exist");
                } else source.sendMessage("%prefix% §c/world import §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("unload")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        if (WorldUtil.getInstance().unloadWorld(world, true)) {
                            source.sendMessage("%prefix% §7Unloaded world§8: §6" + world.getName());
                        } else {
                            source.sendMessage("%prefix% §cFailed to unload world §4" + world.getName());
                        }
                    } else {
                        source.sendMessage("%prefix% §c/world unload §8[§6World§8]");
                    }
                } else {
                    source.sendMessage("%prefix% §c/world unload §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("export")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        world.save();
                        WorldUtil.getInstance().export(world);
                        source.sendMessage(MessageKey.WORLD_SAVED, new Placeholder("world", world.getName()));
                    } else source.sendMessage("%prefix% §c/world export §8(§6World§8)");
                } else {
                    for (World world : Bukkit.getWorlds()) {
                        world.save();
                        source.sendMessage(MessageKey.WORLD_SAVED, new Placeholder("world", world.getName()));
                    }
                    WorldUtil.getInstance().exportAll();
                }
            } else if (args[0].equalsIgnoreCase("load")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world == null) {
                        world = WorldUtil.getInstance().loadWorld(args[1]);
                        if (world != null) source.sendMessage("%prefix% §7Loaded World§8: §6" + args[1]);
                        else source.sendMessage("%prefix% §cFailed to load world §4" + args[1]);
                    } else source.sendMessage("%prefix% §cA world with this name already exist");
                } else source.sendMessage("%prefix% §c/world load §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("setspawn")) {
                if (source.isPlayer()) {
                    TNLPlayer player = (TNLPlayer) source.player();
                    player.worldManager().getWorld().setSpawnLocation(player.worldManager().getLocation().getBlock().getLocation());
                    Location l = player.worldManager().getWorld().getSpawnLocation();
                    source.sendMessage("%prefix% §aSuccessfully set the spawn location to §6" + l.getX() + "§8, §6" + l.getY() + "§8, §6" + l.getZ());
                } else throw new SourceMismatchException();
            } else if (args[0].equalsIgnoreCase("spawn")) {
                if (source.isPlayer()) {
                    TNLPlayer player = (TNLPlayer) source.player();
                    player.worldManager().teleport(player.worldManager().getWorld().getSpawnLocation().add(0.5, 0, 0.5));
                    source.sendMessage("%prefix% §aTeleported you to the world spawn of world §6" + player.worldManager().getWorld().getName());
                } else throw new SourceMismatchException();
            } else if (args[0].equalsIgnoreCase("list")) {
                List<String> worlds = new ArrayList<>();
                Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));
                source.sendMessage("%prefix% §7Loaded Worlds §8(§6" + worlds.size() + "§8): §a" + String.join("§8, §a", worlds));
            } else help(source);
        } else help(source);
    }

    private void help(@Nonnull CommandSource source) {
        source.sendMessage("%prefix% §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
        source.sendMessage("%prefix% §c/world tp §8[§6Player§8] §8[§6World§8]");
        source.sendMessage("%prefix% §c/world delete §8[§6World§8]");
        source.sendMessage("%prefix% §c/world import §8[§6Name§8]");
        source.sendMessage("%prefix% §c/world unload §8[§6World§8]");
        source.sendMessage("%prefix% §c/world export §8(§6World§8)");
        source.sendMessage("%prefix% §c/world load §8[§6Name§8]");
        source.sendMessage("%prefix% §c/world tp §8[§6World§8]");
        source.sendMessage("%prefix% §c/world setspawn");
        source.sendMessage("%prefix% §c/world spawn");
        source.sendMessage("%prefix% §c/world list");
    }

    @Nonnull
    @Override
    protected List<String> suggest(@Nonnull Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();
        if (args.length <= 1) {
            suggestions.add("create");
            suggestions.add("tp");
            suggestions.add("delete");
            suggestions.add("import");
            suggestions.add("unload");
            suggestions.add("export");
            suggestions.add("load");
            suggestions.add("setspawn");
            suggestions.add("spawn");
            suggestions.add("list");
            suggestions.add("help");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("export")
                    || args[0].equalsIgnoreCase("delete")
                    || args[0].equalsIgnoreCase("unload")) {
                for (World world : Bukkit.getWorlds()) suggestions.add(world.getName());
            } else if (args[0].equalsIgnoreCase("tp")) {
                for (World world : Bukkit.getWorlds()) suggestions.add(world.getName());
                for (Player all : Bukkit.getOnlinePlayers()) suggestions.add(all.getName());
            } else if (args[0].equalsIgnoreCase("load")) {
                for (String world : WorldUtil.getInstance().getWorlds()) {
                    if (Bukkit.getWorld(world) == null) suggestions.add(world);
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("tp")) {
                if (TNLPlayer.cast(args[1]) != null) {
                    for (World world : Bukkit.getWorlds()) suggestions.add(world.getName());
                }
            } else if (args[0].equalsIgnoreCase("create")) {
                for (WorldType worldType : WorldType.values()) suggestions.add(worldType.getName());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("create")) {
                for (Environment environment : Environment.values()) suggestions.add(environment.getName());
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("create")) {
                List<Plugin> plugins = new ArrayList<>();
                plugins.addAll(Arrays.asList(PluginManager.getPlugins()));
                plugins.addAll(CustomGenerator.getAdditionalGenerators());
                for (Plugin plugin : plugins) {
                    if (plugin.getDefaultWorldGenerator(args[1], null) != null) suggestions.add(plugin.getName());
                }
            }
        }
        return suggestions;
    }
}
