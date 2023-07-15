package com.github.devcyntrix.deathchest.view.chest;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.util.ChestListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ChestSpawnListener implements ChestListener {

    private final Plugin plugin;

    public ChestSpawnListener(Plugin plugin) {
        this.plugin = plugin;
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
        if (model.getPrevious() == null)
            return;
        Location location = model.getLocation();
        Block block = location.getBlock();
        location.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.clone().add(0.5, 0.5, 0.5), 10, block.getBlockData());
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
}
