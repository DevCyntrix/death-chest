package com.github.devcyntrix.deathchest.command.user;

import cloud.commandframework.Command;
import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.command.CommandProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommandProvider implements CommandProvider {

    private final DeathChestPlugin plugin;

    public TeleportCommandProvider(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Command.Builder<CommandSender> provide(Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .permission("deathchest.command.teleport")
                .handler(context -> {
                    Player playerSender = (Player) context.getSender();

                    DeathChestModel lastChest = plugin.getLastChest(playerSender);
                    if(lastChest == null || lastChest.isExpired())  {
                        plugin.getDeathChestConfig().teleportOptions().showNotFound(plugin.getAudiences().player(playerSender));
                        return;
                    }

                    playerSender.teleport(lastChest.getLocation());
                });
    }
}
