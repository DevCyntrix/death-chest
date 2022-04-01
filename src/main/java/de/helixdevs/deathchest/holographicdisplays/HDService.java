package de.helixdevs.deathchest.holographicdisplays;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class HDService implements IHologramService {

    private final Plugin plugin;

    public HDService(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
    }

    @Override
    public @NotNull IHologram spawnHologram(@NotNull Location location) {
        Hologram hologram = HologramsAPI.createHologram(plugin, location);
        return new HDHologram(this, hologram);
    }
}
