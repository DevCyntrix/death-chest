package de.helixdevs.deathchest.tasks;

import de.helixdevs.deathchest.DeathChestPlugin;
import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class HologramRunnable extends BukkitRunnable {

    private final DeathChest chest;
    private final Map<String, IHologramTextLine> blueprints;
    private final StringSubstitutor substitutor;

    public HologramRunnable(DeathChest chest, Map<String, IHologramTextLine> blueprints, StringSubstitutor substitutor) {
        this.chest = chest;
        this.blueprints = blueprints;
        this.substitutor = substitutor;
    }

    @Override
    public void run() {
        // Updates the hologram lines
        blueprints.forEach((text, line) -> {
            if (substitutor != null) text = substitutor.replace(text);
            if (DeathChestPlugin.isPlaceholderAPIEnabled())
                text = PlaceholderAPI.setPlaceholders(chest.getPlayer(), text);
            String finalText = text;
            Bukkit.getScheduler().runTask(chest.getPlugin(), () -> line.rename(finalText));
        });
    }
}
