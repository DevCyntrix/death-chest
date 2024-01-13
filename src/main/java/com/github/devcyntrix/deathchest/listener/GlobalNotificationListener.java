package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.event.DeathChestSpawnEvent;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.GlobalNotificationOptions;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class GlobalNotificationListener implements Listener {

    private final DeathChestPlugin plugin;

    @EventHandler
    public void onSpawn(DeathChestSpawnEvent event) {
        DeathChestConfig deathChestConfig = plugin.getDeathChestConfig();

        GlobalNotificationOptions globalNotificationOptions = deathChestConfig.globalNotificationOptions();
        if (globalNotificationOptions.enabled() && globalNotificationOptions.message() != null) {
            globalNotificationOptions.showNotification(event.getDeathChest(), plugin.getPlaceHolderController());
        }
    }

}
