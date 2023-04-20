package com.github.devcyntrix.deathchest;

import com.github.devcyntrix.deathchest.api.DeathChest;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DeathChestDestroyEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final DeathChest deathChest;

    public DeathChestDestroyEvent(DeathChest deathChest) {
        this.deathChest = deathChest;
    }

    public DeathChest getDeathChest() {
        return deathChest;
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
