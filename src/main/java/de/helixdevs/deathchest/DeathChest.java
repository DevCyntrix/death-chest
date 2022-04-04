package de.helixdevs.deathchest;

import com.google.common.base.Objects;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import de.helixdevs.deathchest.config.DeathChestConfig;
import de.helixdevs.deathchest.config.HologramOptions;
import de.helixdevs.deathchest.config.InventoryOptions;
import de.helixdevs.deathchest.config.ParticleOptions;
import lombok.Getter;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
public class DeathChest implements Listener, Closeable {

    private final DeathChestPlugin plugin;
    private final Chest chest;
    private final Location location;
    private final Inventory inventory;
    private final long createdAt = System.currentTimeMillis();
    private final long expireAt;
    private final Supplier<String> durationSupplier;

    private final BukkitTask task;
    private BukkitTask particleTask;

    private IHologram hologram;
    private StrSubstitutor substitutor;

    public DeathChest(@NotNull DeathChestPlugin plugin, @NotNull Chest chest, @Nullable Duration expiration, @NotNull OfflinePlayer player, ItemStack... stacks) {
        this.plugin = plugin;
        this.chest = chest;
        this.location = chest.getLocation();

        if (expiration != null && !expiration.isNegative() && !expiration.isZero())
            this.expireAt = createdAt + expiration.toMillis();
        else
            this.expireAt = -1; // Permanent

        DeathChestConfig config = plugin.getDeathChestConfig();

        this.durationSupplier = () -> {
            if (!isExpiring())
                return DurationFormatUtils.formatDuration(0, config.durationFormat());
            long duration = expireAt - System.currentTimeMillis();
            return DurationFormatUtils.formatDuration(duration, config.durationFormat());
        };


        // Creates inventory
        InventoryOptions inventoryOptions = config.inventoryOptions();
        this.inventory = Bukkit.createInventory(new DeathChestHolder(chest), inventoryOptions.size().getSize(stacks.length), inventoryOptions.title());
        this.inventory.addItem(stacks);

        // Creates hologram
        Map<String, IHologramTextLine> map = new LinkedHashMap<>();
        IHologramService service = plugin.getHologramService();

        HologramOptions hologramOptions = config.hologramOptions();
        if (service != null && hologramOptions != null && hologramOptions.enabled()) {
            this.hologram = service.spawnHologram(location.clone().add(0.5, hologramOptions.height(), 0.5));

            substitutor = new StrSubstitutor(new PlayerStrLookup(player, durationSupplier));
            hologramOptions.lines()
                    .forEach(line -> map.put(line, hologram.appendLine(substitutor.replace(line))));
        }

        // Runs a check and update scheduler
        this.task = new BukkitRunnable() {

            @Override
            public void run() {
                // Stops the scheduler when the chest expired
                if (isExpiring()) {
                    long duration = expireAt - System.currentTimeMillis();
                    if (duration < 0) {
                        close();
                        return;
                    }
                }

                IAnimationService animationService = plugin.getAnimationService();
                World world;
                // Spawns the block break animation
                if (animationService != null && isExpiring()) {
                    double process = (double) (System.currentTimeMillis() - createdAt) / (expireAt - createdAt);

                    world = location.getWorld();
                    if (world != null) {
                        try {
                            Stream<Player> playerStream = Bukkit.getScheduler().callSyncMethod(plugin, () -> world.
                                    getNearbyEntities(location, 20, 20, 20, entity -> entity.getType() == EntityType.PLAYER).stream()
                                    .map(entity -> (Player) entity)).get(1, TimeUnit.SECONDS);
                            animationService.spawnBlockBreakAnimation(location.toVector(), (byte) (9 * process), playerStream);
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            plugin.getLogger().info("Warning get nearby entities takes longer than 1 second.");
                        }
                    }
                }

                // Updates the hologram lines
                if (!map.isEmpty()) {
                    map.forEach((s, line) -> {
                        if (substitutor != null)
                            s = substitutor.replace(s);
                        line.rename(s);
                    });
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20, 20);
        World world = location.getWorld();

        ParticleOptions particleOptions = config.particleOptions();
        if (particleOptions != null && particleOptions.enabled() && world != null) {
            this.particleTask = new ParticleScheduler(location, particleOptions.count(), particleOptions.radius(), particleLocation -> {
                Particle.DustOptions options = new Particle.DustOptions(Color.ORANGE, 0.75f);
                world.spawnParticle(
                        Particle.REDSTONE,
                        particleLocation.clone().add(0.5, 0.5, 0.5), // Centred particle location
                        1,
                        options);
                Particle.DustOptions options1 = new Particle.DustOptions(Color.AQUA, 0.75f);
                world.spawnParticle(Particle.REDSTONE, particleLocation.clone().add(0.5, 0.4, 0.5), 1, options1);
            }).runTaskTimerAsynchronously(this.plugin, 0, (long) (20 / particleOptions.speed()));
        }
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
    @EventHandler(ignoreCancelled = true)
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
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
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
     * Drops the items when the chest was destroyed by a block explosion
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.blockList().stream().noneMatch(block -> block.getLocation().equals(location)))
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
     * Drops the items when the chest was destroyed by an entity explosion
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockExplode(EntityExplodeEvent event) {
        if (event.blockList().stream().noneMatch(block -> block.getLocation().equals(location)))
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

        if (this.particleTask != null)
            this.particleTask.cancel();

        HandlerList.unregisterAll(this);
        this.plugin.removeChest(this);
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

    public boolean isExpiring() {
        return this.expireAt > 0;
    }
}
