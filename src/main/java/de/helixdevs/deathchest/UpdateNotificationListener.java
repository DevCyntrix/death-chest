package de.helixdevs.deathchest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotificationListener implements Listener {

    private final DeathChestPlugin plugin;

    public UpdateNotificationListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNotifyUpdate(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getNewerVersion() == null)
            return;
        if (!player.hasPermission("deathchest.admin"))
            return;
        player.sendMessage("§8[§cDeath Chest§8] §cA new version " + plugin.getNewerVersion() + " is out.");
        player.sendMessage("§8[§cDeath Chest§8] §cPlease update the plugin at https://www.spigotmc.org/resources/death-chest.101066/");
    }

}
