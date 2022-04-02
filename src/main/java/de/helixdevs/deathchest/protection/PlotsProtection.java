package de.helixdevs.deathchest.protection;

import de.helixdevs.deathchest.api.protection.IProtectionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * https://www.spigotmc.org/resources/plots.44573/
 */
public class PlotsProtection implements IProtectionService {
    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        return false;
    }
}
