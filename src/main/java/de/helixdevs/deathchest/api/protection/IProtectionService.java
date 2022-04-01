package de.helixdevs.deathchest.api.protection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IProtectionService {

    boolean isAllowedToBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material);

}
