package de.helixdevs.deathchest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BuildPredicate {

    boolean test(@NotNull Player player, @NotNull Location location, @NotNull Material material);

}
