package net.nonswag.tnl.world.commands;

import net.nonswag.tnl.listener.TNLListener;
import net.nonswag.tnl.world.Worlds;
import net.nonswag.tnl.world.api.Environment;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldCommand implements CommandExecutor {

    @Nonnull private static final List<String> environments = new ArrayList<>();
    @Nonnull private static final List<String> worldTypes = new ArrayList<>();

    static {
        for (World.Environment environment : World.Environment.values()) {
            getEnvironments().add(environment.name());
        }
        for (WorldType type : WorldType.values()) {
            getWorldTypes().add(type.getName());
        }
    }

    @Nonnull
    public static List<String> getEnvironments() {
        return environments;
    }

    @Nonnull
    public static List<String> getWorldTypes() {
        return worldTypes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length >= 2) {
                    if (Bukkit.getWorld(args[1]) == null) {
                        if (args.length >= 3) {
                            WorldType type = WorldType.getByName(args[2].toUpperCase());
                            if (type != null) {
                                if (args.length >= 4) {
                                    Environment environment = Environment.getByName(args[3]);
                                    if (environment != null) {
                                        if (args.length >= 5) {
                                            Plugin plugin = Bukkit.getPluginManager().getPlugin(args[4]);
                                            if (plugin != null && plugin.isEnabled()) {
                                                ChunkGenerator generator = plugin.getDefaultWorldGenerator(args[1], null);
                                                if (generator != null) {
                                                    new WorldCreator(args[1]).generator(generator).createWorld();
                                                } else {
                                                    sender.sendMessage(TNLListener.getInstance().getPrefix() + "§4" + plugin.getName() + "§c has no default generator");
                                                }
                                            } else {
                                                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world create " + args[1] + " " + args[2] + " " + environment.name() + " §8(§6Plugin§8)");
                                            }
                                        } else {
                                            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aStarting generation of new world §6" + args[1]);
                                            new WorldCreator(args[1]).type(type).environment(environment.getEnvironment()).createWorld();
                                            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aSuccessfully created world §6" + args[1]);
                                        }
                                    } else {
                                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world create " + args[1] + " " + args[2] + " §8[§6Environment§8] §8(§6Plugin§8)");
                                    }
                                } else {
                                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world create " + args[1] + " " + args[2] + " §8[§6Environment§8] §8(§6Plugin§8)");
                                }
                            } else {
                                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world create " + args[1] + " §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                            }
                        } else {
                            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world create " + args[1] + " §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                        }
                    } else {
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §cA world named §4" + args[1] + " §calready exist");
                    }
                } else {
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                }
            } else if (args[0].equalsIgnoreCase("tp")) {
                if (args.length == 2) {
                    if (sender instanceof Player) {
                        Player player = ((Player) sender);
                        World world = Bukkit.getWorld(args[1]);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aTeleported you to the world spawn of world §6" + world.getName());
                        } else {
                            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6World§8]");
                        }
                    } else {
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    }
                } else if (args.length >= 3) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player != null) {
                        World world = Bukkit.getWorld(args[2]);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            if (sender.equals(player)) {
                                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aTeleported you to the world spawn of world §6" + world.getName());
                            } else {
                                player.sendMessage(TNLListener.getInstance().getPrefix() + " §6" + sender.getName() + " §ateleported you to the world spawn of world §6" + world.getName());
                                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aTeleported §6" + player.getName() + "§a to the world spawn of world §6" + world.getName());
                            }
                        } else {
                            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp " + player.getName() + " §8[§6World§8]");
                        }
                    } else {
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        String name = world.getName();
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aTry to unload world §6" + name);
                        Bukkit.unloadWorld(world, true);
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aSuccessfully unloaded world §6" + name);
                        File file = new File(Worlds.getPlugin().getDataFolder().getAbsoluteFile().getParentFile().getParent() + "/" + name);
                        if (file.exists() && file.isDirectory()) {
                            TNLListener.getInstance().runShellCommand("rm -r " + file.getAbsolutePath());
                            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aSuccessfully deleted world folder §6" + name);
                        } else {
                            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §cStrange things happens while deleting world folder §4" + name);
                        }
                    } else {
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world delete §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world delete §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("import")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world == null) {
                    } else {
                    }
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §cSoon");
                } else {
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world import §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("unload")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        String name = world.getName();
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aTry to unload world §6" + name);
                        Bukkit.unloadWorld(world, true);
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aSuccessfully unloaded world §6" + name);
                    } else {
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world unload §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world unload §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("save")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aTry to save world §6" + world.getName());
                        world.save();
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aSuccessfully saved world §6" + world.getName());
                    } else {
                        sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world save §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world save §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("load")) {
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §cSoon");
            } else if (args[0].equalsIgnoreCase("setspawn")) {
                if (sender instanceof Player) {
                    Player player = ((Player) sender);
                    player.getWorld().setSpawnLocation(player.getLocation());
                    Location l = player.getWorld().getSpawnLocation();
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aSuccessfully set the spawn location to §6" + l.getX() + "§8, §6" + l.getY() + "§8, §6" + l.getZ());
                } else {
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §cThis is a player command");
                }
            } else if (args[0].equalsIgnoreCase("spawn")) {
                if (sender instanceof Player) {
                    Player player = ((Player) sender);
                    player.teleport(player.getWorld().getSpawnLocation());
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §aTeleported you to the world spawn of world §6" + player.getWorld().getName());
                } else {
                    sender.sendMessage(TNLListener.getInstance().getPrefix() + " §cThis is a player command");
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                List<String> worlds = new ArrayList<>();
                Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §7Loaded Worlds §8(§6" + worlds.size() + "§8): §a" + String.join("§8, §a", worlds));
            } else {
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world delete §8[§6World§8]");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world import §8[§6Name§8]");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world unload §8[§6World§8]");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world save §8[§6World§8]");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world load §8[§6Name§8]");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6World§8]");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world setspawn");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world spawn");
                sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world list");
            }
        } else {
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world delete §8[§6World§8]");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world import §8[§6Name§8]");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world unload §8[§6World§8]");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world save §8[§6World§8]");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world load §8[§6Name§8]");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world tp §8[§6World§8]");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world setspawn");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world spawn");
            sender.sendMessage(TNLListener.getInstance().getPrefix() + " §c/world list");
        }
        return false;
    }
}
