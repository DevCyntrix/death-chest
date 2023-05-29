package com.github.devcyntrix.deathchest.command.admin.report;

import cloud.commandframework.Command;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.command.CommandProvider;
import com.github.devcyntrix.deathchest.command.admin.argument.DateArgument;
import org.bukkit.command.CommandSender;

import java.util.Date;

public class ReportDeleteCommandProvider implements CommandProvider {

    private final DeathChestPlugin plugin;

    public ReportDeleteCommandProvider(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Command.Builder<CommandSender> provide(Command.Builder<CommandSender> builder) {
        return builder
                .argument(DateArgument.of(plugin, "date"))
                .handler(commandContext -> {
                    Date date = commandContext.get("date");

                    boolean success = plugin.getReportManager().deleteReport(date);
                    if (success) {
                        commandContext.getSender().sendMessage(
                                plugin.getPrefix() + "§7You deleted the report successfully"
                        );
                    } else {
                        commandContext.getSender().sendMessage(
                                plugin.getPrefix() + "§cCannot find report"
                        );
                    }
                });
    }
}
