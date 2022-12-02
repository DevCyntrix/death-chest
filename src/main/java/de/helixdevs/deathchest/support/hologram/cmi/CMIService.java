package de.helixdevs.deathchest.support.hologram.cmi;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMILocation;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class CMIService implements IHologramService {

    private final Plugin plugin;

    public CMIService(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull IHologram spawnHologram(@NotNull Location location) {
        plugin.getLogger().info("Spawning holograms with DecentHolograms");
        CMIHologram hologram = new CMIHologram(UUID.randomUUID().toString(), new CMILocation(location));
        CMI.getInstance().getHologramManager().addHologram(hologram);
        return new CMISupportHologram(this, hologram);
    }
}
