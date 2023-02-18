package de.helixdevs.deathchest.support.protection;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GriefDefenderProtectionService implements IProtectionService {

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        World world = location.getWorld();
        if (world == null)
            return true;
        Claim claimAt = GriefDefender.getCore().getClaimAt(world.getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (claimAt == null)
            return true;
        return claimAt.canPlace(player, material, location, null);
    }
}
