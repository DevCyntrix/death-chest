package com.github.devcyntrix.deathchest.feature.report;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.report.PluginInfo;
import com.github.devcyntrix.deathchest.api.report.Report;
import com.github.devcyntrix.deathchest.api.report.ReportStore;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private final ReportStore reportStore;

    public ReportService(ReportStore reportStore) {
        Preconditions.checkNotNull(reportStore);
        this.reportStore = reportStore;
    }


    /**
     * Creates a new report object
     *
     * @return the new report object
     */
    public void createReport() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Set<PluginInfo> collect = Arrays.stream(pluginManager.getPlugins()).map(PluginInfo::of).collect(Collectors.toSet());
        DeathChestPlugin plugin = JavaPlugin.getPlugin(DeathChestPlugin.class);
        addReport(new Report(new Date(), collect, plugin.getDeathChestConfig(), new HashMap<>()));
    }


    public void addReport(@NotNull Report report) {
        reportStore.addReport(report);
    }

    public @Nullable Report getLatestReport() {
        return this.reportStore.getLatestReport();
    }

    public @NotNull Set<@NotNull Report> getReports() {
        return this.reportStore.getReports();
    }

    public @NotNull TreeSet<Date> getReportDates() {
        return this.reportStore.getReportDates();
    }

    public boolean deleteReport(@NotNull Date date) {
        return this.reportStore.deleteReport(date);
    }

    public void deleteReports() {
        this.reportStore.deleteReports();
    }

}
