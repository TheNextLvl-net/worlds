package worlds.command.world;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import net.nonswag.core.api.command.CommandSource;
import net.nonswag.core.api.command.Invocation;
import net.nonswag.core.api.message.Placeholder;
import net.nonswag.tnl.listener.api.command.exceptions.SourceMismatchException;
import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.listener.api.plugin.PluginManager;
import worlds.Worlds;
import net.thenextlvl.worlds.api.WorldUtil;
import net.thenextlvl.worlds.api.world.Environment;
import net.thenextlvl.worlds.api.world.TNLWorld;
import net.thenextlvl.worlds.api.world.WorldType;
import worlds.command.CustomExceptionHandler;
import worlds.command.CustomSyntaxFormatter;
import net.thenextlvl.worlds.generator.CustomGenerator;
import worlds.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class WorldCommand {

    public static void register(Worlds plugin) throws Exception {
        var manager = new PaperCommandManager<>(plugin, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());
        CustomExceptionHandler.INSTANCE.apply(manager, sender -> sender);
        manager.commandSyntaxFormatter(new CustomSyntaxFormatter<>());
        manager.registerAsynchronousCompletions();
        manager.registerBrigadier();
        var builder = manager.commandBuilder("world").permission("worlds.command.world");
        // manager.command(WorldCreateCommand.create(builder));
    }

    @Override
    @SuppressWarnings("deprecation")
    // i am so sorry for this, will be fixed in the future
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
                                                if (creator != null) {
                                                    creator.type(type.getWorldType()).environment(environment.getEnvironment());
                                                } else generator = plugin.getDefaultWorldGenerator(args[1], null);
                                                if (generator != null || creator != null) {
                                                    source.sendMessage("%prefix% §aGenerating world §6" + args[1]);
                                                    TNLWorld world = null;
                                                    if (creator == null) {
                                                        World created = new WorldCreator(args[1]).generator(generator).environment(environment.getEnvironment()).type(type.getWorldType()).createWorld();
                                                        if (created != null) {
                                                            world = new TNLWorld(created, environment, type, plugin.getName(), false).register();
                                                        }
                                                    } else {
                                                        World created = creator.createWorld();
                                                        if (created != null) {
                                                            world = new TNLWorld(created, Environment.valueOf(created.getEnvironment()), WorldType.valueOf(created.getWorldType()), plugin.getName(), false).register();
                                                        }
                                                    }
                                                    if (world != null) {
                                                        source.sendMessage("%prefix% §aGenerated world §6" + world.bukkit().getName());
                                                        if (source instanceof TNLPlayer player) {
                                                            player.worldManager().teleport(world.bukkit().getSpawnLocation().add(0.5, 0, 0.5));
                                                        }
                                                    } else source.sendMessage("%prefix% §cFailed to generate world");
                                                } else {
                                                    source.sendMessage("%prefix% §4" + plugin.getName() + "§c is not a world generator");
                                                }
                                            } else {
                                                source.sendMessage("%prefix% §c/world create " + args[1] + " " + args[2] + " " + environment.getName() + " §8(§6Plugin§8)");
                                            }
                                        } else {
                                            TNLWorld world = TNLWorld.nullable(new WorldCreator(args[1]).type(type.getWorldType()).environment(environment.getEnvironment()).createWorld());
                                            if (world != null) {
                                                world.register();
                                                source.sendMessage("%prefix% §7Created World§8: §6" + args[1]);
                                                if (source instanceof TNLPlayer player) {
                                                    player.worldManager().teleport(world.bukkit().getSpawnLocation().add(0.5, 0, 0.5));
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
                    } else source.sendMessage("%prefix% §cA world named §4" + args[1] + "§c already exists");
                } else {
                    source.sendMessage("%prefix% §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                }
            } else if (args[0].equalsIgnoreCase("tp")) {
                if (args.length == 2) {
                    if (source instanceof TNLPlayer player) {
                        World world = Bukkit.getWorld(args[1]);
                        if (world != null) {
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
                    TNLWorld world = TNLWorld.cast(args[1]);
                    if (world != null) {
                        if (WorldUtil.deleteWorld(world)) {
                            source.sendMessage("%prefix% §7Deleted World§8: §6" + world.bukkit().getName());
                        } else source.sendMessage("%prefix% §cFailed to delete world §4" + world.bukkit().getName());
                    } else source.sendMessage("%prefix% §c/world delete §8[§6World§8]");
                } else source.sendMessage("%prefix% §c/world delete §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("import")) {
                if (args.length >= 2) {
                    TNLWorld world = TNLWorld.cast(args[1]);
                    if (world == null) {
                        File file = new File(Bukkit.getWorldContainer(), args[1]);
                        if (file.exists() && file.isDirectory()) {
                            world = WorldUtil.loadWorld(args[1]);
                            if (world == null) {
                                WorldCreator creator = new WorldCreator(args[1]);
                                creator.type(org.bukkit.WorldType.NORMAL);
                                creator.environment(World.Environment.NORMAL);
                                world = TNLWorld.nullable(creator.createWorld());
                            }
                            if (world != null) {
                                source.sendMessage("%prefix% §7Imported world§8: §6" + args[1]);
                                if (source instanceof TNLPlayer player) {
                                    player.worldManager().teleport(world.bukkit().getSpawnLocation().add(0.5, 0, 0.5));
                                }
                            } else source.sendMessage("%prefix% §cFailed to import world §4" + args[1]);
                        } else source.sendMessage("%prefix% §cCan't find the folder §4" + file.getAbsolutePath());
                    } else source.sendMessage("%prefix% §cA world with this name already exist");
                } else source.sendMessage("%prefix% §c/world import §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("unload")) {
                if (args.length >= 2) {
                    TNLWorld world = TNLWorld.cast(args[1]);
                    if (world != null) {
                        if (WorldUtil.unloadWorld(world, true)) {
                            source.sendMessage("%prefix% §7Unloaded world§8: §6" + world.bukkit().getName());
                        } else source.sendMessage("%prefix% §cFailed to unload world §4" + world.bukkit().getName());
                    } else source.sendMessage("%prefix% §c/world unload §8[§6World§8]");
                } else source.sendMessage("%prefix% §c/world unload §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("info")) {
                if (args.length >= 2) {
                    TNLWorld world = TNLWorld.cast(args[1]);
                    if (world != null) {
                        source.sendMessage("%prefix% §7World§8: §6" + world.bukkit().getName());
                        if (world.generator() != null && world.bukkit().getGenerator() != null) {
                            source.sendMessage("%prefix% §7Generator§8: §6" + world.generator());
                        } else if (world.bukkit().getGenerator() != null) {
                            source.sendMessage("%prefix% §7Generator§8: §cFailed to read §8(§4" + world.bukkit().getGenerator().getClass().getName() + "§8)");
                        } else if (world.generator() != null) {
                            source.sendMessage("%prefix% §7Generator§8: §cFailed to load §8(§4" + world.generator() + "§8)");
                        } else source.sendMessage("%prefix% §7Generator§8: §7-§8/§7-");
                        source.sendMessage("%prefix% §7Type§8: §6" + world.type().getName());
                        source.sendMessage("%prefix% §7Environment§8: §6" + world.environment().getName());
                    } else source.sendMessage("%prefix% §c/world info §8[§6World§8]");
                } else source.sendMessage("%prefix% §c/world info §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("export")) {
                if (args.length >= 2) {
                    TNLWorld world = TNLWorld.cast(args[1]);
                    if (world != null) {
                        world.bukkit().save();
                        WorldUtil.export(world);
                        source.sendMessage(Messages.WORLD_SAVED, new Placeholder("world", world.bukkit().getName()));
                    } else source.sendMessage("%prefix% §c/world export §8(§6World§8)");
                } else {
                    for (World world : Bukkit.getWorlds()) {
                        world.save();
                        source.sendMessage(Messages.WORLD_SAVED, new Placeholder("world", world.getName()));
                    }
                    WorldUtil.exportAll();
                }
            } else if (args[0].equalsIgnoreCase("load")) {
                if (args.length >= 2) {
                    TNLWorld world = TNLWorld.cast(args[1]);
                    if (world == null) {
                        world = WorldUtil.loadWorld(args[1]);
                        if (world != null) source.sendMessage("%prefix% §7Loaded World§8: §6" + args[1]);
                        else source.sendMessage("%prefix% §cFailed to load world §4" + args[1]);
                    } else source.sendMessage("%prefix% §cA world with this name already exist");
                } else source.sendMessage("%prefix% §c/world load §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("setspawn")) {
                if (source instanceof TNLPlayer player) {
                    player.worldManager().getWorld().setSpawnLocation(player.worldManager().getLocation().getBlock().getLocation());
                    Location l = player.worldManager().getWorld().getSpawnLocation();
                    source.sendMessage("%prefix% §aSuccessfully set the spawn location to §6" + l.getX() + "§8, §6" + l.getY() + "§8, §6" + l.getZ());
                } else throw new SourceMismatchException();
            } else if (args[0].equalsIgnoreCase("spawn")) {
                if (source instanceof TNLPlayer player) {
                    player.worldManager().teleport(player.worldManager().getWorld().getSpawnLocation().add(0.5, 0, 0.5));
                    source.sendMessage("%prefix% §aTeleported you to the spawn of world §6" + player.worldManager().getWorld().getName());
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
        source.sendMessage("%prefix% §c/world tp §8[§6World§8] §8(§6Player§8)");
        source.sendMessage("%prefix% §c/world delete §8[§6World§8]");
        source.sendMessage("%prefix% §c/world import §8[§6Name§8]");
        source.sendMessage("%prefix% §c/world unload §8[§6World§8]");
        source.sendMessage("%prefix% §c/world export §8(§6World§8)");
        source.sendMessage("%prefix% §c/world load §8[§6Name§8]");
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
            suggestions.add("info");
            suggestions.add("help");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("export")
                    || args[0].equalsIgnoreCase("delete")
                    || args[0].equalsIgnoreCase("unload")
                    || args[0].equalsIgnoreCase("info")) {
                for (World world : Bukkit.getWorlds()) suggestions.add(world.getName());
            } else if (args[0].equalsIgnoreCase("import") || args[0].equalsIgnoreCase("create")) {
                File[] files = Bukkit.getWorldContainer().listFiles((file, name) -> file.isDirectory() && !file.getName().contains(" "));
                if (files != null) for (File file : files) {
                    if (Bukkit.getWorld(file.getName()) == null) suggestions.add(file.getName());
                }
            } else if (args[0].equalsIgnoreCase("tp")) {
                for (World world : Bukkit.getWorlds()) suggestions.add(world.getName());
                for (Player all : Bukkit.getOnlinePlayers()) suggestions.add(all.getName());
            } else if (args[0].equalsIgnoreCase("load")) {
                for (String world : WorldUtil.getWorlds()) {
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