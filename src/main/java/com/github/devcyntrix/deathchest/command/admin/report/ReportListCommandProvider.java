package com.github.devcyntrix.deathchest.command.admin.report;

import cloud.commandframework.Command;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.report.Report;
import com.github.devcyntrix.deathchest.command.CommandProvider;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportListCommandProvider implements CommandProvider {

    private final DeathChestPlugin plugin;

    public ReportListCommandProvider(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Command.Builder<CommandSender> provide(Command.Builder<CommandSender> builder) {
        return builder
                .handler(commandContext -> {
                    Set<@NotNull Report> reports = plugin.getReportManager().getReports();
                    commandContext.getSender().sendMessage(
                            plugin.getPrefix() + "§7" + reports.stream()
                                    .map(Report::date)
                                    .map(Date::toString)
                                    .collect(Collectors.joining(", "))
                    );
                });
    }
}
