package com.github.devcyntrix.deathchest.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
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

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
