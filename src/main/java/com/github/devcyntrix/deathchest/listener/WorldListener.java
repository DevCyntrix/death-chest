package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * Loads and unloads the chests if a world is loading or unloading during the runtime
 */
public class WorldListener implements Listener {

    private final DeathChestPlugin plugin;

    public WorldListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.getDeathChestController().loadChests(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        plugin.getDeathChestController().unloadChests(event.getWorld(), true);
    }

}
