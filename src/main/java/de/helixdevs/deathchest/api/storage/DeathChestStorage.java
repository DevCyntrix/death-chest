package de.helixdevs.deathchest.api.storage;

import de.helixdevs.deathchest.api.DeathChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public interface DeathChestStorage extends Closeable {

    ConfigurationSection getDefaultOptions();

    void init(JavaPlugin plugin, ConfigurationSection section) throws IOException;

    void put(DeathChest chest);

    Collection<DeathChest> getChests();

    void remove(DeathChest chest);

    void save();

}
