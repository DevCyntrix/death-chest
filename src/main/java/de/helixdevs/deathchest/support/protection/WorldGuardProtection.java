package de.helixdevs.deathchest.support.protection;

import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * https://dev.bukkit.org/projects/worldguard
 */
public class WorldGuardProtection implements IProtectionService {

    private final ProtectionQuery protectionQuery = WorldGuardPlugin.inst().createProtectionQuery();

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        return protectionQuery.testBlockPlace(player, location, material);
    }
}
