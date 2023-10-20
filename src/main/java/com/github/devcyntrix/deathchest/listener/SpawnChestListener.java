package com.github.devcyntrix.deathchest.listener;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.event.DeathChestSpawnEvent;
import com.github.devcyntrix.deathchest.config.ChangeDeathMessageOptions;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.NoExpirationPermission;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.*;
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

    private final Set<Player> set = Collections.newSetFromMap(new WeakHashMap<>());

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

        plugin.debug(0, "Spawning death chest...");

        Player player = event.getEntity();
        plugin.debug(1, "Checking multiple deaths at once...");
        if (set.contains(player)) {
            event.getDrops().clear();
            return;
        }
        set.add(player);

        DeathChestConfig config = plugin.getDeathChestConfig();
        Location location = player.getLocation();
        World world = player.getWorld();

        plugin.debug(1, "Checking keep inventory...");
        if (event.getKeepInventory())
            return;

        plugin.debug(1, "Removing air and blacklisted items...");
        boolean removed = event.getDrops()
                .removeIf(itemStack -> itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() <= 0 || !plugin.getBlacklist().isValidItem(itemStack)); // Prevent spawning an empty chest
        if (removed) {
            plugin.debug(2, "Inventory has been updated.");
        }
        ItemStack[] items = event.getDrops().stream()
                .filter(Objects::nonNull)
                .filter(stack -> plugin.getBlacklist().isValidItem(stack))
                .toArray(ItemStack[]::new);

        if (items.length == 0) {
            plugin.debug(1, "Clearing drops because the inventory was empty after removing blacklisted items");
            event.getDrops().clear();
            return;
        }

        plugin.debug(1, "Checking world filter...");
        if (!config.worldFilterConfig().test(player.getWorld()))
            return;

        Location deathLocation = new Location(
                player.getWorld(),
                location.getX(),
                Math.round(location.getY()),
                location.getZ()
        );

        plugin.debug(1, "Checking world height limitations...");
        int highestBlockYAt = world.getHighestBlockYAt(deathLocation, HeightMap.WORLD_SURFACE);
        // Check Minecraft limitation of block positions
        if (deathLocation.getBlockY() <= world.getMinHeight()) { // Min build height
            deathLocation.setY(Math.max(0, highestBlockYAt));
        }
        if (deathLocation.getBlockY() >= world.getMaxHeight()) { // Max build height
            deathLocation.setY(highestBlockYAt);
            if (deathLocation.getBlockY() >= world.getMaxHeight()) {
                deathLocation.setY(world.getSeaLevel());
            }
        }

        plugin.debug(1, "Checking protection service...");
        boolean build = plugin.getProtectionService().canBuild(player, deathLocation, Material.CHEST);
        if (!build)
            return;

        plugin.debug(1, "Getting expiration time...");
        Duration expiration = config.expiration();
        if (expiration == null)
            expiration = Duration.ofSeconds(-1);

        plugin.debug(1, "Checking no expiration permission...");
        NoExpirationPermission permission = config.noExpirationPermission();
        boolean expires = permission == null || !permission.enabled() || !player.hasPermission(permission.permission());
        long createdAt = System.currentTimeMillis();
        long expireAt = !expiration.isNegative() && !expiration.isZero() && expires ? createdAt + expiration.toMillis() : -1;
        if (expireAt <= 0) {
            plugin.debug(1, "The chest will never expire");
        } else {
            plugin.debug(1, "The chest will expire at " + new Date(expireAt));
        }

        Location loc = deathLocation.getBlock().getLocation();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        long start = System.currentTimeMillis();
        while (!plugin.canPlaceChestAt(loc)) {
            if (System.currentTimeMillis() - start > 1000) {
                loc.setY(world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
                plugin.debug(1, "Finding a valid location took longer than 1 second.");
                break;
            }

            int x = random.nextInt(10) - 5;
            int z = random.nextInt(10) - 5;
            loc.add(x, 0, z);
        }

        ChangeDeathMessageOptions changeDeathMessageOptions = config.changeDeathMessageOptions();
        if (changeDeathMessageOptions.enabled()) {
            plugin.debug(1, "Changing death message...");
            if (changeDeathMessageOptions.message() != null && location.getWorld() != null) {
                StringSubstitutor substitutor = new StringSubstitutor(Map.of("x", location.getBlockX(), "y", location.getBlockY(), "z", location.getBlockZ(), "world", location.getWorld().getName(), "player_name", player.getName(), "player_display_name", player.getDisplayName()));

                event.setDeathMessage(Arrays.stream(changeDeathMessageOptions.message()).map(substitutor::replace).map(s -> {
                    if (DeathChestPlugin.isPlaceholderAPIEnabled()) {
                        return PlaceholderAPI.setPlaceholders(player, s);
                    }
                    return s;
                }).collect(Collectors.joining("\n")));
            } else {
                event.setDeathMessage(null); // Disable death message
            }
        }

        try {
            boolean protectedChest = config.chestProtectionOptions().enabled() && player.hasPermission(config.chestProtectionOptions().permission());
            plugin.debug(1, "Protected chest: %s".formatted(protectedChest));

            DeathChestModel deathChest = plugin.createDeathChest(loc, createdAt, expireAt, player, protectedChest, items);

            DeathChestSpawnEvent deathChestSpawnEvent = new DeathChestSpawnEvent(player, deathChest);
            Bukkit.getPluginManager().callEvent(deathChestSpawnEvent);

            // Clears the drops
            plugin.debug(1, "Clearing drops...");
            event.getDrops().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
