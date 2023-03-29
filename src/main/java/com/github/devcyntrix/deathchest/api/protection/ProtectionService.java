package com.github.devcyntrix.deathchest.api.protection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ProtectionService {

    boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material);

}
