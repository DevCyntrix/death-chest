package com.github.devcyntrix.deathchest;

import com.github.devcyntrix.deathchest.api.DeathChest;
import com.github.devcyntrix.deathchest.api.DeathChestSnapshot;
import com.github.devcyntrix.deathchest.api.animation.IAnimationService;
import com.github.devcyntrix.deathchest.api.audit.AuditAction;
import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyChestInfo;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyReason;
import com.github.devcyntrix.deathchest.api.hologram.IHologram;
import com.github.devcyntrix.deathchest.api.hologram.IHologramService;
import com.github.devcyntrix.deathchest.api.hologram.IHologramTextLine;
import com.github.devcyntrix.deathchest.config.*;
import com.github.devcyntrix.deathchest.tasks.AnimationRunnable;
import com.github.devcyntrix.deathchest.tasks.ExpirationRunnable;
import com.github.devcyntrix.deathchest.tasks.HologramRunnable;
import com.github.devcyntrix.deathchest.tasks.ParticleRunnable;
import com.github.devcyntrix.deathchest.util.EntityIdHelper;
import com.github.devcyntrix.deathchest.util.PlayerStringLookup;
import com.google.common.base.Preconditions;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lidded;
import org.bukkit.entity.Entity;
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
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This is an implementation of the {@link DeathChest} interface.
 */
@Getter
public class DeathChestImpl implements DeathChest {

    private final DeathChestPlugin plugin;

    @Expose
    private final Location location;

    private final BlockState previousState;
    private BlockState state;

    @Expose(deserialize = false)
    @NotNull
    private final Inventory inventory;
    @Expose
    private final long createdAt;
    @Expose
    private final long expireAt;
    @Expose
    @Nullable
    private final OfflinePlayer player;
    private final Supplier<String> durationSupplier;

    private final List<BukkitTask> tasks = new ArrayList<>();

    private IHologram hologram;

    private boolean closed;

    private int breakingEntityId;
    private final boolean isProtected;

    public DeathChestImpl(DeathChestSnapshot snapshot) {
        this(snapshot.getLocation(), DeathChestBuilder.builder().setCreatedAt(snapshot.getCreatedAt()).setExpireAt(snapshot.getExpireAt()).setItems(snapshot.getItems()).setPlayer(snapshot.getOwner()));
    }

    public DeathChestImpl(@NotNull Location location, @NotNull DeathChestBuilder builder) {
        this.location = location;
        if (!location.isWorldLoaded()) throw new IllegalArgumentException("The world cannot be null");

        this.plugin = JavaPlugin.getPlugin(DeathChestPlugin.class);
        this.previousState = location.getBlock().getState();

        // Creates the chest in the next tick because if you try to sleep in the nether the explosion spawns after the player dies. That means the chest would be destroyed by the explosion.
        Bukkit.getScheduler().runTask(plugin, () -> {
            Block block = location.getBlock();
            block.setType(Material.CHEST);
            this.state = block.getState();
        });

        this.createdAt = builder.createdAt();
        this.expireAt = builder.expireAt();
        this.player = builder.player();
        this.isProtected = builder.isProtected();
        this.durationSupplier = () -> {
            if (!isExpiring()) return DurationFormatUtils.formatDuration(0, builder.durationFormat());
            long duration = expireAt - System.currentTimeMillis();
            if (duration <= 0) duration = 0;
            return DurationFormatUtils.formatDuration(duration, builder.durationFormat());
        };

        ItemStack[] stacks = builder.items();

        try {
            // Creates inventory
            InventoryOptions inventoryOptions = builder.inventoryOptions();
            this.inventory = Bukkit.createInventory(new DeathChestHolder(this), inventoryOptions.size().getSize(stacks.length), inventoryOptions.title());
            this.inventory.setContents(stacks);

            // Creates hologram
            IHologramService hologramService = builder.hologramService();
            HologramOptions hologramOptions = builder.hologramOptions();
            if (hologramService != null && hologramOptions != null && hologramOptions.enabled()) {
                this.hologram = hologramService.spawnHologram(getLocation().clone().add(0.5, hologramOptions.height(), 0.5));

                Map<String, IHologramTextLine> blueprints = new LinkedHashMap<>(hologramOptions.lines().size());
                StringSubstitutor substitutor = new StringSubstitutor(new PlayerStringLookup(builder.player(), durationSupplier));
                hologramOptions.lines().forEach(line -> blueprints.put(line, hologram.appendLine(substitutor.replace(line)))); // A map of blueprints

                // Start task
                if (!blueprints.isEmpty()) {
                    this.tasks.add(new HologramRunnable(this, blueprints, substitutor).runTaskTimerAsynchronously(plugin, 20, 20));
                }
            }


            IAnimationService animationService = builder.animationService();
            BreakEffectOptions breakEffectOptions = builder.breakEffectOptions();
            // Spawns the block break animation
            if (animationService != null && isExpiring() && breakEffectOptions.enabled()) {
                this.breakingEntityId = EntityIdHelper.increaseAndGet();
                tasks.add(new AnimationRunnable(this, animationService, breakEffectOptions, breakingEntityId).runTaskTimerAsynchronously(plugin, 20, 20));
            }

            ParticleOptions particleOptions = builder.particleOptions();
            if (particleOptions != null && particleOptions.enabled()) {
                this.tasks.add(runParticleTask(particleOptions));
            }

            if (isExpiring() && !isClosed()) {
                this.tasks.add(new ExpirationRunnable(plugin.getAuditManager(), this).runTaskLater(plugin, (getExpireAt() - System.currentTimeMillis()) / 50));
            }
            Bukkit.getPluginManager().registerEvents(this, plugin);
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    private BukkitTask runParticleTask(ParticleOptions particleOptions) {
        Particle.DustOptions orangeDustOptions = new Particle.DustOptions(Color.ORANGE, 0.75f);
        Particle.DustOptions aquaDustOptions = new Particle.DustOptions(Color.AQUA, 0.75f);

        return new ParticleRunnable(getLocation(), particleOptions.count(), particleOptions.radius(), particleLocation -> {
            // Orange dust
            Location orangeDust = particleLocation.clone().add(0.5, 0.5, 0.5); // Center the particle location
            Bukkit.getScheduler().runTask(plugin, () -> getWorld().spawnParticle(Particle.REDSTONE, orangeDust, 1, orangeDustOptions));

            // Aqua dust
            Location aquaDust = orangeDust.clone().subtract(0, 0.1, 0);
            Bukkit.getScheduler().runTask(plugin, () -> getWorld().spawnParticle(Particle.REDSTONE, aquaDust, 1, aquaDustOptions));

        }).runTaskTimerAsynchronously(this.plugin, 0, (long) (20 / particleOptions.speed()));
    }


    /**
     * Closes the chest if the player was the last viewer and
     * removes the chest if the player closes an empty inventory,
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!inventory.equals(event.getInventory())) return;

        DeathChestHolder holder = (DeathChestHolder) inventory.getHolder();
        if (holder == null) return;
        DeathChest chest = holder.getChest();
        BlockState state = chest.getState();

        HumanEntity human = event.getPlayer();

        if (state instanceof Lidded lidded && human.getGameMode() != GameMode.SPECTATOR) {
            lidded.close();
        }

        if (inventory.isEmpty()) {
            DestroyReason reason = DestroyReason.PLAYER;
            if (!human.equals(player))
                reason = DestroyReason.THIEF;

            this.plugin.getAuditManager().audit(new AuditItem(new Date(), AuditAction.DESTROY_CHEST, new DestroyChestInfo(this, reason, Map.of("player", human))));
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
        if (!event.hasBlock()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!this.getLocation().equals(block.getLocation())) return;

        Player player = event.getPlayer();
        if (event.isBlockInHand() && player.isSneaking()) // That maintains the natural minecraft feeling
            return;
        event.setCancelled(true);

        // Chest Protection
        ChestProtectionOptions protectionOptions = getPlugin().getDeathChestConfig().chestProtectionOptions();
        Long expiration = protectionOptions.expiration() == null ? null : protectionOptions.expiration().toMillis() + createdAt - System.currentTimeMillis();

        if (protectionOptions.enabled() &&
                getPlayer() != null && player != getPlayer() &&
                isProtected() &&
                !player.hasPermission(protectionOptions.bypassPermission()) &&
                (expiration == null || expiration >= 0)) {
            protectionOptions.playSound(player, block.getLocation());
            protectionOptions.notify(player);
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
        if (!inventory.equals(event.getView().getTopInventory())) return;
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
        if (!inventory.equals(event.getInventory())) return;
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

        var inv = event.getDestination();
        if (inv.getType() != InventoryType.CHEST) return;

        var holder = inv.getHolder();
        if (holder == null) return;

        if (holder instanceof BlockInventoryHolder bh && bh.getBlock().getLocation().equals(location)) {
            event.setCancelled(true);
        }
    }

    /**
     * Drops the item from the death chest when the player breaks the chest.
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.getLocation().equals(getLocation())) return;
        Player player = event.getPlayer();

        event.setCancelled(true);
        // Chest Protection (Vault is required)
        ChestProtectionOptions protectionOptions = getPlugin().getDeathChestConfig().chestProtectionOptions();
        Long expiration = protectionOptions.expiration() == null ? null : protectionOptions.expiration().toMillis() + createdAt - System.currentTimeMillis();
        if (protectionOptions.enabled() &&
                isProtected() &&
                getPlayer() != null &&
                player != getPlayer() &&
                !player.hasPermission(protectionOptions.bypassPermission()) &&
                (expiration == null || expiration >= 0)) {
            protectionOptions.playSound(player, block.getLocation());
            protectionOptions.notify(player);
            return;
        }

        for (ItemStack content : this.inventory.getContents()) {
            if (content == null) continue;
            getWorld().dropItemNaturally(getLocation(), content);
        }

        this.plugin.getAuditManager().audit(new AuditItem(new Date(), AuditAction.DESTROY_CHEST, new DestroyChestInfo(this, DestroyReason.BREAK, Map.of("player", player))));
        close();
    }

    /**
     * Drops the items when the chest was destroyed by a block explosion
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.blockList().stream().noneMatch(block -> block.getLocation().equals(location))) return;
        if (plugin.getDeathChestConfig().blastProtection()) {
            event.blockList().removeIf(block -> block.getLocation().equals(getLocation()));
            return;
        }
        for (ItemStack content : this.inventory.getContents()) {
            if (content == null) continue;
            getWorld().dropItemNaturally(getLocation(), content);
        }
        Block block = event.getBlock();
        this.plugin.getAuditManager().audit(new AuditItem(new Date(), AuditAction.DESTROY_CHEST, new DestroyChestInfo(this, DestroyReason.BLOCK_EXPLOSION, Map.of("block", block))));
        close();
    }

    /**
     * Drops the items when the chest was destroyed by an entity explosion
     *
     * @param event the event from the Bukkit API
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockExplode(EntityExplodeEvent event) {
        if (event.blockList().stream().noneMatch(block -> block.getLocation().equals(location))) return;
        if (plugin.getDeathChestConfig().blastProtection()) {
            event.blockList().removeIf(block -> block.getLocation().equals(getLocation()));
            return;
        }
        for (ItemStack content : this.inventory.getContents()) {
            if (content == null) continue;
            getWorld().dropItemNaturally(getLocation(), content);
        }

        Entity entity = event.getEntity();

        this.plugin.getAuditManager().audit(new AuditItem(new Date(), AuditAction.DESTROY_CHEST, new DestroyChestInfo(this, DestroyReason.ENTITY_EXPLOSION, Map.of("entity", entity))));
        close();
    }

    /**
     * Destroys the chest, deletes the hologram, cancels the update scheduler and unregister all events for the death chest.
     */
    @Override
    public void close() {
        if (closed) return;
        closed = true;

        try {
            if (inventory != null) {
                List<HumanEntity> humanEntities = new ArrayList<>(inventory.getViewers()); // Copies the list to avoid a concurrent modification exception
                humanEntities.forEach(HumanEntity::closeInventory);
            }
        } catch (Exception e) {
            System.err.println("Failed to close inventories of viewers.");
            e.printStackTrace();
        }

        if (this.location != null) {
            Block block = getLocation().getBlock();
            getWorld().spawnParticle(Particle.BLOCK_CRACK, getLocation().clone().add(0.5, 0.5, 0.5), 10, block.getBlockData());
        }

        try {
            this.previousState.update(true, false);
        } catch (Exception e) {
            System.err.println("Failed to replace the block with the previous block.");
            e.printStackTrace();
        }

        try {
            if (this.hologram != null) {
                this.hologram.delete();
                this.hologram = null;
            }
        } catch (Exception e) {
            System.err.println("Failed to delete the hologram");
            e.printStackTrace();
        }

        try {
            this.tasks.forEach(BukkitTask::cancel);
            this.tasks.clear();
        } catch (Exception e) {
            System.err.println("Failed to cancel the bukkit tasks");
            e.printStackTrace();
        }

        try {
            // Resets the breaking animation if the service is available
            IAnimationService animationService = plugin.getAnimationService();
            if (animationService != null && isExpiring()) {
                Stream<Player> playerStream = getWorld().getNearbyEntities(getLocation(), 20, 20, 20, entity -> entity.getType() == EntityType.PLAYER).stream().map(entity -> (Player) entity);
                animationService.spawnBlockBreakAnimation(breakingEntityId, getLocation().toVector(), -1, playerStream);
            }
        } catch (Exception e) {
            System.err.println("Failed to reset the block animation of all players in the area");
            e.printStackTrace();
        }

        try {
            HandlerList.unregisterAll(this);
        } catch (Exception e) {
            System.err.println("Failed to unregister the listener of the death chest.");
            e.printStackTrace();
        }
        this.plugin.deathChests.remove(this);
    }

    @Override
    public void dropItems(@NotNull Location location) {
        Preconditions.checkNotNull(location.getWorld(), "invalid location");
        for (ItemStack itemStack : getInventory()) {
            if (itemStack == null) continue;
            getWorld().dropItemNaturally(location, itemStack); // World won't be null
        }
    }

    @Override
    public @NotNull DeathChestConfig getConfig() {
        return plugin.getDeathChestConfig();
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
    public boolean isProtected() {
        return isProtected;
    }

    @Override
    public DeathChestSnapshot createSnapshot() {
        return new DeathChestSnapshotImpl(this);
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
