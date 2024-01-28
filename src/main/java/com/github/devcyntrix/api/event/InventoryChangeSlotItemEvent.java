package com.github.devcyntrix.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InventoryChangeSlotItemEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final HumanEntity entity;

    @Getter
    private final Inventory inventory;

    @Getter
    private final int slot;

    @Setter
    @Getter
    private ItemStack from, to;

    private boolean cancelled;

    public InventoryChangeSlotItemEvent(HumanEntity entity, Inventory inventory, int slot, ItemStack from, ItemStack to) {
        this.entity = entity;
        this.inventory = inventory;
        this.slot = slot;
        this.from = from;
        this.to = to;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public String toString() {
        return "InventoryChangeSlotItemEvent{" +
                "entity=" + entity +
                ", inventory=" + inventory +
                ", slot=" + slot +
                ", from=" + from +
                ", to=" + to +
                ", cancelled=" + cancelled +
                '}';
    }
}
