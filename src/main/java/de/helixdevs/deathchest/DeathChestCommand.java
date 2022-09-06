package de.helixdevs.deathchest;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DeathChestCommand implements TabExecutor {

    private final DeathChestPlugin plugin;

    public DeathChestCommand(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0)
            return false;
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.onDisable();
            plugin.onEnable();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return ImmutableList.of("reload");
        }
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], ImmutableList.of("reload"), new LinkedList<>());
        }
        return Collections.emptyList();
    }
}
