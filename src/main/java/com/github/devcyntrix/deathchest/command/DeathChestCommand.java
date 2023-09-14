package com.github.devcyntrix.deathchest.command;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.api.audit.AuditAction;
import com.github.devcyntrix.deathchest.api.audit.AuditItem;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyChestInfo;
import com.github.devcyntrix.deathchest.api.audit.info.DestroyReason;
import com.github.devcyntrix.deathchest.api.audit.info.ReloadInfo;
import com.github.devcyntrix.deathchest.api.report.Report;
import com.github.devcyntrix.deathchest.api.report.ReportManager;
import com.github.devcyntrix.deathchest.blacklist.ItemBlacklist;
import com.google.common.collect.ImmutableList;
import com.google.gson.internal.bind.util.ISO8601Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DeathChestCommand implements TabExecutor {

    private final DeathChestPlugin plugin;

    public DeathChestCommand(DeathChestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return false;
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!(sender.hasPermission("deathchest.command.reload"))) {
                return true;
            }

            plugin.reload();
            plugin.getAuditManager().audit(new AuditItem(new Date(), AuditAction.RELOAD_PLUGIN, new ReloadInfo(sender)));
            sender.sendMessage(plugin.getPrefix() + "§cThe plugin has been successfully reloaded");
            return true;
        }

        if (args.length <= 2 && args[0].equalsIgnoreCase("deleteInWorld") && sender.hasPermission("deathchest.command.deleteInWorld")) {
            World world = null;
            if (args.length == 2) {
                world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    sender.sendMessage(plugin.getPrefix() + "§cWorld not found. Please check your input.");
                    return true;
                }
            }

            if (world == null) { // Delete all chests
                plugin.getChests().forEach(deathChest -> {
                    plugin.getAuditManager().audit(new AuditItem(new Date(), AuditAction.DESTROY_CHEST, new DestroyChestInfo(
                            deathChest,
                            DestroyReason.COMMAND,
                            Map.of("executor", sender,
                                    "command", "/" + label + " " + String.join(" ", args))
                    )));
                    try {
                        deathChest.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                sender.sendMessage(plugin.getPrefix() + "§cAll chests in each world were deleted.");
                return true;
            }

            World finalWorld = world;
            plugin.getChests().filter(deathChest -> finalWorld.equals(deathChest.getWorld())).forEach(deathChest -> {
                try {
                    deathChest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            sender.sendMessage(plugin.getPrefix() + "§cAll chests in the world \"" + world.getName() + "\" were deleted.");
            return true;
        }

        if (args[0].equalsIgnoreCase("chests") && sender.hasPermission("deathchest.command.chest")) {
            if (!(sender instanceof Player player)) {
                return false;
            }
           /* ChestsGui gui = new ChestsGui(player, plugin);
            gui.open(); */
            return true;
        }

        if (args[0].equalsIgnoreCase("blacklist") && sender.hasPermission("deathchest.command.blacklist")) {
            if (!(sender instanceof Player player)) {
                return false;
            }
            ItemBlacklist blacklist = plugin.getBlacklist();
            Inventory inventory = blacklist.getInventory();
            player.openInventory(inventory);

            return true;
        }

        if (args[0].equalsIgnoreCase("report") && sender.hasPermission("deathchest.command.report")) {

            if (args.length == 1) {
                return true;
            }

            if (args[1].equalsIgnoreCase("create")) {
                plugin.getReportManager().createReport();
                sender.sendMessage(plugin.getPrefix() + "§7A new report was created successfully.");
                return true;
            }

            if (args[1].equalsIgnoreCase("list")) {
                Set<@NotNull Report> reports = plugin.getReportManager().getReports();
                sender.sendMessage(plugin.getPrefix() + "§7" + reports.stream().map(Report::date).map(Date::toString).collect(Collectors.joining(", ")));
                return true;
            }

            if (args[1].equalsIgnoreCase("latest")) {
                Report latestReport = plugin.getReportManager().getLatestReport();
                if (latestReport == null) {
                    sender.sendMessage(plugin.getPrefix() + "§cNo report found");
                    return true;
                }

                BaseComponent[] baseComponents = TextComponent.fromLegacyText(plugin.getPrefix() + "§7");
                DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();
                TextComponent message = new TextComponent("The latest report you created is from " + dateTimeInstance.format(latestReport.date()) + " ");
                message.setColor(ChatColor.GRAY);

                TextComponent copy = new TextComponent("[Copy]");
                copy.setColor(ChatColor.RED);
                copy.setUnderlined(true);
                copy.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ReportManager.formatISO(latestReport.date())));
                copy.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to copy the file name")));


                BaseComponent[] components = new BaseComponent[baseComponents.length + 2];

                System.arraycopy(baseComponents, 0, components, 0, baseComponents.length);
                components[baseComponents.length] = message;
                components[baseComponents.length + 1] = copy;

                sender.spigot().sendMessage(components);
                //sender.sendMessage(plugin.getPrefix() + "§7" + ReportManager.formatISO(latestReport.date()));
                return true;
            }

            if (args.length == 3 && args[1].equalsIgnoreCase("delete")) {
                Date date = ReportManager.parseISO(args[2]);
                if (date == null) {
                    sender.sendMessage(plugin.getPrefix() + "§cCannot parse given date format");
                    return true;
                }
                boolean b = plugin.getReportManager().deleteReport(date);
                if (b) {
                    sender.sendMessage(plugin.getPrefix() + "§7You deleted the report successfully");
                } else {
                    sender.sendMessage(plugin.getPrefix() + "§cCannot find report");
                }
                return true;
            }

            if (args[1].equalsIgnoreCase("deleteAll")) {
                plugin.getReportManager().deleteReports();
                sender.sendMessage(plugin.getPrefix() + "§7You deleted all reports successfully");
                return true;
            }

        }

//        if (args.length == 1 && args[0].equalsIgnoreCase("chests")) {
//            if (!(sender.hasPermission("deathchest.command.chests"))) {
//                return true;
//            }
//            if (!(sender instanceof Player)) {
//                return true;
//            }
//            Player player = (Player) sender;
//            ChestMenu.Builder pageBuilder = ChestMenu.builder(4).title("Death Chests").redraw(true);
//            PaginatedMenuBuilder chestsMenu = PaginatedMenuBuilder.builder(pageBuilder);
//            chestsMenu.slots(BinaryMask.builder(pageBuilder.getDimensions()).pattern("011111110").build());
//
//            Set<DeathChest> deathChests = plugin.getDeathChests();
//            chestsMenu.addSlotSettings(deathChests.stream().map(next -> {
//                ItemStack stack = new ItemStack(Material.CHEST_MINECART);
//                ItemMeta itemMeta = stack.getItemMeta();
//                OfflinePlayer deadPlayer = next.getPlayer();
//                String itemName = deadPlayer != null ? deadPlayer.getName() + "'s death chest" : "Unknown death chest";
//                itemMeta.setDisplayName(itemName);
//                Location location = next.getLocation();
//                itemMeta.setLore(Arrays.asList(
//                        "§7" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ(),
//                        "§7Created at: §e" + new Date(next.getCreatedAt()),
//                        "§7Expires at: §e" + new Date(next.getExpireAt())
//                ));
//                stack.setItemMeta(itemMeta);
//                return SlotSettings.builder()
//                        .item(stack)
//                        .clickHandler((player1, click) -> {
//                            if (click.getClickType() != ClickType.LEFT)
//                                return;
//                            player1.openInventory(next.getInventory());
//                        }).build();
//            }).collect(Collectors.toList()));
//
//            List<Menu> build = chestsMenu.build();
//            build.get(0).open(player);
//            return true;
//        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], ImmutableList.of("reload", "deleteInWorld", "report", "blacklist"), new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("deleteInWorld")) {
            return StringUtil.copyPartialMatches(args[1], Bukkit.getWorlds().stream().map(WorldInfo::getName).toList(), new ArrayList<>());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("report")) {
            return StringUtil.copyPartialMatches(args[1], ImmutableList.of("create", "list", "latest", "delete", "deleteAll"), new ArrayList<>());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("report") && args[1].equalsIgnoreCase("delete")) {
            return StringUtil.copyPartialMatches(args[2], plugin.getReportManager().getReportDates().stream().map(ISO8601Utils::format).toList(), new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
