package com.github.devcyntrix.deathchest.command.admin;

import cloud.commandframework.Command;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.audit.AuditAction;
import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.info.ReloadInfo;
import com.github.devcyntrix.deathchest.command.CommandProvider;
import org.bukkit.command.CommandSender;

import java.util.Date;

public class ReloadCommandProvider implements CommandProvider {

    private final DeathChestPlugin plugin;

    public ReloadCommandProvider(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Command.Builder<CommandSender> provide(Command.Builder<CommandSender> builder) {
        return builder
                .permission("deathchest.admin")
                .permission("deathchest.command.reload")
                .handler(commandContext -> {
                    plugin.getAuditManager().audit(
                            new AuditItem(
                                    new Date(),
                                    AuditAction.RELOAD_PLUGIN,
                                    new ReloadInfo(commandContext.getSender())
                            )
                    );
                    plugin.reload();
                    commandContext.getSender().sendMessage(
                            plugin.getPrefix() + "§cThe plugin has been successfully reloaded"
                    );
                });
    }
}
