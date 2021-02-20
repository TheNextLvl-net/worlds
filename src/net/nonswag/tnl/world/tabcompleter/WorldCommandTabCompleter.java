package net.nonswag.tnl.world.tabcompleter;

import net.nonswag.tnl.world.Worlds;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class WorldCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (sender.hasPermission("tnl.worlds")) {
            if (args.length <= 1) {
                suggestions.add("create");
                suggestions.add("tp");
                suggestions.add("delete");
                suggestions.add("import");
                suggestions.add("unload");
                suggestions.add("save");
                suggestions.add("load");
                suggestions.add("setspawn");
                suggestions.add("spawn");
                suggestions.add("list");
                suggestions.add("help");
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("save")
                        || args[0].equalsIgnoreCase("delete")
                        || args[0].equalsIgnoreCase("unload")) {
                    for (World world : Bukkit.getWorlds()) {
                        suggestions.add(world.getName());
                    }
                } else if (args[0].equalsIgnoreCase("tp")) {
                    for (World world : Bukkit.getWorlds()) {
                        suggestions.add(world.getName());
                    }
                    for (Player all : Bukkit.getOnlinePlayers()) {
                        suggestions.add(all.getName());
                    }
                } else if (args[0].equalsIgnoreCase("load")) {
                    suggestions.addAll(Worlds.getWorlds());
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("tp")) {
                    if (Bukkit.getPlayer(args[1]) != null) {
                        for (World world : Bukkit.getWorlds()) {
                            suggestions.add(world.getName());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("create")) {
                    for (WorldType worldType : WorldType.values()) {
                        suggestions.add(worldType.name());
                    }
                }
            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("create")) {
                    for (World.Environment environment : World.Environment.values()) {
                        suggestions.add(environment.name());
                    }
                }
            } else if (args.length == 5) {
                if (args[0].equalsIgnoreCase("create")) {
                    for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                        if (plugin.getDefaultWorldGenerator("test", null) != null) {
                            suggestions.add(plugin.getName());
                        }
                    }
                }
            }
            if (!suggestions.isEmpty() && args.length >= 1) {
                suggestions.removeIf(suggestion -> !suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()));
            }
        }
        return suggestions;
    }
}
