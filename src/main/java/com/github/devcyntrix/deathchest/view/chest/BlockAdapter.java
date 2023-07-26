package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.controller.DeathChestController;
import com.github.devcyntrix.deathchest.util.ChestAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockAdapter implements ChestAdapter, Listener {

    private final DeathChestPlugin plugin;
    private DeathChestController controller;

    public BlockAdapter(DeathChestPlugin plugin, DeathChestController controller) {
        this.plugin = plugin;
        this.controller = controller;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        // Creates the chest in the next tick because if you try to sleep in the nether the explosion spawns after the player dies. That means the chest would be destroyed by the explosion.
        new BukkitRunnable() {
            @Override
            public void run() {
                Location location = model.getLocation();
                BlockState state = location.getBlock().getState();
                model.setPrevious(state);
                location.getBlock().setType(Material.CHEST);
            }
        }.runTask(plugin);
    }

    @Override
    public void onDestroy(DeathChestModel model) {
        try {
            if (model.getLocation() != null) {
                Block block = model.getLocation().getBlock();
                model.getWorld().spawnParticle(Particle.BLOCK_CRACK, model.getLocation().clone().add(0.5, 0.5, 0.5), 10, block.getBlockData().getMaterial().getNewData((byte) 0));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to play block crack particle");
            e.printStackTrace();
        }

        if (model.getPrevious() == null)
            return;
        model.getPrevious().update(true, false);
    }


    @Override
    public void onLoad(DeathChestModel model) {
        onCreate(model);
    }

    @Override
    public void onUnload(DeathChestModel model) {
        onDestroy(model);
    }


//    /**
//     * Tries to detect an empty inventory to close the inventory and to destroy the chest.
//     *
//     * @param event the event from the Bukkit API
//     */
//    @EventHandler
//    public void onClick(InventoryClickEvent event) {
//        if (!inventory.equals(event.getView().getTopInventory())) return;
//        if (inventory.isEmpty()) {
//            event.getWhoClicked().closeInventory();
//        }
//    }

//    /**
//     * Tries to detect an empty inventory to close the inventory and to destroy the chest.
//     *
//     * @param event the event from the Bukkit API
//     */
//    @EventHandler
//    public void onDrag(InventoryDragEvent event) {
//        if (!inventory.equals(event.getInventory())) return;
//        if (inventory.isEmpty()) {
//            event.getWhoClicked().closeInventory();
//        }
//    }

}
