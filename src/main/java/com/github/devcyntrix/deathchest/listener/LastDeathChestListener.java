package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestDestroyEvent;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.DeathChestSpawnEvent;
import com.github.devcyntrix.deathchest.api.DeathChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Comparator;
import java.util.Optional;

public class LastDeathChestListener implements Listener {

    private final DeathChestPlugin plugin;

    public LastDeathChestListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        Optional<DeathChest> first = this.plugin.getChests()
                .filter(deathChest -> deathChest.getPlayer() != null)
                .filter(deathChest -> player.equals(deathChest.getPlayer()))
                .max(Comparator.comparingLong(DeathChest::getCreatedAt));
        if (first.isEmpty())
            return;
        plugin.getLastDeathChests().put(player, first.get());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(DeathChestSpawnEvent event) {
        Player player = event.getPlayer();
        DeathChest deathChest = event.getDeathChest();
        DeathChest oldChest = plugin.getLastDeathChests().get(player);
        if (oldChest == null) {
            plugin.getLastDeathChests().put(player, deathChest);
            return;
        }
        if (deathChest.getCreatedAt() > oldChest.getCreatedAt()) {
            plugin.getLastDeathChests().put(player, deathChest);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDestroy(DeathChestDestroyEvent event) {
        if (event.getDeathChest().getPlayer() == null)
            return;

        Player player = event.getDeathChest().getPlayer().getPlayer();
        if (player == null)
            return;

        Optional<DeathChest> first = this.plugin.getChests()
                .filter(deathChest -> deathChest.getPlayer() != null)
                .filter(deathChest -> event.getDeathChest().getPlayer().equals(deathChest.getPlayer()))
                .max(Comparator.comparingLong(DeathChest::getCreatedAt));
        if (first.isEmpty()) {
            plugin.getLastDeathChests().remove(player);
            return;
        }

        plugin.getLastDeathChests().put(player, first.get());
    }

}
