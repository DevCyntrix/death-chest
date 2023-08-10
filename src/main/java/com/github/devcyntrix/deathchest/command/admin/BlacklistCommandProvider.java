package com.github.devcyntrix.deathchest.command.admin;

import cloud.commandframework.Command;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.blacklist.ItemBlacklist;
import com.github.devcyntrix.deathchest.command.CommandProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BlacklistCommandProvider implements CommandProvider {

    private final DeathChestPlugin plugin;

    public BlacklistCommandProvider(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Command.Builder<CommandSender> provide(Command.Builder<CommandSender> builder) {
        return builder
                .permission("deathchest.admin")
                .permission("deathchest.command.blacklist")
                .handler(commandContext -> {
                    CommandSender sender = commandContext.getSender();
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("§cYou have to be in game");
                        return;
                    }
                    ItemBlacklist blacklist = plugin.getBlacklist();
                    Inventory inventory = blacklist.getInventory();
                    player.openInventory(inventory);
                });
    }

}
