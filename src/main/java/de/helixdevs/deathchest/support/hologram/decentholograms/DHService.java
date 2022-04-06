package de.helixdevs.deathchest.support.hologram.decentholograms;

import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class DHService implements IHologramService {

    private final Plugin plugin;

    public DHService(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull IHologram spawnHologram(@NotNull Location location) {
        plugin.getLogger().info("Spawning hologram with decent holograms");
        Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), location, false);
        return new DHHologram(this, hologram);
    }
}
