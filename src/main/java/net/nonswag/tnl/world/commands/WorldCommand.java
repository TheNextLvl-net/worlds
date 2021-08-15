package net.nonswag.tnl.world.commands;

import net.nonswag.tnl.listener.api.message.ChatComponent;
import net.nonswag.tnl.listener.api.message.Message;
import net.nonswag.tnl.world.api.Environment;
import net.nonswag.tnl.world.api.WorldType;
import net.nonswag.tnl.world.api.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WorldCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length >= 2) {
                    if (Bukkit.getWorld(args[1]) == null) {
                        if (args.length >= 3) {
                            WorldType type = WorldType.valueOf(args[2].toUpperCase());
                            if (args.length >= 4) {
                                Environment environment = Environment.getByName(args[3]);
                                if (environment != null) {
                                    if (args.length >= 5) {
                                        Plugin plugin = Bukkit.getPluginManager().getPlugin(args[4]);
                                        if (plugin != null && plugin.isEnabled()) {
                                            ChunkGenerator generator = plugin.getDefaultWorldGenerator(args[1], null);
                                            if (generator != null) {
                                                new WorldCreator(args[1]).generator(generator).environment(environment.getEnvironment()).type(type.getWorldType()).createWorld();
                                            } else {
                                                sender.sendMessage(Message.PREFIX.getText() + "§4" + plugin.getName() + "§c has no default generator");
                                            }
                                        } else {
                                            sender.sendMessage(Message.PREFIX.getText() + " §c/world create " + args[1] + " " + args[2] + " " + environment.name() + " §8(§6Plugin§8)");
                                        }
                                    } else {
                                        World world = new WorldCreator(args[1]).type(type.getWorldType()).environment(environment.getEnvironment()).createWorld();
                                        if (world != null) {
                                            sender.sendMessage(ChatComponent.getText("%prefix% §7Created World§8: §6" + args[1]));
                                        } else {
                                            sender.sendMessage(ChatComponent.getText("%prefix% §cFailed to create World §4" + args[1]));
                                        }
                                    }
                                } else {
                                    sender.sendMessage(ChatComponent.getText("%prefix% §c/world create " + args[1] + " " + args[2] + " §8[§6Environment§8] §8(§6Plugin§8)"));
                                }
                            } else {
                                sender.sendMessage(ChatComponent.getText("%prefix% §c/world create " + args[1] + " " + args[2] + " §8[§6Environment§8] §8(§6Plugin§8)"));
                            }
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §c/world create " + args[1] + " §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)"));
                        }
                    } else {
                        sender.sendMessage(ChatComponent.getText("%prefix% §cA world named §4" + args[1] + "§c already exist"));
                    }
                } else {
                    sender.sendMessage(ChatComponent.getText("%prefix% §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)"));
                }
            } else if (args[0].equalsIgnoreCase("tp")) {
                if (args.length == 2) {
                    if (sender instanceof Player) {
                        Player player = ((Player) sender);
                        World world = Bukkit.getWorld(args[1]);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            sender.sendMessage(Message.PREFIX.getText() + " §aTeleported you to the world spawn of world §6" + world.getName());
                        } else {
                            sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6World§8]");
                        }
                    } else {
                        sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    }
                } else if (args.length >= 3) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player != null) {
                        World world = Bukkit.getWorld(args[2]);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            if (sender.equals(player)) {
                                sender.sendMessage(Message.PREFIX.getText() + " §aTeleported you to the world spawn of world §6" + world.getName());
                            } else {
                                player.sendMessage("%prefix% §6" + sender.getName() + " §aTeleported you to the world spawn of world §6" + world.getName());
                                sender.sendMessage(Message.PREFIX.getText() + " §aTeleported §6" + player.getName() + "§a to the world spawn of world §6" + world.getName());
                            }
                        } else {
                            sender.sendMessage(Message.PREFIX.getText() + " §c/world tp " + player.getName() + " §8[§6World§8]");
                        }
                    } else {
                        sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                    sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("delete")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        if (WorldUtil.getInstance().deleteWorld(world)) {
                            sender.sendMessage(Message.PREFIX.getText() + " §7Deleted World§8: §6" + world.getName());
                        } else {
                            sender.sendMessage(Message.PREFIX.getText() + " §cFailed to delete World §4" + world.getName());
                        }
                    } else sender.sendMessage(Message.PREFIX.getText() + " §c/world delete §8[§6World§8]");
                } else sender.sendMessage(Message.PREFIX.getText() + " §c/world delete §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("import")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world == null) {
                        world = WorldUtil.getInstance().loadWorld(args[1]);
                        if (world == null) {
                            WorldCreator creator = new WorldCreator(args[1]);
                            creator.type(org.bukkit.WorldType.NORMAL);
                            creator.environment(World.Environment.NORMAL);
                            world = creator.createWorld();
                        }
                        if (world != null) {
                            sender.sendMessage(ChatComponent.getText("%prefix% §7Imported World§8: §6" + args[1]));
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §cFailed to import World §4" + args[1]));
                        }
                    } else sender.sendMessage(Message.PREFIX.getText() + " §cA world with this name already exist");
                } else sender.sendMessage(Message.PREFIX.getText() + " §c/world import §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("unload")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        if (WorldUtil.getInstance().unloadWorld(world, true)) {
                            sender.sendMessage(Message.PREFIX.getText() + " §7Unloaded World§8: §6" + world.getName());
                        } else {
                            sender.sendMessage(Message.PREFIX.getText() + " §cFailed to unload World §4" + world.getName());
                        }
                    } else {
                        sender.sendMessage(Message.PREFIX.getText() + " §c/world unload §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(Message.PREFIX.getText() + " §c/world unload §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("export")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world != null) {
                        world.save();
                        sender.sendMessage(Message.WORLD_SAVED_EN.getText());
                    } else {
                        sender.sendMessage(Message.PREFIX.getText() + " §c/world export §8[§6World§8]");
                    }
                } else {
                    sender.sendMessage(Message.PREFIX.getText() + " §c/world export §8[§6World§8]");
                }
            } else if (args[0].equalsIgnoreCase("load")) {
                if (args.length >= 2) {
                    World world = Bukkit.getWorld(args[1]);
                    if (world == null) {
                        world = WorldUtil.getInstance().loadWorld(args[1]);
                        if (world != null) {
                            sender.sendMessage(ChatComponent.getText("%prefix% §7Loaded World§8: §6" + args[1]));
                        } else {
                            sender.sendMessage(ChatComponent.getText("%prefix% §cFailed to load World §4" + args[1]));
                        }
                    } else sender.sendMessage(Message.PREFIX.getText() + " §cA world with this name already exist");
                } else sender.sendMessage(Message.PREFIX.getText() + " §c/world load §8[§6World§8]");
            } else if (args[0].equalsIgnoreCase("setspawn")) {
                if (sender instanceof Player) {
                    Player player = ((Player) sender);
                    player.getWorld().setSpawnLocation(player.getLocation());
                    Location l = player.getWorld().getSpawnLocation();
                    sender.sendMessage(Message.PREFIX.getText() + " §aSuccessfully set the spawn location to §6" + l.getX() + "§8, §6" + l.getY() + "§8, §6" + l.getZ());
                } else {
                    sender.sendMessage(Message.PREFIX.getText() + " §cThis is a player command");
                }
            } else if (args[0].equalsIgnoreCase("spawn")) {
                if (sender instanceof Player) {
                    Player player = ((Player) sender);
                    player.teleport(player.getWorld().getSpawnLocation());
                    sender.sendMessage(Message.PREFIX.getText() + " §aTeleported you to the world spawn of world §6" + player.getWorld().getName());
                } else sender.sendMessage(Message.PLAYER_COMMAND_EN.getText());
            } else if (args[0].equalsIgnoreCase("list")) {
                List<String> worlds = new ArrayList<>();
                Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));
                sender.sendMessage(Message.PREFIX.getText() + " §7Loaded Worlds §8(§6" + worlds.size() + "§8): §a" + String.join("§8, §a", worlds));
            } else {
                sender.sendMessage(Message.PREFIX.getText() + " §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world delete §8[§6World§8]");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world import §8[§6Name§8]");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world unload §8[§6World§8]");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world export §8[§6World§8]");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world load §8[§6Name§8]");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6World§8]");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world setspawn");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world spawn");
                sender.sendMessage(Message.PREFIX.getText() + " §c/world list");
            }
        } else {
            sender.sendMessage(Message.PREFIX.getText() + " §c/world create §8[§6Name§8] §8[§6Type§8] §8[§6Environment§8] §8(§6Plugin§8)");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6Player§8] §8[§6World§8]");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world delete §8[§6World§8]");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world import §8[§6Name§8]");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world unload §8[§6World§8]");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world export §8[§6World§8]");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world load §8[§6Name§8]");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world tp §8[§6World§8]");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world setspawn");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world spawn");
            sender.sendMessage(Message.PREFIX.getText() + " §c/world list");
        }
        return false;
    }
}
