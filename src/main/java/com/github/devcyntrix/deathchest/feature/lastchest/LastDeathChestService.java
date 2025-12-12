package com.github.devcyntrix.deathchest.feature.lastchest;

import com.github.devcyntrix.deathchest.feature.chest.DeathChestModel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class LastDeathChestService {

    @Getter
    private final Map<Player, DeathChestModel> lastDeathChests = new WeakHashMap<>();

    public DeathChestModel getLastDeathChest(Player player) {
        return lastDeathChests.get(player);
    }
}
