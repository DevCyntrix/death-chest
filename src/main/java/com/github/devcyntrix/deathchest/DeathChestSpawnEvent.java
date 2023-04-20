package com.github.devcyntrix.deathchest;

import com.github.devcyntrix.deathchest.api.DeathChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DeathChestSpawnEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final DeathChest deathChest;

    public DeathChestSpawnEvent(Player player, DeathChest deathChest) {
        this.player = player;
        this.deathChest = deathChest;
    }

    public Player getPlayer() {
        return player;
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
