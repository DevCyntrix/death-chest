package com.github.devcyntrix.deathchest.controller;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.ChestView;
import com.github.devcyntrix.deathchest.api.audit.AuditAction;
import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.AuditManager;
import com.github.devcyntrix.deathchest.api.audit.info.CreateChestInfo;
import com.github.devcyntrix.deathchest.api.event.DeathChestDestroyEvent;
import com.github.devcyntrix.deathchest.api.storage.DeathChestStorage;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.InventoryOptions;
import com.github.devcyntrix.deathchest.config.ThiefProtectionOptions;
import com.github.devcyntrix.deathchest.util.ChestModelStringLookup;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Singleton;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

@Singleton
public class DeathChestController implements Closeable {

    private final DeathChestPlugin plugin;
    private final Logger logger;
    private final AuditManager auditManager;
    private final DeathChestStorage storage;

    private final Set<ChestView> listeners = new HashSet<>();

    protected final Table<World, Location, DeathChestModel> loadedChests = HashBasedTable.create();

    private final Function<Long, String> durationFormat;

    public DeathChestController(DeathChestPlugin plugin, Logger logger, AuditManager auditManager, DeathChestStorage storage) {
        this.plugin = plugin;
        this.logger = logger;
        this.auditManager = auditManager;
        this.storage = storage;

        this.durationFormat = (expireAt) -> {
            long duration = expireAt - System.currentTimeMillis();
            if (duration <= 0) duration = 0;
            return DurationFormatUtils.formatDuration(duration, getConfig().durationFormat());
        };
    }

    public void registerAdapter(ChestView adapter) {
        this.listeners.add(adapter);
    }

    public void loadChests() {
        Bukkit.getWorlds()
                .forEach(this::loadChests);
    }

    public void loadChests(World world) {
        this.storage.getChests(world)
                .forEach(model -> {
                    for (ChestView listener : listeners) {
                        listener.onLoad(model);
                    }
                    this.loadedChests.put(model.getWorld(), model.getLocation(), model);
                });
        logger.info(this.loadedChests.row(world).size() + " death chests loaded in world \"" + world.getName() + "\"");
        long time = System.currentTimeMillis();
        logger.info("  Of these %d have expired".formatted(this.loadedChests.row(world).values().stream().filter(model1 -> model1.getExpireAt() < time).count()));
    }

    public @NotNull DeathChestModel createChest(@NotNull Location location, long expireAt, @Nullable Player player, ItemStack @NotNull ... items) {
        boolean protectedChest = player != null && player.hasPermission(getConfig().chestOptions().thiefProtectionOptions().permission());
        return createChest(location, System.currentTimeMillis(), expireAt, player, protectedChest, items);
    }

    public @NotNull DeathChestModel createChest(@NotNull Location location, long createdAt, long expireAt, @Nullable Player player, boolean isProtected, ItemStack @NotNull ... items) {
        DeathChestModel model = new DeathChestModel(location, createdAt, expireAt, player, isProtected);
        StringSubstitutor substitution = new StringSubstitutor(new ChestModelStringLookup(plugin.getDeathChestConfig(), model, durationFormat));

        // Creates inventory
        InventoryOptions inventoryOptions = getConfig().inventoryOptions();

        model.setInventory(inventoryOptions.createInventory(model, title -> {
            title = substitution.replace(title);
            if (DeathChestPlugin.isPlaceholderAPIEnabled()) {
                title = PlaceholderAPI.setPlaceholders(player, title);
            }
            return title;
        }, items));

        for (ChestView listener : listeners) {
            listener.onCreate(model);
        }

        this.loadedChests.put(model.getWorld(), model.getLocation(), model);

        if (auditManager != null)
            auditManager.audit(new AuditItem(new Date(), AuditAction.CREATE_CHEST, new CreateChestInfo(model)));

        return model;
    }

    public @Nullable DeathChestModel getChest(@NotNull Location location) {
        return this.loadedChests.get(location.getWorld(), location);
    }

    public @NotNull Collection<DeathChestModel> getChests() {
        return this.loadedChests.values();
    }

    public @NotNull Collection<DeathChestModel> getChests(World world) {
        return this.loadedChests.row(world).values();
    }

    public boolean isAccessibleBy(@NotNull DeathChestModel model, @NotNull Player player) {
        ThiefProtectionOptions protectionOptions = plugin.getDeathChestConfig().chestOptions().thiefProtectionOptions();

        long remainingTime = 0L;
        long expiration = protectionOptions.expiration().toMillis();
        if (expiration > 0) {
            remainingTime = expiration + model.getCreatedAt() - System.currentTimeMillis();
        }

        return !protectionOptions.enabled() ||
                !model.isProtected() ||
                model.getOwner() == null ||
                model.getOwner().getUniqueId().equals(player.getUniqueId()) ||
                player.hasPermission(protectionOptions.bypassPermission()) ||
                remainingTime < 0;
    }

    public void destroyChest(DeathChestModel model) {
        // Prevent multiple invocation
        if (model.isDeleting())
            return;
        model.setDeleting(true);

        model.cancelTasks();
        for (ChestView listener : this.listeners) {
            listener.onDestroy(model);
        }

        model = this.loadedChests.remove(model.getWorld(), model.getLocation()); // Remove from cache
        if (model == null)
            throw new IllegalArgumentException("Invalid model");

        this.storage.remove(model); // Remove from database

        Bukkit.getPluginManager().callEvent(new DeathChestDestroyEvent(model));
    }


    @Override
    public void close() throws IOException {
        unloadChests();
    }

    private void unloadChests() {
        saveChests();

        this.loadedChests.values().forEach(model -> {
            this.listeners.forEach(listener -> listener.onUnload(model));
            model.cancelTasks();
        });
        this.loadedChests.clear();
    }

    public void unloadChests(World world, boolean save) {
        if (save) {
            saveChests(world);
        }

        Collection<DeathChestModel> values = this.loadedChests.row(world).values();
        for (DeathChestModel model : values) {
            this.listeners.forEach(listener -> listener.onUnload(model));
            model.cancelTasks();
        }
        values.clear();
    }

    public void saveChests() {
        this.storage.update(this.loadedChests.values());
    }

    public void saveChests(@NotNull World world) {
        Collection<DeathChestModel> values = this.loadedChests.row(world).values();
        this.storage.update(values);
    }


    public DeathChestConfig getConfig() {
        return this.plugin.getDeathChestConfig();
    }
}
