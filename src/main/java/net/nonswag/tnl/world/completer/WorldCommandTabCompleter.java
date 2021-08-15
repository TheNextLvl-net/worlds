package net.nonswag.tnl.world.completer;

import net.nonswag.tnl.listener.api.player.TNLPlayer;
import net.nonswag.tnl.world.api.Environment;
import net.nonswag.tnl.world.api.WorldType;
import net.nonswag.tnl.world.api.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WorldCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
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
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (plugin.getDefaultWorldGenerator(args[1], null) != null) suggestions.add(plugin.getName());
                }
            }
        }
        if (!suggestions.isEmpty() && args.length >= 1) {
            suggestions.removeIf(suggestion -> !suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()));
        }
        return suggestions;
    }
}
