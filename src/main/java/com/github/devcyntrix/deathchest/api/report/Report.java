package com.github.devcyntrix.deathchest.api.report;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.config.DeathChestConfig;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public record Report(
        @SerializedName("date")
        Date date,
        @SerializedName("plugins")
        Set<PluginInfo> plugins,
        @SerializedName("config")
        DeathChestConfig config,
        @SerializedName("extra")
        Map<String, Object> extra
) {

    /**
     * Creates a new report object
     *
     * @return the new report object
     */
    public static Report create() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Set<PluginInfo> collect = Arrays.stream(pluginManager.getPlugins()).map(PluginInfo::of).collect(Collectors.toSet());
        DeathChestPlugin plugin = JavaPlugin.getPlugin(DeathChestPlugin.class);
        return new Report(new Date(), collect, plugin.getDeathChestConfig(), new HashMap<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(date, report.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
