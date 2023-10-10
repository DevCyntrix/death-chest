package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.ChestView;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public class BlockView implements ChestView, Listener {

    private final DeathChestPlugin plugin;

    public BlockView(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCreate(DeathChestModel model) {
        // Creates the chest in the next tick because if you try to sleep in the nether the explosion spawns after the player dies. That means the chest would be destroyed by the explosion.
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.debug(0, "Creating death chest block...");
                Location location = model.getLocation();
                BlockState state = location.getBlock().getState();
                model.setPrevious(state);
                location.getBlock().setType(Material.CHEST);
            }
        }.runTask(plugin);
        model.getTasks().add(bukkitTask::cancel);
    }

    @Override
    public void onDestroy(DeathChestModel model) {

        try {
            World world = model.getWorld();
            if (world != null) {
                plugin.debug(0, "Spawning block crack particle...");
                Location location = model.getLocation();
                Block block = location.getBlock();
                if (!plugin.isTest())
                    world.spawnParticle(Particle.BLOCK_CRACK, location.clone().add(0.5, 0.5, 0.5), 10, block.getBlockData());
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to play block crack particle", e);
        }

        if (model.getPrevious() == null)
            return;
        plugin.debug(0, "Resetting the death chest block...");
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
