package com.github.devcyntrix.deathchest.command.admin.report;

import cloud.commandframework.Command;
import com.github.devcyntrix.deathchest.command.CommandProvider;
import org.bukkit.command.CommandSender;

public class ReportCommandProvider implements CommandProvider {
    @Override
    public Command.Builder<CommandSender> provide(Command.Builder<CommandSender> builder) {
        return builder
                .permission("deathchest.admin")
                .permission("deathchest.command.report");
    }
}
