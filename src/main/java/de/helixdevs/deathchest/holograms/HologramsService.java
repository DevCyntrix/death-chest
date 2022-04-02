package de.helixdevs.deathchest.holograms;

import com.sainttx.holograms.api.Hologram;
import com.sainttx.holograms.api.HologramManager;
import com.sainttx.holograms.api.HologramPlugin;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class HologramsService implements IHologramService {

    private final Plugin plugin;
    private final HologramManager hologramManager;

    public HologramsService(Plugin plugin) {
        this.plugin = plugin;
        this.hologramManager = JavaPlugin.getPlugin(HologramPlugin.class).getHologramManager();
    }

    @Override
    public @NotNull IHologram spawnHologram(@NotNull Location location) {
        Hologram hologram = new Hologram(UUID.randomUUID().toString(), location);
        hologramManager.addActiveHologram(hologram);
        return new HologramsHologram(this, hologram);
    }
}
