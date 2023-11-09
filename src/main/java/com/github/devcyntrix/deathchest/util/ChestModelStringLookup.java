package com.github.devcyntrix.deathchest.util;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.github.devcyntrix.deathchest.config.WorldAliasConfig;
import org.apache.commons.text.lookup.StringLookup;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ChestModelStringLookup implements StringLookup {

    private final DeathChestConfig config;
    @NotNull
    private final DeathChestModel model;
    @NotNull
    private final Function<Long, String> duration;

    public ChestModelStringLookup(@NotNull DeathChestConfig config, @NotNull DeathChestModel model, @NotNull Function<Long, String> duration) {
        this.config = config;
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
        World world = location.getWorld();
        if ("world".equals(key)) {
            return world != null ? world.getName() : null;
        }
        if ("world_alias".equals(key)) {
            if (world == null)
                return null;
            return config.worldAlias().getAlias(world.getName());
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
