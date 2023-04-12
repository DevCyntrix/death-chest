package com.github.devcyntrix.deathchest.support.protection;

import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.flag.implementations.PlaceFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import com.plotsquared.core.util.Permissions;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <a href="https://www.spigotmc.org/resources/plotsquared-v4-v6-out-now.1177/">PlotSquared</a>
 */
public class PlotSquaredProtection implements ProtectionService {

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull org.bukkit.Location loc, @NotNull Material material) {
        /*
         * Copied from original code of the PlotSquared git repository
         * https://github.com/IntellectualSites/PlotSquared/blob/dc5c80d8123afd4ff8997078901f2133ba3c6052/Bukkit/src/main/java/com/plotsquared/bukkit/listener/BlockEventListener.java#L273-L343
         */

        Location location = BukkitUtil.adapt(loc);
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return true;
        }
        BukkitPlayer pp = BukkitUtil.adapt(player);
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if ((location.getY() >= area.getMaxBuildHeight() || location.getY() < area
                    .getMinBuildHeight()) && !Permissions
                    .hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_HEIGHT_LIMIT)) {
                return false;
            }
            if (!plot.hasOwner()) {
                return Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_UNOWNED);
            } else if (!plot.isAdded(pp.getUUID())) {
                List<BlockTypeWrapper> place = plot.getFlag(PlaceFlag.class);
                if (place.contains(BlockTypeWrapper.get(BukkitAdapter.asBlockType(material)))) {
                    return true;
                }
                return Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_OTHER);
            } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                return Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_OTHER);
            }
        } else return Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_ROAD);
        return true;
    }
}
