package net.thenextlvl.worlds.link;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import core.file.FileIO;
import core.file.format.GsonFile;
import core.io.IO;
import core.paper.adapters.world.WorldAdapter;
import lombok.RequiredArgsConstructor;
import net.thenextlvl.worlds.WorldsPlugin;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class CraftLinkRegistry implements LinkRegistry {
    private final Set<Link> links = new HashSet<>();
    private final WorldsPlugin plugin;

    @Override
    public Set<Link> getLinks() {
        return Set.copyOf(links);
    }

    @Override
    public boolean isRegistered(Link link) {
        return links.contains(link);
    }

    @Override
    public boolean register(Link link) {
        return links.add(link);
    }

    @Override
    public boolean unregister(Link link) {
        return links.remove(link);
    }

    @Override
    public boolean unregisterAll(World world) {
        return links.removeIf(link -> link.source().equals(world) || link.destination().equals(world));
    }

    public void saveLinks() {
        var file = loadFile();
        file.setRoot(links);
        file.save();
    }

    public void loadLinks() {
        var file = loadFile();
        links.addAll(file.getRoot());
    }

    private FileIO<Set<Link>> loadFile() {
        return new GsonFile<>(
                IO.of(plugin.getDataFolder(), "links.json"),
                new HashSet<>(), new TypeToken<>() {
        }, new GsonBuilder()
                .registerTypeHierarchyAdapter(World.class, WorldAdapter.Key.INSTANCE)
                .setPrettyPrinting()
                .create());
    }
}
