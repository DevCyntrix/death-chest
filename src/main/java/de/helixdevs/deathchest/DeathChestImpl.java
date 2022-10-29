package de.helixdevs.deathchest;

import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologram;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.hologram.IHologramTextLine;
import de.helixdevs.deathchest.config.*;
import de.helixdevs.deathchest.util.ParticleScheduler;
import de.helixdevs.deathchest.util.PlayerStringLookup;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Lidded;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
public class DeathChestImpl implements DeathChest {

    private final DeathChestPlugin plugin;
    private final Location location;

    private final BlockState state;

    private final Inventory inventory;
    private final long createdAt;
    private final long expireAt;
    @Nullable
    private final OfflinePlayer player;
    private final Supplier<String> durationSupplier;

    private final List<BukkitTask> tasks = new LinkedList<>();

    private IHologram hologram;
    private StringSubstitutor substitutor;

    private boolean closed;

    public DeathChestImpl(@NotNull Location location, @NotNull DeathChestBuilder builder) {
        this.location = location;
        if (location.getWorld() == null)
            throw new IllegalArgumentException("The world cannot be null");

        this.plugin = JavaPlugin.getPlugin(DeathChestPlugin.class);

        Block block = location.getBlock();
        block.setType(Material.CHEST);

        this.state = block.getState();

        this.createdAt = builder.createdAt();
        this.expireAt = builder.expireAt();
        this.player = builder.player();
        this.durationSupplier = () -> {
            if (!isExpiring())
                return DurationFormatUtils.formatDuration(0, builder.durationFormat());
            long duration = expireAt - System.currentTimeMillis();
            if (duration <= 0)
                duration = 0;
            return DurationFormatUtils.formatDuration(duration, builder.durationFormat());
        };

        ItemStack[] stacks = builder.items();

        // Creates inventory
        InventoryOptions inventoryOptions = builder.inventoryOptions();
        this.inventory = Bukkit.createInventory(new DeathChestHolder(this), inventoryOptions.size().getSize(stacks.length), inventoryOptions.title());
        this.inventory.setContents(stacks);

        // Creates hologram
        IHologramService hologramService = plugin.getHologramService();
        HologramOptions hologramOptions = builder.hologramOptions();
        if (hologramService != null && hologramOptions != null && hologramOptions.enabled()) {
            this.hologram = hologramService.spawnHologram(getLocation().clone().add(0.5, hologramOptions.height(), 0.5));

            Map<String, IHologramTextLine> blueprints = new LinkedHashMap<>(hologramOptions.lines().size());
            substitutor = new StringSubstitutor(new PlayerStringLookup(builder.player(), durationSupplier));
            hologramOptions.lines()
                    .forEach(line -> blueprints.put(line, hologram.appendLine(substitutor.replace(line)))); // A map of blueprints

            // Start task
            if (!blueprints.isEmpty()) {
                this.tasks.add(runHologramUpdateTask(blueprints));
            }
        }

        IAnimationService animationService = plugin.getAnimationService();
        BreakEffectOptions breakEffectOptions = builder.breakEffectOptions();
        // Spawns the block break animation
        if (animationService != null && isExpiring() && breakEffectOptions.enabled()) {
            tasks.add(runAnimationTask(animationService, breakEffectOptions));
        }

        ParticleOptions particleOptions = builder.particleOptions();
        if (particleOptions != null && particleOptions.enabled()) {
            this.tasks.add(runParticleTask(particleOptions));
        }

        if (isExpiring()) {
            tasks.add(runExpirationTask(animationService));
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private BukkitTask runHologramUpdateTask(Map<String, IHologramTextLine> blueprints) {
        return new BukkitRunnable() {

            @Override
            public void run() {
                // Updates the hologram lines
                blueprints.forEach((s, line) -> {
                    if (substitutor != null)
                        s = substitutor.replace(s);
                    if (plugin.isPlaceholderAPIEnabled())
                        s = PlaceholderAPI.setPlaceholders(player, s);
                    line.rename(s);
                });
            }
        }.runTaskTimerAsynchronously(plugin, 20, 20);
    }

    private BukkitTask runParticleTask(ParticleOptions particleOptions) {
        return new ParticleScheduler(getLocation(), particleOptions.count(), particleOptions.radius(), particleLocation -> {
            Particle.DustOptions options = new Particle.DustOptions(Color.ORANGE, 0.75f);
            getWorld().spawnParticle(
                    Particle.REDSTONE,
                    particleLocation.clone().add(0.5, 0.5, 0.5), // Centred particle location
                    1,
                    options);
            Particle.DustOptions options1 = new Particle.DustOptions(Color.AQUA, 0.75f);
            getWorld().spawnParticle(Particle.REDSTONE, particleLocation.clone().add(0.5, 0.4, 0.5), 1, options1);
        }).runTaskTimerAsynchronously(this.plugin, 0, (long) (20 / particleOptions.speed()));
    }

    private BukkitTask runAnimationTask(@NotNull IAnimationService animationService, BreakEffectOptions breakEffectOptions) {
        return new BukkitRunnable() {

            @Override
            public void run() {
                double process = (double) (System.currentTimeMillis() - createdAt) / (expireAt - createdAt);

                try {
                    Stream<Player> playerStream = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                            getWorld().
                                    getNearbyEntities(
                                            getLocation(),
                                            breakEffectOptions.viewDistance(),
                                            breakEffectOptions.viewDistance(),
                                            breakEffectOptions.viewDistance(),
                                            entity -> entity.getType() == EntityType.PLAYER).stream()
                                    .map(entity -> (Player) entity)).get(1, TimeUnit.SECONDS);
                    animationService.spawnBlockBreakAnimation(getLocation().toVector(), (byte) (9 * process), playerStream);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    plugin.getLogger().info("Warning get nearby entities takes longer than 1 second.");
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20, 20);
    }

    private BukkitTask runExpirationTask(@Nullable IAnimationService animationService) {
        return new BukkitRunnable() {

            @Override
            public void run() {
                // Stops the scheduler when the chest expired
                if (isExpiring()) {
                    long duration = expireAt - System.currentTimeMillis();
                    if (duration < 0) {
                        Bukkit.getScheduler().runTask(plugin, () -> close());

                        // Reset animation
                        if (animationService != null) {
                            try {
                                Stream<Player> playerStream = Bukkit.getScheduler().callSyncMethod(plugin, () ->
                                        getWorld().
                                                getNearbyEntities(getLocation(), 20, 20, 20, entity -> entity.getType() == EntityType.PLAYER).stream()
                                                .map(entity -> (Player) entity)).get(1, TimeUnit.SECONDS);
                                animationService.spawnBlockBreakAnimation(getLocation().toVector(), (byte) 0, playerStream);
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            } catch (TimeoutException e) {
                                plugin.getLogger().info("Warning get nearby entities takes longer than 1 second.");
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20, 20);
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

        DeathChestHolder holder = (DeathChestHolder) inventory.getHolder();
        if (holder == null)
            return;
        DeathChest chest = holder.getChest();
        BlockState state = chest.getState();

        HumanEntity human = event.getPlayer();

        if (state instanceof Lidded lidded && human.getGameMode() != GameMode.SPECTATOR) {
            lidded.close();
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
        if (!this.getLocation().equals(block.getLocation()))
            return;

        Player player = event.getPlayer();
        if (event.isBlockInHand() && player.isSneaking()) // That maintains the natural minecraft feeling
            return;
        event.setCancelled(true);

        // Chest Protection (Vault is required)
        Permission permission = getPlugin().getPermission();
        ChestProtectionOptions protectionOptions = getPlugin().getDeathChestConfig().chestProtectionOptions();
        if (protectionOptions.enabled() &&
                getPlugin().getPermission() != null &&
                getPlayer() != null &&
                player != getPlayer() &&
                permission.playerHas(getWorld().getName(), getPlayer(), protectionOptions.permission()) &&
                !permission.playerHas(getWorld().getName(), player, protectionOptions.bypassPermission())) {
            protectionOptions.playSound(player, block.getLocation());
            protectionOptions.sendMessage(player);
            return;
        }

        if (block.getState() instanceof Lidded lidded && player.getGameMode() != GameMode.SPECTATOR) {
            lidded.open();
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
            event.getWhoClicked().closeInventory();
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
            event.getWhoClicked().closeInventory();
        }
    }

    /**
     * Cancels that a hopper can move items into the chest inventory
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler(ignoreCancelled = true)
    public void onHopperMoveItem(InventoryMoveItemEvent event) {
        if (!(state instanceof Container))
            return;
        Container container = (Container) this.state;

        if (!container.getInventory().equals(event.getDestination()))
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
        if (!event.getBlock().getLocation().equals(getLocation()))
            return;
        for (ItemStack content : this.inventory.getContents()) {
            if (content == null)
                continue;
            getWorld().dropItemNaturally(getLocation(), content);
        }
        event.setCancelled(true);
        close();
    }

    /**
     * Drops the items when the chest was destroyed by a block explosion
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (plugin.getDeathChestConfig().blastProtection())
            return;
        if (!event.blockList().removeIf(block -> block.getLocation().equals(getLocation())))
            return;

        for (ItemStack content : this.inventory.getContents()) {
            if (content == null)
                continue;
            getWorld().dropItemNaturally(getLocation(), content);
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
        if (plugin.getDeathChestConfig().blastProtection())
            return;
        if (!event.blockList().removeIf(block -> block.getLocation().equals(getLocation())))
            return;

        for (ItemStack content : this.inventory.getContents()) {
            if (content == null)
                continue;
            getWorld().dropItemNaturally(getLocation(), content);
        }
        close();
    }

    /**
     * Destroys the chest, deletes the hologram, cancels the update scheduler and unregister all events for the death chest.
     */
    @Override
    public void close() {
        if (closed)
            return;
        closed = true;
        if (this.inventory != null) {
            LinkedList<HumanEntity> humanEntities = new LinkedList<>(inventory.getViewers()); // Copies the list to avoid a concurrent modification exception
            humanEntities.forEach(HumanEntity::closeInventory);
        }
        Block block = getLocation().getBlock();
        block.setType(Material.AIR);
        getWorld().spawnParticle(Particle.BLOCK_CRACK, getLocation().clone().add(0.5, 0.5, 0.5), 10, block.getBlockData());

        if (this.hologram != null) {
            this.hologram.delete();
            this.hologram = null;
        }

        this.tasks.forEach(BukkitTask::cancel);
        this.tasks.clear();

        HandlerList.unregisterAll(this);
        this.plugin.deathChests.remove(this);
    }

    @Override
    public @NotNull BlockState getState() {
        return this.state;
    }

    @Override
    public @Nullable OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public long getExpireAt() {
        return expireAt;
    }

    @Override
    public boolean isExpiring() {
        return this.expireAt > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeathChestImpl that)) return false;
        return Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}
