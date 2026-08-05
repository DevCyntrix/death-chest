package com.github.devcyntrix.deathchest.feature.chest.views;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.ChestListener;
import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import com.github.devcyntrix.deathchest.util.ParticleUtils;
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

public class BlockCreationChestListener implements ChestListener, Listener {

    private final DeathChestPlugin plugin;
    private final Particle blockCrackParticle;

    public BlockCreationChestListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
        this.blockCrackParticle = ParticleUtils.findParticle("BLOCK", "BLOCK_CRACK");
    }

    @Override
    public void onCreate(DeathChestModel model) {
        // Creates the chest in the next tick because if you try to sleep in the nether the explosion spawns after the player dies. That means the chest would be destroyed by the explosion.
        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.debug(0, "Creating death chest block...");
                Location location = model.getLocation();
                World world = location.getWorld();
                if (world == null)
                    return;

                // Fix: ensure the chunk is loaded before placing the block, retrying for a few ticks.
                // Previously the chest block was set on a possibly unloaded chunk when the player
                // disconnected right after dying, which silently discarded the block change and
                // caused the items (already cleared from the world) to be lost forever.
                int chunkX = location.getBlockX() >> 4;
                int chunkZ = location.getBlockZ() >> 4;
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    // Try to load the chunk synchronously and retry in the same run
                    world.getChunkAt(chunkX, chunkZ);
                }

                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    // Chunk still not loaded - retry for up to 40 ticks (2 seconds)
                    new BukkitRunnable() {
                        private int attempts = 0;
                        @Override
                        public void run() {
                            attempts++;
                            if (world.isChunkLoaded(chunkX, chunkZ) || attempts >= 40) {
                                if (attempts >= 40 && !world.isChunkLoaded(chunkX, chunkZ)) {
                                    plugin.debug(0, "Failed to load chunk for death chest at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
                                } else {
                                    placeChest(model, location, world);
                                }
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 1, 1);
                    return;
                }

                placeChest(model, location, world);
            }
        }.runTask(plugin);
        model.getTasks().add(bukkitTask::cancel);
    }

    private void placeChest(DeathChestModel model, Location location, World world) {
        BlockState state = location.getBlock().getState();
        model.setPrevious(state);
        location.getBlock().setType(Material.CHEST);
        plugin.debug(0, "Death chest block created at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
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
                    world.spawnParticle(blockCrackParticle, location.clone().add(0.5, 0.5, 0.5), 10, block.getBlockData());
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
