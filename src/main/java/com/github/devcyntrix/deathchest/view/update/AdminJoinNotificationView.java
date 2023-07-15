package com.github.devcyntrix.deathchest.view.update;

import com.github.devcyntrix.deathchest.controller.UpdateController;
import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AdminJoinNotificationView implements Listener {

    private UpdateController controller;

    public AdminJoinNotificationView(UpdateController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onNotifyUpdate(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (controller.getNewestVersion() == null)
            return;
        if (!player.hasPermission("deathchest.admin"))
            return;
        player.sendMessage("§8[§cDeath Chest§8] §cA new version " + controller.getNewestVersion() + " is out.");
        player.sendMessage("§8[§cDeath Chest§8] §cPlease update the plugin at https://www.spigotmc.org/resources/death-chest.101066/");
    }

}
