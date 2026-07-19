package com.github.devcyntrix.deathchest.api.event;

import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class DeathChestSpawnEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final DeathChestModel deathChest;

    public DeathChestSpawnEvent(Player player, DeathChestModel deathChest) {
        super(player);
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
