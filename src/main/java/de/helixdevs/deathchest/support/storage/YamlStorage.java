package de.helixdevs.deathchest.support.storage;

import com.google.common.collect.Lists;
import de.helixdevs.deathchest.DeathChestSnapshotImpl;
import de.helixdevs.deathchest.api.DeathChestSnapshot;
import de.helixdevs.deathchest.api.storage.DeathChestStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

/**
 * Currently not used but will be implemented soon.
 */
public class YamlStorage implements DeathChestStorage {

    private File file;
    private final Set<DeathChestSnapshot> deathChests = new HashSet<>();

    @Override
    public ConfigurationSection getDefaultOptions() {
        ConfigurationSection section = new MemoryConfiguration();
        section.addDefault("file", "chests.yml");
        return section;
    }

    @Override
    public void init(JavaPlugin plugin, ConfigurationSection section) throws IOException {
        String filename = section.getString("file", "saved-chests.yml");
        this.file = new File(plugin.getDataFolder(), filename);
        if (!this.file.isFile()) {
            this.file.createNewFile();
            return;
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.file);
        List<?> chests = configuration.getList("chests", Collections.emptyList());

        if (chests == null)
            return;

        for (Object chest : chests) {
            if (!(chest instanceof Map<?, ?> map))
                continue;
            DeathChestSnapshot deserialize = DeathChestSnapshotImpl.deserialize((Map<String, Object>) map);
            if (deserialize == null)
                continue;
            this.deathChests.add(deserialize);
        }
    }

    @Override
    public void put(DeathChestSnapshot chest) {
        this.deathChests.add(chest);
    }

    @Override
    public void putAll(Collection<DeathChestSnapshot> chests) {
        this.deathChests.addAll(chests);
    }

    @Override
    public Set<DeathChestSnapshot> getChests() {
        return this.deathChests;
    }

    @Override
    public void remove(DeathChestSnapshot chest) {
        this.deathChests.remove(chest);
    }

    @Override
    public void save() throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        List<Map<String, Object>> collect = deathChests.stream()
                .map(DeathChestSnapshot::serialize)
                .collect((Supplier<List<Map<String, Object>>>) Lists::newArrayList, List::add, List::addAll);
        configuration.set("chests", collect);
        configuration.save(file);
    }

    @Override
    public void close() throws IOException {

    }
}
