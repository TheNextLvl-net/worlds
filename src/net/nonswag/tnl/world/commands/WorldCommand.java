package net.nonswag.tnl.world.commands;

import net.nonswag.tnl.listener.NMSMain;
import net.nonswag.tnl.world.Worlds;
import net.nonswag.tnl.world.api.WorldGenerator;
import net.nonswag.tnl.world.api.enerators.VoidGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldCommand implements CommandExecutor {

    private static final List<String> environments = new ArrayList<>();
    private static final List<String> types = new ArrayList<>();

    static {
        for (World.Environment environment : World.Environment.values()) {
            environments.add(environment.name());
        }
        for (WorldType type : WorldType.values()) {
            types.add(type.name());
        }
        types.add("VOID");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length >= 2) {
                    String name = args[1];
                    if (Bukkit.getWorld(name) == null) {
                        if (args.length >= 3) {
                            try {
                                if (args[2].equalsIgnoreCase("void")) {
                                    if (args.length >= 4) {
                                        try {
                                            World.Environment environment = World.Environment.valueOf(args[3].toUpperCase());
                                            sender.sendMessage(NMSMain.getPrefix() + " §aStarting generation of new world §6" + name);
                                            try {
                                                new WorldGenerator(name).getWorldCreator().environment(environment).generateStructures(false).generator(new VoidGenerator()).createWorld();
                                                sender.sendMessage(NMSMain.getPrefix() + " §aSuccessfully created world §6" + name);
                                            } catch (Throwable t) {
                                                sender.sendMessage(NMSMain.getPrefix() + " §cAn error has occurred while creating world §4" + name);
                                            }
                                        } catch (Throwable t) {
                                            sender.sendMessage(NMSMain.getPrefix() + " §cThe Environment §8(§4" + args[3].toUpperCase() + "§8) §cdoesn't exist §8(§4/world help§8)");
                                        }
                                    } else {
                                        sender.sendMessage(NMSMain.getPrefix() + " §c/world create " + args[1] + " " + args[2].toUpperCase() + " §8[§6Environment§8]");
                                    }
                                } else {
                                    WorldType type = WorldType.valueOf(args[2].toUpperCase());
                                    if (args.length >= 4) {
                                        try {
                                            World.Environment environment = World.Environment.valueOf(args[3].toUpperCase());
                                            sender.sendMessage(NMSMain.getPrefix() + " §aStarting generation of new world §6" + name);
                                            try {
                                                new WorldGenerator(name).getWorldCreator().type(type).environment(environment).createWorld();
                                                sender.sendMessage(NMSMain.getPrefix() + " §aSuccessfully created world §6" + name);
                                            } catch (Throwable t) {
                                                sender.sendMessage(NMSMain.getPrefix() + " §cAn error has occurred while creating world §4" + name);
                                            }
                                        } catch (Throwable t) {
                                            sender.sendMessage(NMSMain.getPrefix() + " §cThe Environment §8(§4" + args[3].toUpperCase() + "§8) §cdoesn't exist §8(§4/world help§8)");
                                        }
                                    } else {
                                        sender.sendMessage(NMSMain.getPrefix() + " §c/world create " + args[1] + " " + args[2].toUpperCase() + " §8[§6Environment§8]");
                                    }
                                }
                            } catch (Throwable t) {
                                sender.sendMessage(NMSMain.getPrefix() + " §cThe World Type §8(§4" + args[2].toUpperCase() + "§8) §cdoesn't exist §8(§4/world help§8)");
                            }
                        } else {
                            sender.sendMessage(NMSMain.getPrefix() + " §c/world create " + args[1] + " §8[§6Type§8] §8[§6Environment§8]");
                        }
                    } else {
                        sender.sendMessage(NMSMain.getPrefix() + " §cA world named §4" + Bukkit.getWorld(name).getName() + " §calready exist");
                    }
                } else {
                    sender.sendMessage(NMSMain.getPrefix() + " §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8]");
                }
            } else if (args[0].equalsIgnoreCase("tp")) {
                if (args.length == 2) {
                    if (sender instanceof Player) {
                        Player player = ((Player) sender);
                        World world = Bukkit.getWorld(args[1]);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            sender.sendMessage(NMSMain.getPrefix() + " §aTeleported you to the world spawn of world §6" + world.getName());
                        } else {
                            sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6World§8]");
                        }
                    } else {
                        sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    }
                } else if (args.length >= 3) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player != null) {
                        World world = Bukkit.getWorld(args[2]);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            if (sender.equals(player)) {
                                sender.sendMessage(NMSMain.getPrefix() + " §aTeleported you to the world spawn of world §6" + world.getName());
                            } else {
                                player.sendMessage(NMSMain.getPrefix() + " §6" + sender.getName() + " §ateleported you to the world spawn of world §6" + world.getName());
                                sender.sendMessage(NMSMain.getPrefix() + " §aTeleported §6" + player.getName() + "§a to the world spawn of world §6" + world.getName());
                            }
                        } else {
                            sender.sendMessage(NMSMain.getPrefix() + " §c/world tp " + player.getName() + " §8[§6World§8]");
                        }
                    } else {
                        sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        String name = world.getName();
                        sender.sendMessage(NMSMain.getPrefix() + " §aTry to unload world §6" + name);
                        try {
                            Bukkit.unloadWorld(world, true);
                            sender.sendMessage(NMSMain.getPrefix() + " §aSuccessfully unloaded world §6" + name);
                            try {
                                File file = new File(Worlds.getPlugin().getDataFolder().getAbsoluteFile().getParentFile().getParent() + "/" + name);
                                if (file.exists() && file.isDirectory()) {
                                    NMSMain.runShellCommand("rm -r " + file.getAbsolutePath());
                                    sender.sendMessage(NMSMain.getPrefix() + " §aSuccessfully deleted world folder §6" + name);
                                } else {
                                    sender.sendMessage(NMSMain.getPrefix() + " §cStrange things happens while deleting world folder §4" + name);
                                }
                            } catch (Throwable t) {
                                sender.sendMessage(NMSMain.getPrefix() + " §cAn error has occourred while deleting world folder §4" + name);
                            }
                        } catch (Throwable t) {
                            sender.sendMessage(NMSMain.getPrefix() + " §cAn error has occourred while unloading world §4" + name);
                        }
                    } else {
                        sender.sendMessage(NMSMain.getPrefix() + " §c/world delete §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(NMSMain.getPrefix() + " §c/world delete §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("import")) {
                sender.sendMessage(NMSMain.getPrefix() + " §cSoon");
            } else if (args[0].equalsIgnoreCase("unload")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        String name = world.getName();
                        sender.sendMessage(NMSMain.getPrefix() + " §aTry to unload world §6" + name);
                        try {
                            Bukkit.unloadWorld(world, true);
                            sender.sendMessage(NMSMain.getPrefix() + " §aSuccessfully unloaded world §6" + name);
                        } catch (Throwable t) {
                            sender.sendMessage(NMSMain.getPrefix() + " §cAn error has occourred while unloading world §4" + name);
                        }
                    } else {
                        sender.sendMessage(NMSMain.getPrefix() + " §c/world unload §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(NMSMain.getPrefix() + " §c/world unload §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("save")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        sender.sendMessage(NMSMain.getPrefix() + " §aTry to save world §6" + world.getName());
                        try {
                            world.save();
                            sender.sendMessage(NMSMain.getPrefix() + " §aSuccessfully saved world §6" + world.getName());
                        } catch (Throwable t) {
                            sender.sendMessage(NMSMain.getPrefix() + " §cAn error has occourred while saving world " + world.getName());
                        }
                    } else {
                        sender.sendMessage(NMSMain.getPrefix() + " §c/world save §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(NMSMain.getPrefix() + " §c/world save §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("load")) {
                sender.sendMessage(NMSMain.getPrefix() + " §cSoon");
            } else if (args[0].equalsIgnoreCase("setspawn")) {
                if (sender instanceof Player) {
                    Player player = ((Player) sender);
                    player.getWorld().setSpawnLocation(player.getLocation());
                    Location l = player.getWorld().getSpawnLocation();
                    sender.sendMessage(NMSMain.getPrefix() + " §aSuccessfully set the spawn location to §6" + l.getX() + "§8, §6" + l.getY() + "§8, §6" + l.getZ());
                } else {
                    sender.sendMessage(NMSMain.getPrefix() + " §cThis is a player command");
                }
            } else if (args[0].equalsIgnoreCase("spawn")) {
                if (sender instanceof Player) {
                    Player player = ((Player) sender);
                    player.teleport(player.getWorld().getSpawnLocation());
                    sender.sendMessage(NMSMain.getPrefix() + " §aTeleported you to the world spawn of world §6" + player.getWorld().getName());
                } else {
                    sender.sendMessage(NMSMain.getPrefix() + " §cThis is a player command");
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(NMSMain.getPrefix() + " §7Environments §8(§6" + environments.size() + "§8): §a" + String.join("§8, §a", environments));
                sender.sendMessage(NMSMain.getPrefix() + " §7Types §8(§6" + types.size() + "§8): §a" + String.join("§8, §a", types));
            } else if (args[0].equalsIgnoreCase("list")) {
                List<String> worlds = new ArrayList<>();
                Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));
                sender.sendMessage(NMSMain.getPrefix() + " §7Loaded Worlds §8(§6" + worlds.size() + "§8): §a" + String.join("§8, §a", worlds));
            } else {
                sender.sendMessage(NMSMain.getPrefix() + " §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8]");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world delete §8[§6World§8]");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world import §8[§6Name§8]");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world unload §8[§6World§8]");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world save §8[§6World§8]");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world load §8[§6Name§8]");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6World§8]");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world setspawn");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world spawn");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world list");
                sender.sendMessage(NMSMain.getPrefix() + " §c/world help");
            }
        } else {
            sender.sendMessage(NMSMain.getPrefix() + " §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8]");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world delete §8[§6World§8]");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world import §8[§6Name§8]");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world unload §8[§6World§8]");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world save §8[§6World§8]");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world load §8[§6Name§8]");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world tp §8[§6World§8]");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world setspawn");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world spawn");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world list");
            sender.sendMessage(NMSMain.getPrefix() + " §c/world help");
        }
        return false;
    }
}
