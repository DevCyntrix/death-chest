package com.github.devcyntrix.deathchest.feature.lastchest;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.event.DeathChestDestroyEvent;
import com.github.devcyntrix.deathchest.api.event.DeathChestSpawnEvent;
import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
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
        LastDeathChestService controller = plugin.getLastDeathChestService();
        controller.getLastDeathChests().put(player, first.get());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(DeathChestSpawnEvent event) {
        Player player = event.getPlayer();
        DeathChestModel deathChest = event.getDeathChest();
        LastDeathChestService controller = plugin.getLastDeathChestService();
        DeathChestModel oldChest = controller.getLastDeathChest(player);
        if (oldChest == null) {
            controller.getLastDeathChests().put(player, deathChest);
            return;
        }
        if (deathChest.getCreatedAt() > oldChest.getCreatedAt()) {
            controller.getLastDeathChests().put(player, deathChest);
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

        LastDeathChestService controller = plugin.getLastDeathChestService();
        if (first.isEmpty()) {
            controller.getLastDeathChests().remove(player);
            return;
        }
        controller.getLastDeathChests().put(player, first.get());
    }

}
