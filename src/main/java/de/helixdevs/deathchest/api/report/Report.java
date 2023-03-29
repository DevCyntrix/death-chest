package de.helixdevs.deathchest.api.report;

import com.google.gson.annotations.SerializedName;
import de.helixdevs.deathchest.DeathChestPlugin;
import de.helixdevs.deathchest.config.DeathChestConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Report(
        @SerializedName("date")
        Date date,
        @SerializedName("plugins")
        Set<PluginInfo> plugins,
        @SerializedName("config")
        DeathChestConfig config
) {

    public static Report create() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Set<PluginInfo> collect = Arrays.stream(pluginManager.getPlugins()).map(PluginInfo::of).collect(Collectors.toSet());
        DeathChestPlugin plugin = JavaPlugin.getPlugin(DeathChestPlugin.class);
        return new Report(new Date(), collect, plugin.getDeathChestConfig());
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
