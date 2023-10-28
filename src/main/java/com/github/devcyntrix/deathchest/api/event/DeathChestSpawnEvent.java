package com.github.devcyntrix.deathchest.api.event;

import com.github.devcyntrix.deathchest.DeathChestModel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class DeathChestSpawnEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final DeathChestModel deathChest;

    public DeathChestSpawnEvent(Player player, DeathChestModel deathChest) {
        super(player);
        this.deathChest = deathChest;
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
