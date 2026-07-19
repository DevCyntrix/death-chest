package com.github.devcyntrix.deathchest.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PreDeathChestSpawnEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Location location;
    private boolean protectedChest;
    private long createdAt, expireAt;
    private ItemStack[] items;

    private boolean cancelled;

    public PreDeathChestSpawnEvent(@NotNull Player who, Location location, boolean protectedChest, long createdAt, long expireAt, ItemStack... items) {
        super(who);
        this.location = location;
        this.protectedChest = protectedChest;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.items = items;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isProtectedChest() {
        return protectedChest;
    }

    public void setProtectedChest(boolean protectedChest) {
        this.protectedChest = protectedChest;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public void setItems(ItemStack[] items) {
        this.items = items;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
