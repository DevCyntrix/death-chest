package com.github.devcyntrix.deathchest.support.protection;

import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.minecodes.plots.api.plot.PlotApi;
import pl.minecodes.plots.api.plot.PlotServiceApi;

/**
 * <a href="https://builtbybit.com/resources/mineplots.21646/">minePlots</a>
 */
public class MinePlotsProtection implements ProtectionService {

    private final PlotServiceApi api;

    public MinePlotsProtection() {
        this.api = Bukkit.getServicesManager().load(PlotServiceApi.class);
        if (this.api == null)
            throw new IllegalStateException();
    }

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        PlotApi plot = api.getPlot(location);
        return plot == null || plot.hasAccess(player);
    }
}
