package de.helixdevs.deathchest;

import com.google.common.base.Objects;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.Closeable;
import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeathChest implements Listener, Closeable {

    private final DeathChestPlugin plugin;
    private final Chest chest;
    private final Location location;
    private final Inventory inventory;
    private final long createdAt = System.currentTimeMillis();
    private final long expireAt;
    private final BukkitTask task;
    private IHologram hologram;

    public DeathChest(DeathChestPlugin plugin, Chest chest, Duration expiration, ItemStack... stacks) {
        this.plugin = plugin;
        this.chest = chest;
        this.location = chest.getLocation();

        DeathChestConfig config = plugin.getDeathChestConfig();

        // Creates inventory
        this.inventory = Bukkit.createInventory(new DeathChestHolder(chest), 9 * 5, config.getInventoryTitle());
        this.inventory.addItem(stacks);

        // Creates hologram
        this.expireAt = createdAt + expiration.toMillis();
        IHologramTextLine textLine;
        IHologramService service = plugin.getHologramService();
        if (config.isHologram() && service != null) {
            this.hologram = service.spawnHologram(location.clone().add(0.5, 1.5, 0.5));
            long duration = expireAt - System.currentTimeMillis();
            String format = DurationFormatUtils.formatDuration(duration, config.getDurationFormat());
            textLine = this.hologram.appendLine(format);
        } else {
            textLine = null;
        }

        // Runs a check and update scheduler
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                long duration = expireAt - System.currentTimeMillis();
                if (duration < 0) {
                    close();
                    return;
                }

                IAnimationService animationService = plugin.getAnimationService();
                if (animationService != null) {
                    double process = (double) (System.currentTimeMillis() - createdAt) / (expireAt - createdAt);

                    World world = location.getWorld();
                    if (world != null) {
                        Stream<Player> nearbyPlayers = world.
                                getNearbyEntities(location, 20, 20, 20, entity -> entity.getType() == EntityType.PLAYER).stream()
                                .map(entity -> (Player) entity);
                        animationService.spawnBlockBreakAnimation(location.toVector(), (byte) (9 * process), nearbyPlayers.collect(Collectors.toList()));
                    }
                }

                if (textLine != null) {
                    String format = DurationFormatUtils.formatDuration(duration, config.getDurationFormat());
                    textLine.rename(format);
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    /**
     * Closes the chest if the player was the last viewer and
     * removes the chest if the player closes an empty inventory,
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!inventory.equals(event.getInventory()))
            return;

        if (inventory.getViewers().size() - 1 <= 0) {
            DeathChestHolder holder = (DeathChestHolder) inventory.getHolder();
            if (holder == null)
                return;
            holder.getChest().close();
        }

        if (inventory.isEmpty()) {
            close();
        }
    }

    /**
     * Opens the inventory from the death chest if the player interacts with the chest and
     * opens the chest if it is the first viewer.
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler
    public void onOpenInventory(PlayerInteractEvent event) {
        if (!event.hasBlock())
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;
        if (!this.location.equals(block.getLocation()))
            return;
        if (!(block.getState() instanceof Chest chest))
            return;

        Player player = event.getPlayer();
        if (event.isBlockInHand() && player.isSneaking()) // That maintains the natural minecraft feeling
            return;
        event.setCancelled(true);
        if (this.inventory.getViewers().isEmpty()) {
            chest.open();
        }

        player.openInventory(this.inventory);
    }

    /**
     * Tries to detect an empty inventory to close the inventory and to destroy the chest.
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!inventory.equals(event.getView().getTopInventory()))
            return;
        if (inventory.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> event.getWhoClicked().closeInventory());
        }
    }

    /**
     * Tries to detect an empty inventory to close the inventory and to destroy the chest.
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!inventory.equals(event.getInventory()))
            return;
        if (inventory.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> event.getWhoClicked().closeInventory());
        }
    }

    /**
     * Cancels that a hopper can move items into the chest inventory
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler
    public void onHopperMoveItem(InventoryMoveItemEvent event) {
        if (!chest.getBlockInventory().equals(event.getDestination()))
            return;
        event.setCancelled(true);
    }

    /**
     * Drops the item from the death chest when the player breaks the chest.
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getBlock().getLocation().equals(location))
            return;
        for (ItemStack content : this.inventory.getContents()) {
            if (content == null)
                continue;
            World world = location.getWorld();
            if (world != null)
                world.dropItemNaturally(location, content);
        }
        close();
    }

    /**
     * Destroys the chest, deletes the hologram, cancels the update scheduler and unregister all events for the death chest.
     */
    @Override
    public void close() {
        if (this.inventory != null)
            Bukkit.getScheduler().runTask(this.plugin, () -> inventory.getViewers().forEach(HumanEntity::closeInventory));

        World world = this.location.getWorld();
        if (world != null) {
            Block block = this.location.getBlock();
            world.spawnParticle(Particle.BLOCK_CRACK, this.location.clone().add(0.5, 0.5, 0.5), 10, block.getBlockData());
            block.setType(Material.AIR);
        }

        if (this.hologram != null)
            this.hologram.delete();

        this.task.cancel();
        HandlerList.unregisterAll(this);
        this.plugin.unregisterChest(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeathChest that)) return false;
        return Objects.equal(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(location);
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
