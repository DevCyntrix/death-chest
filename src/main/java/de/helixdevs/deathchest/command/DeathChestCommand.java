package de.helixdevs.deathchest.command;

import com.google.common.collect.ImmutableList;
import de.helixdevs.deathchest.DeathChestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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
        if (args.length == 0) return false;
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!(sender.hasPermission("deathchest.command.reload"))) {
                return true;
            }

            plugin.onDisable();
            plugin.onEnable();
            sender.sendMessage(plugin.getPrefix() + "§cThe plugin has been reloaded successfully");
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
            plugin.getChests().stream().filter(deathChest -> finalWorld.equals(deathChest.getWorld())).forEach(deathChest -> {
                try {
                    deathChest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            sender.sendMessage(plugin.getPrefix() + "§cAll chests in the world \"" + world.getName() + "\" were deleted.");

            return true;
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
        if (args.length == 0) {
            return ImmutableList.of("reload", "deleteInWorld");
        }
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], ImmutableList.of("reload", "deleteInWorld"), new LinkedList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("deleteInWorld")) {
            return StringUtil.copyPartialMatches(args[1], Bukkit.getWorlds().stream().map(WorldInfo::getName).toList(), new LinkedList<>());
        }
        return Collections.emptyList();
    }
}
