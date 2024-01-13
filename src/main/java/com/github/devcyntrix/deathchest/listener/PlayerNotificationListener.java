package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.event.DeathChestSpawnEvent;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.PlayerNotificationOptions;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class PlayerNotificationListener implements Listener {

    private final DeathChestPlugin plugin;

    @EventHandler
    public void onSpawn(DeathChestSpawnEvent event) {
        DeathChestConfig deathChestConfig = plugin.getDeathChestConfig();

        Player player = event.getPlayer();
        Audience audience = plugin.getAudiences().player(player);
        DeathChestModel deathChest = event.getDeathChest();
        PlaceholderController controller = plugin.getPlaceHolderController();

        // Player notification
        PlayerNotificationOptions playerNotificationOptions = deathChestConfig.playerNotificationOptions();
        if (playerNotificationOptions.enabled() && playerNotificationOptions.message() != null) {
            playerNotificationOptions.showNotification(audience, deathChest, controller);
        }
    }

}
