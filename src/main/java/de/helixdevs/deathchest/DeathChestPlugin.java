package de.helixdevs.deathchest;

import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * This plugin will create chests on death and will destroy them in after a specific time.
 */
public class DeathChestPlugin extends JavaPlugin implements Listener {

    private final Set<DeathChest> deathChests = new HashSet<>();

    private DeathChestConfig deathChestConfig;
    private ProtectionQuery protectionQuery;

    @Override
    public void onDisable() {
        // Reset all death chests
        this.deathChests.forEach(DeathChest::close);
        this.deathChests.clear();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        this.deathChestConfig = DeathChestConfig.load(getConfig());
        this.protectionQuery = WorldGuardPlugin.inst().createProtectionQuery();

        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Creates the death chest if the player dies.
     * It only spawns a chest if the player has the permission to build in the region.
     * It puts the drops into the chest and clears the drops.
     *
     * @param event the event from the bukkit api
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory())
            return;
        if (event.getDrops().isEmpty())
            return;

        Player player = event.getPlayer();
        Location deathLocation = player.getLocation();

        // Check protection
        boolean build = this.protectionQuery.testBlockPlace(player, deathLocation, Material.CHEST);
        if (!build)
            return;

        // Spawns the death chest
        deathLocation.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) deathLocation.getBlock().getState();
        DeathChest deathChest = new DeathChest(
                this,
                chest,
                deathChestConfig.getExpiration(),
                event.getDrops().toArray(new ItemStack[0]));
        getServer().getPluginManager().registerEvents(deathChest, this);
        this.deathChests.add(deathChest);

        String[] notificationMessage = deathChestConfig.getNotificationMessage();
        if (notificationMessage != null) {
            player.sendMessage(notificationMessage);
        }

        // Clears the drops
        event.getDrops().clear();
    }

    public DeathChestConfig getDeathChestConfig() {
        return deathChestConfig;
    }

    public ProtectionQuery getProtectionQuery() {
        return protectionQuery;
    }

    public void unregisterChest(DeathChest chest) {
        this.deathChests.remove(chest);
    }
}
