package com.github.devcyntrix.deathchest.util;

import org.bukkit.Location;

public final class LocationUtil {

    public static boolean isValidBlock(Location location) {
        if (location.getWorld() == null)
            return false;
        return location.getBlockY() >= location.getWorld().getMinHeight() && location.getBlockY() < location.getWorld().getMaxHeight();
    }

}
