package com.github.devcyntrix.deathchest.view.update;

import org.bukkit.Bukkit;

import java.util.function.Consumer;

public class AdminNotificationView implements Consumer<String> {

    @Override
    public void accept(String version) {
        Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("deathchest.update"))
                .forEach(player -> {
                    player.sendMessage("§8[§cDeath Chest§8] §cA new version " + version + " is out.");
                    player.sendMessage("§8[§cDeath Chest§8] §cPlease update the plugin at https://www.spigotmc.org/resources/death-chest.101066/");
                });
    }
}
