package de.helixdevs.deathchest.api;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public interface DeathChestSnapshot extends ConfigurationSerializable {

    @NotNull Location getLocation();

    @NotNull ItemStack @NotNull [] getItems();

    @Nullable OfflinePlayer getOwner();

    long getCreatedAt();

    long getExpireAt();

    default DeathChest createChest(DeathChestService service) {
        return service.createDeathChest(getLocation(), getCreatedAt(), getExpireAt(), getOwner(), getItems());
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
        map.put("items", getItems());
        return map;
    }

}
