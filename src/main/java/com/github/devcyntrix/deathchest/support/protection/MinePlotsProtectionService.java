package com.github.devcyntrix.deathchest.support.protection;

import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.minecodes.plots.api.plot.PlotServiceApi;

public class MinePlotsProtectionService implements ProtectionService {

    private PlotServiceApi api;

    public MinePlotsProtectionService() {
        this.api = Bukkit.getServicesManager().load(PlotServiceApi.class);
        if (this.api == null)
            throw new IllegalStateException();
    }

    @Override
    public boolean canBuild(@NotNull Player player, @NotNull Location location, @NotNull Material material) {
        return api.getPlot(location).hasAccess(player);
    }
}
