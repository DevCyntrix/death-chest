package com.github.devcyntrix.deathchest.util;

import com.github.devcyntrix.deathchest.DeathChestModel;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ChestModelStringLookup implements StringLookup {

    @NotNull
    private final DeathChestModel model;
    @NotNull
    private final Function<Long, String> duration;

    public ChestModelStringLookup(@NotNull DeathChestModel model, @NotNull Function<Long, String> duration) {
        this.model = model;
        this.duration = duration;
    }

    @Override
    public String lookup(String key) {

        if ("duration".equals(key))
            return duration.apply(model.getExpireAt());
        if ("player_name".equals(key)) {
            if (model.getOwner() == null)
                return "Unknown";
            return model.getOwner().getName();
        }
        if ("player_displayname".equals(key)) {
            if (model.getOwner() == null)
                return "Unknown";
            Player oP = model.getOwner().getPlayer();
            return oP != null ? oP.getDisplayName() : model.getOwner().getName();
        }

        Location location = model.getLocation();
        if ("world".equals(key)) {
            World world = location.getWorld();
            return world != null ? world.getName() : null;
        }
        if ("x".equals(key) || "chest_x".equals(key)) {
            return String.valueOf(location.getBlockX());
        }
        if ("y".equals(key) || "chest_y".equals(key)) {
            return String.valueOf(location.getBlockY());
        }
        if ("z".equals(key) || "chest_z".equals(key)) {
            return String.valueOf(location.getBlockZ());
        }
        return null;
    }
}
