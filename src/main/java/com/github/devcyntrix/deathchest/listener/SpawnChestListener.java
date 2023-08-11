package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.event.DeathChestSpawnEvent;
import com.github.devcyntrix.deathchest.config.*;
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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * This class is only for handling if a player dies. Here is the spawning of a death chest written and some checks if a
 * chest should spawn. Additionally, here is the death message, custom info and broadcast message implemented.
 */
public class SpawnChestListener implements Listener {

    private final DeathChestPlugin plugin;

    public SpawnChestListener(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    private Set<Player> set = Collections.newSetFromMap(new WeakHashMap<>());

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        set.remove(event.getPlayer());
    }

    /**
     * Creates the death chest if the player dies.
     * It only spawns a chest if the player has the permission to build in the region.
     * It puts the drops into the chest and clears the drops.
     *
     * @param event the event from the bukkit api
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        Location location = player.getLocation();
        if(set.contains(player)) {
            event.getDrops().clear();
            return;
        }
        set.add(player);


        ChangeDeathMessageOptions changeDeathMessageOptions = plugin.getDeathChestConfig().changeDeathMessageOptions();
        if (changeDeathMessageOptions.enabled()) {
            if (changeDeathMessageOptions.message() == null) {
                event.setDeathMessage(null);
                return;
            }
            if (location.getWorld() == null) {
                return; // Invalid location
            }

            StringSubstitutor substitutor = new StringSubstitutor(Map.of(
                    "x", location.getBlockX(),
                    "y", location.getBlockY(),
                    "z", location.getBlockZ(),
                    "world", location.getWorld().getName(),
                    "player_name", player.getName(),
                    "player_displayname", player.getDisplayName()));
            event.setDeathMessage(Arrays.stream(changeDeathMessageOptions.message()).map(substitutor::replace).map(s -> {
                if (DeathChestPlugin.isPlaceholderAPIEnabled()) {
                    return PlaceholderAPI.setPlaceholders(player, s);
                }
                return s;
            }).collect(Collectors.joining("\n")));
        }

        if (event.getKeepInventory())
            return;
        event.getDrops().removeIf(itemStack -> itemStack.getType() == Material.AIR); // Prevent spawning an empty chest
        if (event.getDrops().isEmpty())
            return;

        Location deathLocation = new Location(
                player.getWorld(),
                player.getLocation().getX(),
                Math.round(player.getLocation().getY()),
                player.getLocation().getZ()
        );

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

        NoExpirationPermission permission = deathChestConfig.noExpirationPermission();
        boolean expires = permission == null || !permission.enabled() || !player.hasPermission(permission.permission());
        long createdAt = System.currentTimeMillis();
        long expireAt = !expiration.isNegative() && !expiration.isZero() && expires ? createdAt + expiration.toMillis() : -1;

        Location loc = deathLocation.getBlock().getLocation();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        long start = System.currentTimeMillis();
        while (!plugin.canPlaceChestAt(loc)) {
            if (System.currentTimeMillis() - start > 1000) {
                loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
                break;
            }

            int x = random.nextInt(10) - 5;
            int z = random.nextInt(10) - 5;
            loc.add(x, 0, z);
        }

        try {
            boolean protectedChest = player.hasPermission(deathChestConfig.chestProtectionOptions().permission());
            DeathChestModel deathChest = plugin.createDeathChest(loc, createdAt, expireAt, player, protectedChest, event.getDrops().toArray(new ItemStack[0]));

            DeathChestSpawnEvent deathChestSpawnEvent = new DeathChestSpawnEvent(player, deathChest);
            Bukkit.getPluginManager().callEvent(deathChestSpawnEvent);
            // Clears the drops
            event.getDrops().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Player notification
        PlayerNotificationOptions playerNotificationOptions = deathChestConfig.playerNotificationOptions();
        if (playerNotificationOptions.enabled() && playerNotificationOptions.messages() != null) {
            if (deathLocation.getWorld() == null) {
                return; // Invalid location
            }
            StringSubstitutor substitutor = new StringSubstitutor(Map.of(
                    "x", deathLocation.getBlockX(),
                    "y", deathLocation.getBlockY(),
                    "z", deathLocation.getBlockZ(),
                    "chest_x", loc.getBlockX(),
                    "chest_y", loc.getBlockY(),
                    "chest_z", loc.getBlockZ(),
                    "world", deathLocation.getWorld().getName()));
            playerNotificationOptions.showNotification(player, substitutor);
        }

        // Global notification
        GlobalNotificationOptions globalNotificationOptions = deathChestConfig.globalNotificationOptions();
        if (globalNotificationOptions.enabled() && globalNotificationOptions.messages() != null) {
            if (deathLocation.getWorld() == null) {
                return; // Invalid location
            }
            StringSubstitutor substitutor = new StringSubstitutor(Map.of(
                    "x", deathLocation.getBlockX(),
                    "y", deathLocation.getBlockY(),
                    "z", deathLocation.getBlockZ(),
                    "chest_x", loc.getBlockX(),
                    "chest_y", loc.getBlockY(),
                    "chest_z", loc.getBlockZ(),
                    "world", deathLocation.getWorld().getName(),
                    "player_name", player.getName(),
                    "player_displayname", player.getDisplayName()));
            globalNotificationOptions.showNotification(player, substitutor);
        }


    }

}
