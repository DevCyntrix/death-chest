package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.ThiefProtectionOptions;
import com.github.devcyntrix.deathchest.controller.DeathChestController;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ChestInteractionListener implements Listener {

    private final DeathChestPlugin plugin;

    public ChestInteractionListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }


    /**
     * Opens the inventory from the death chest if the player interacts with the chest and
     * opens the chest if it is the first viewer.
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler
    public void onOpenChest(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        if (event.isBlockInHand() && player.isSneaking()) // That maintains the natural minecraft feeling
            return;

        DeathChestController controller = plugin.getDeathChestController();
        DeathChestModel model = controller.getChest(block.getLocation());
        if (model == null)
            return;

        event.setCancelled(true);


        // Chest Protection
        if (!controller.isAccessibleBy(model, player)) {
            ThiefProtectionOptions protectionOptions = plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions();
            protectionOptions.playSound(player, block.getLocation());
            protectionOptions.notify(player);
            return;
        }

        if (model.getInventory().isEmpty()) { //
            return;
        }

        if (block.getState() instanceof Lidded lidded && player.getGameMode() != GameMode.SPECTATOR) {
            try {
                lidded.open();
            } catch (Exception ignored) {
            }
        }

        player.openInventory(model.getInventory());
    }


}
