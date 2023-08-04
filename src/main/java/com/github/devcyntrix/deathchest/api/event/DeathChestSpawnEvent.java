package com.github.devcyntrix.deathchest.api.event;

import com.github.devcyntrix.deathchest.DeathChestModel;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DeathChestSpawnEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final DeathChestModel deathChest;

    public DeathChestSpawnEvent(Player player, DeathChestModel deathChest) {
        this.player = player;
        this.deathChest = deathChest;
    }

    public Player getPlayer() {
        return player;
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
