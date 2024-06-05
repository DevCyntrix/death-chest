package com.github.devcyntrix.deathchest.config;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface DropCondition {

    boolean shouldDropItems(@NotNull Location location);

}
