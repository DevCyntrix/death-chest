package de.helixdevs.deathchest;

import com.google.common.collect.ImmutableList;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This plugin will create chests on death and will destroy them in after a specific time.
 */
@Getter
public class DeathChestPlugin extends JavaPlugin implements Listener {

    public static final int RESOURCE_ID = 101066;

    private final Set<DeathChest> deathChests = new HashSet<>();

    private DeathChestConfig deathChestConfig;

    private IHologramService hologramService;
    private IAnimationService animationService;
    private IProtectionService protectionService;

    private String newerVersion;

    @Override
    public void onDisable() {
        // Reset all death chests
        this.deathChests.forEach(DeathChest::close);
        this.deathChests.clear();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        this.deathChestConfig = DeathChestConfig.load(getConfig());

        this.hologramService = SupportServices.getHologramService(this, deathChestConfig.getPreferredHologramService());
        this.animationService = SupportServices.getAnimationService(this, deathChestConfig.getPreferredAnimationService());
        this.protectionService = SupportServices.getProtectionService(this);
        // Standard protection service: No service
        if (protectionService == null)
            this.protectionService = (player, location, material) -> true;

        getServer().getPluginManager().registerEvents(this, this);
        PluginCommand deathChestCommand = getCommand("deathchest");
        if (deathChestCommand != null) {
            deathChestCommand.setExecutor(this);
            deathChestCommand.setTabCompleter(this);
        }

        if (deathChestConfig.isUpdateCheck()) {
            UpdateChecker checker = new UpdateChecker(this, RESOURCE_ID);
            checker.getVersion(version -> {
                if (getDescription().getVersion().equals(version))
                    return;
                this.newerVersion = version;
                getLogger().warning("New version " + version + " is out. You are still running " + getDescription().getVersion());
                getLogger().warning("Update the plugin at https://www.spigotmc.org/resources/death-chest.101066/");
            });
        }
    }

    /**
     * Reloads the configuration file of the plugin
     */
    public void reload() {
        reloadConfig();
        this.deathChestConfig = DeathChestConfig.load(getConfig());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0)
            return false;
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reload();
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return ImmutableList.of("reload");
        }
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], ImmutableList.of("reload"), new LinkedList<>());
        }
        return Collections.emptyList();
    }

    /**
     * Creates the death chest if the player dies.
     * It only spawns a chest if the player has the permission to build in the region.
     * It puts the drops into the chest and clears the drops.
     *
     * @param event the event from the bukkit api
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory())
            return;
        if (event.getDrops().isEmpty())
            return;

        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        // fell into void
        if (deathLocation.getBlockY() <= player.getWorld().getMinHeight())
            return;

        // Check protection
        boolean build = protectionService.canBuild(player, deathLocation, Material.CHEST);
        if (!build)
            return;

        // Spawns the death chest
        deathLocation.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) deathLocation.getBlock().getState();
        DeathChest deathChest = new DeathChest(
                this,
                chest,
                deathChestConfig.getExpiration(),
                event.getDrops().toArray(new ItemStack[0]));
        getServer().getPluginManager().registerEvents(deathChest, this);
        this.deathChests.add(deathChest);

        String[] notificationMessage = deathChestConfig.getNotificationMessage();
        if (notificationMessage != null) {
            player.sendMessage(notificationMessage);
        }

        // Clears the drops
        event.getDrops().clear();
    }

    @EventHandler
    public void onNotifyUpdate(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (newerVersion == null)
            return;
        if (!player.hasPermission("deathchest.admin"))
            return;
        player.sendMessage("§8[§cDeath Chest§8] §cA new version " + newerVersion + " is out.");
        player.sendMessage("§8[§cDeath Chest§8] §cPlease update the plugin at https://www.spigotmc.org/resources/death-chest.101066/");
    }

    public void addChest(DeathChest chest) {
        this.deathChests.add(chest);
    }

    public void removeChest(DeathChest chest) {
        this.deathChests.remove(chest);
    }
}
