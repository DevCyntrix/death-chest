package com.github.devcyntrix.deathchest.command.admin;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.bukkit.parsers.WorldArgument;
import cloud.commandframework.context.CommandContext;
import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.audit.AuditAction;
import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyChestInfo;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyReason;
import com.github.devcyntrix.deathchest.command.CommandProvider;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Date;
import java.util.Map;

public class DeleteinworldCommandProvider implements CommandProvider {

    private final DeathChestPlugin plugin;

    public DeleteinworldCommandProvider(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Command.Builder<CommandSender> provide(Command.Builder<CommandSender> builder) {
        return builder
                .permission("deathchest.admin")
                .permission("deathchest.command.deleteinworld")
                .argument(
                        WorldArgument.of("world"),
                        ArgumentDescription.of("The world to delete death chests in.")
                )
                .handler(commandContext -> {
                    World world = commandContext.get("world");

                    long deletedCount = plugin.getChests()
                            .map(deathChest -> deleteChest(commandContext, deathChest))
                            .filter(aBoolean -> aBoolean)
                            .count();

                    commandContext.getSender().sendMessage("§aA total of " + deletedCount + " chests were deleted in world " + world.getName());
                });
    }

    private boolean deleteChest(CommandContext<CommandSender> commandContext, DeathChestModel deathChest) {
        plugin.getDeathChestController().destroyChest(deathChest);

        plugin.getAuditManager().audit(new AuditItem(new Date(), AuditAction.DESTROY_CHEST, new DestroyChestInfo(
                deathChest,
                DestroyReason.COMMAND,
                Map.of("executor", commandContext.getSender(),
                        "command", "/" + commandContext.getRawInputJoined())
        )));

        return true;
    }

    private String formatLocation(Location location) {
        return String.format("%d, %d, %d in world %s", location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }
}
