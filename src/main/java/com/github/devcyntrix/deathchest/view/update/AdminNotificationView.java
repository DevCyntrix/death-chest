package com.github.devcyntrix.deathchest.view.update;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import org.bukkit.Bukkit;

import java.util.function.Consumer;

public class AdminNotificationView implements Consumer<String> {

    private final DeathChestPlugin plugin;

    public AdminNotificationView(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(String version) {
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("deathchest.update"))
                .forEach(player -> {
                    player.sendMessage(this.plugin.getPrefix() + "§cA new version " + version + " is out.");
                    player.sendMessage(this.plugin.getPrefix() + "§cPlease update the plugin at " + plugin.getDescription().getWebsite());
                });
    }
}
