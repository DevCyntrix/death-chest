package com.github.devcyntrix.deathchest.command.admin.report;

import cloud.commandframework.Command;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.command.CommandProvider;
import org.bukkit.command.CommandSender;

public class ReportDeleteallCommandProvider implements CommandProvider {

    private final DeathChestPlugin plugin;

    public ReportDeleteallCommandProvider(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Command.Builder<CommandSender> provide(Command.Builder<CommandSender> builder) {
        return builder
                .handler(commandContext -> {
                    plugin.getReportManager().deleteReports();
                    commandContext.getSender().sendMessage(
                            plugin.getPrefix() + "§7You deleted all reports successfully"
                    );
                });
    }
}
