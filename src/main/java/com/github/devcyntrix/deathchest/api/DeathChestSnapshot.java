package com.github.devcyntrix.deathchest.api;

import com.github.devcyntrix.deathchest.DeathChestModel;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface DeathChestSnapshot extends ConfigurationSerializable {

    @NotNull Location getLocation();

    @NotNull ItemStack @NotNull [] getItems();

    @Nullable Player getOwner();

    long getCreatedAt();

    long getExpireAt();

    boolean isProtected();

    default DeathChestModel createChest(DeathChestService service) {
        return service.createDeathChest(getLocation(), getCreatedAt(), getExpireAt(), getOwner(), isProtected(), getItems());
    }

    @NotNull
    @Override
    default Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("location", getLocation());
        map.put("createdAt", getCreatedAt());
        map.put("expireAt", getExpireAt());
        if (getOwner() != null)
            map.put("player", getOwner().getUniqueId().toString());
        map.put("protected", isProtected());
        map.put("items", getItems());
        return map;
    }

}
