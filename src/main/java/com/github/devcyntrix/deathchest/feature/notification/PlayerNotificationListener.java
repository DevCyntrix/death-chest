package com.github.devcyntrix.deathchest.feature.notification;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.event.DeathChestSpawnEvent;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.PlayerNotificationOptions;
import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import com.github.devcyntrix.deathchest.feature.placeholder.PlaceholderService;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerNotificationListener implements Listener {

    private final DeathChestPlugin plugin;

    public PlayerNotificationListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(DeathChestSpawnEvent event) {
        DeathChestConfig deathChestConfig = plugin.getDeathChestConfig();

        Player player = event.getPlayer();
        Audience audience = plugin.getAudiences().player(player);
        DeathChestModel deathChest = event.getDeathChest();
        PlaceholderService controller = plugin.getPlaceHolderService();

        // Player notification
        PlayerNotificationOptions playerNotificationOptions = deathChestConfig.playerNotificationOptions();
        if (playerNotificationOptions.enabled() && playerNotificationOptions.message() != null) {
            playerNotificationOptions.showNotification(audience, deathChest, controller);
        }
    }

}
