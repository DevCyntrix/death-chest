package de.helixdevs.deathchest.support.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import de.helixdevs.deathchest.util.WorldGuardDeathChestFlag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * https://dev.bukkit.org/projects/worldguard
 */
public class WorldGuardProtection implements IProtectionService {

    private final ProtectionQuery protectionQuery = WorldGuardPlugin.inst().createProtectionQuery();
    private final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        RegionQuery query = container.createQuery();
        com.sk89q.worldedit.util.Location guardLocation = BukkitAdapter.adapt(location);
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        return protectionQuery.testBlockPlace(player, location, material) || query.testState(guardLocation, localPlayer, WorldGuardDeathChestFlag.FLAG);
    }
}
