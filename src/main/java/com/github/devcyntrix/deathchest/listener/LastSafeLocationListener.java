package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.controller.LastSafeLocationController;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class LastSafeLocationListener implements Listener {

    private final DeathChestPlugin plugin;

    public LastSafeLocationListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!hasBlockChanged(event))
            return;
        Player player = event.getPlayer();
        Block block1 = player.getLocation().getBlock();
        if (!block1.isEmpty() && !block1.isLiquid())
            return;

        Location location = player.getLocation().clone().subtract(0, 0.2, 0);
        Block block = location.getBlock();
        if (block.isEmpty())
            return;
        LastSafeLocationController controller = plugin.getLastSafeLocationController();
        controller.updatePosition(player);
    }

    public static boolean hasBlockChanged(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null)
            return true;
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ() || from.getWorld() != to.getWorld();
    }


}
