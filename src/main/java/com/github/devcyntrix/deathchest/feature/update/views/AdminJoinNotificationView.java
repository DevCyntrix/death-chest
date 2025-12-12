package com.github.devcyntrix.deathchest.feature.update.views;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.feature.update.NewUpdate;
import com.github.devcyntrix.deathchest.feature.update.UpdateService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AdminJoinNotificationView implements Listener {

    private final DeathChestPlugin plugin;
    private final UpdateService controller;

    public AdminJoinNotificationView(DeathChestPlugin plugin, UpdateService controller) {
        this.plugin = plugin;
        this.controller = controller;
    }

    @EventHandler
    public void onNotifyUpdate(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NewUpdate update = controller.getNewestVersion();
        if (update == null)
            return;
        if (!player.hasPermission("deathchest.update"))
            return;
        player.sendMessage(plugin.getPrefix() + "§cA new version " + update.version() + " is out.");

        if (update.installed()) {
            player.sendMessage(this.plugin.getPrefix() + "§cPlease restart the server to run the newest version.");
        } else {
            player.sendMessage(this.plugin.getPrefix() + "§cPlease update the plugin from " + plugin.getDescription().getWebsite());
        }
    }

}
