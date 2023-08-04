package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.event.DeathChestDestroyEvent;
import com.github.devcyntrix.deathchest.api.event.DeathChestSpawnEvent;
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
        Optional<DeathChestModel> first = this.plugin.getChests()
                .filter(deathChest -> deathChest.getOwner() != null)
                .filter(deathChest -> player.equals(deathChest.getOwner()))
                .max(Comparator.comparingLong(DeathChestModel::getCreatedAt));
        if (first.isEmpty())
            return;
        plugin.getLastDeathChests().put(player, first.get());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(DeathChestSpawnEvent event) {
        Player player = event.getPlayer();
        DeathChestModel deathChest = event.getDeathChest();
        DeathChestModel oldChest = plugin.getLastDeathChests().get(player);
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
        if (event.getDeathChest().getOwner() == null)
            return;

        Player player = event.getDeathChest().getOwner().getPlayer();
        if (player == null)
            return;

        Optional<DeathChestModel> first = this.plugin.getChests()
                .filter(deathChest -> deathChest.getOwner() != null)
                .filter(deathChest -> event.getDeathChest().getOwner().equals(deathChest.getOwner()))
                .max(Comparator.comparingLong(DeathChestModel::getCreatedAt));
        if (first.isEmpty()) {
            plugin.getLastDeathChests().remove(player);
            return;
        }

        plugin.getLastDeathChests().put(player, first.get());
    }

}
