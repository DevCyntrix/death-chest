package com.github.devcyntrix.deathchest.feature.update.views;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.feature.update.NewUpdate;
import org.bukkit.Bukkit;

import java.util.function.Consumer;

public class AdminNotificationView implements Consumer<NewUpdate> {

    private final DeathChestPlugin plugin;

    public AdminNotificationView(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(NewUpdate update) {
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("deathchest.update"))
                .forEach(player -> {
                    player.sendMessage(this.plugin.getPrefix() + "§cA new version " + update.version() + " is out.");

                    if (update.installed()) {
                        player.sendMessage(this.plugin.getPrefix() + "§cPlease restart the server to run the newest version.");
                    } else {
                        player.sendMessage(this.plugin.getPrefix() + "§cPlease update the plugin from " + plugin.getDescription().getWebsite());
                    }
                });
    }
}
