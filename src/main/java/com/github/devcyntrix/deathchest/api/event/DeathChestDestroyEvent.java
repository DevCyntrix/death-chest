package com.github.devcyntrix.deathchest.api.event;

import com.github.devcyntrix.deathchest.DeathChestModel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DeathChestDestroyEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final DeathChestModel deathChest;

    public DeathChestDestroyEvent(DeathChestModel deathChest) {
        this.deathChest = deathChest;
    }

    public DeathChestModel getDeathChest() {
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
