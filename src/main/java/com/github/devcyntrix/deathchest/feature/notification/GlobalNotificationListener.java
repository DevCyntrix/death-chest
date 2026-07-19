package com.github.devcyntrix.deathchest.feature.notification;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.event.DeathChestSpawnEvent;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.GlobalNotificationOptions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GlobalNotificationListener implements Listener {

    private final DeathChestPlugin plugin;

    public GlobalNotificationListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(DeathChestSpawnEvent event) {
        DeathChestConfig deathChestConfig = plugin.getDeathChestConfig();

        GlobalNotificationOptions globalNotificationOptions = deathChestConfig.globalNotificationOptions();
        if (globalNotificationOptions.enabled() && globalNotificationOptions.message() != null) {
            globalNotificationOptions.showNotification(event.getDeathChest(), event.getPlayer(), plugin.getPlaceHolderService());
        }
    }

}
