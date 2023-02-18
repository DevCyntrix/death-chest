package de.helixdevs.deathchest.api.storage;

import de.helixdevs.deathchest.api.DeathChestSnapshot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public interface DeathChestStorage extends Closeable {

    ConfigurationSection getDefaultOptions();

    void init(JavaPlugin plugin, ConfigurationSection section) throws IOException;

    void put(DeathChestSnapshot chest);

    void putAll(Collection<DeathChestSnapshot> chests);

    Set<DeathChestSnapshot> getChests();

    void remove(DeathChestSnapshot chest);

    void save() throws IOException;

}
