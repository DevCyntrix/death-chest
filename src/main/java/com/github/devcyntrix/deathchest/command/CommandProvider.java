package com.github.devcyntrix.deathchest.command;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import org.bukkit.command.CommandSender;

public interface CommandProvider {

    Command.Builder<CommandSender> provide(Command.Builder<CommandSender> commandManager);

}
