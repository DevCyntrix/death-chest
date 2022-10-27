package de.helixdevs.deathchest.listener;

import de.helixdevs.deathchest.DeathChestPlugin;
import de.helixdevs.deathchest.config.DeathChestConfig;
import de.helixdevs.deathchest.config.GlobalNotificationOptions;
import de.helixdevs.deathchest.config.PlayerNotificationOptions;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Map;

public class SpawnChestListener implements Listener {

    private final DeathChestPlugin plugin;

    public SpawnChestListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates the death chest if the player dies.
     * It only spawns a chest if the player has the permission to build in the region.
     * It puts the drops into the chest and clears the drops.
     *
     * @param event the event from the bukkit api
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory())
            return;
        if (event.getDrops().isEmpty())
            return;

        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        if (!plugin.getDeathChestConfig().worldFilterConfig().test(deathLocation.getWorld()))
            return;

        // Check Minecraft limitation of block positions
        if (deathLocation.getBlockY() <= player.getWorld().getMinHeight()) // Min build height
            return;
        if (deathLocation.getBlockY() >= player.getWorld().getMaxHeight()) // Max build height
            return;

        // Check protection
        boolean build = plugin.getProtectionService().canBuild(player, deathLocation, Material.CHEST);
        if (!build)
            return;

        DeathChestConfig deathChestConfig = plugin.getDeathChestConfig();
        Duration expiration = deathChestConfig.expiration();
        if (expiration == null)
            expiration = Duration.ofSeconds(-1);

        long createdAt = System.currentTimeMillis();

        long expireAt;
        if (!expiration.isNegative() && !expiration.isZero()) {
            expireAt = createdAt + expiration.toMillis();
        } else {
            expireAt = -1; // Permanent
        }

        try {
            plugin.createDeathChest(deathLocation.getBlock().getLocation(), createdAt, expireAt, player, event.getDrops().toArray(new ItemStack[0]));

            // Clears the drops
            event.getDrops().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Player notification
        PlayerNotificationOptions playerNotificationOptions = deathChestConfig.playerNotificationOptions();
        if (playerNotificationOptions != null && playerNotificationOptions.enabled() && playerNotificationOptions.messages() != null) {
            StringSubstitutor substitutor = new StringSubstitutor(Map.of(
                    "x", deathLocation.getBlockX(),
                    "y", deathLocation.getBlockY(),
                    "z", deathLocation.getBlockZ(),
                    "world", deathLocation.getWorld().getName()));
            for (String message : playerNotificationOptions.messages()) {
                message = substitutor.replace(message);

                if (plugin.isPlaceholderAPIEnabled())
                    message = PlaceholderAPI.setPlaceholders(player, message);

                player.sendMessage(message);
            }
        }

        // Global notification
        GlobalNotificationOptions globalNotificationOptions = deathChestConfig.globalNotificationOptions();
        if (globalNotificationOptions != null && globalNotificationOptions.enabled() && globalNotificationOptions.messages() != null) {
            StringSubstitutor substitutor = new StringSubstitutor(Map.of(
                    "x", deathLocation.getBlockX(),
                    "y", deathLocation.getBlockY(),
                    "z", deathLocation.getBlockZ(),
                    "world", deathLocation.getWorld().getName(),
                    "player_name", player.getName(),
                    "player_displayname", player.getDisplayName()));

            for (String message : globalNotificationOptions.messages()) {
                message = substitutor.replace(message);

                if (plugin.isPlaceholderAPIEnabled())
                    message = PlaceholderAPI.setPlaceholders(player, message);

                Bukkit.broadcastMessage(message);
            }
        }


    }

}
