package com.github.devcyntrix.deathchest.support.protection;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RedProtection implements ProtectionService {

    private final RedProtect protect = RedProtect.get();

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        Region region = protect.getAPI().getRegion(location);
        if (region == null)
            return true;
        return region.canBuild(player);
    }
}
