package com.github.devcyntrix.deathchest.view.update;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.controller.UpdateController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AdminJoinNotificationView implements Listener {

    private final DeathChestPlugin plugin;
    private final UpdateController controller;

    public AdminJoinNotificationView(DeathChestPlugin plugin, UpdateController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }

    @EventHandler
    public void onNotifyUpdate(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (controller.getNewestVersion() == null)
            return;
        if (!player.hasPermission("deathchest.update"))
            return;
        player.sendMessage(plugin.getPrefix() + "§cA new version " + controller.getNewestVersion() + " is out.");
        player.sendMessage(plugin.getPrefix() + "§cPlease update the plugin at " + plugin.getDescription().getWebsite());
    }

}
