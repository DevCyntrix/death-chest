package de.helixdevs.deathchest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.DeathChestService;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import de.helixdevs.deathchest.config.DeathChestConfig;
import de.helixdevs.deathchest.config.NotificationOptions;
import lombok.Getter;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * This plugin will create chests on death and will destroy them in after a specific time.
 */
@Getter
public class DeathChestPlugin extends JavaPlugin implements Listener, DeathChestService {

    public static final int RESOURCE_ID = 101066;
    public static final int BSTATS_ID = 14866;

    protected final Set<DeathChest> deathChests = new HashSet<>();

    private DeathChestConfig deathChestConfig;

    private IHologramService hologramService;
    private IAnimationService animationService;
    private IProtectionService protectionService;

    private String newerVersion;

    @Override
    public void onDisable() {
        // Save all chests
        File savedChests = new File(getDataFolder(), "saved-chests.yml");
        try {
            saveChests(savedChests, this.deathChests);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Reset all death chests
        this.deathChests.forEach(deathChest -> {
            try {
                deathChest.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.deathChests.clear();
    }

    private void saveChests(File file, Collection<DeathChest> chests) throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        List<Map<String, Object>> collect = chests.stream().map(DeathChest::serialize).collect((Supplier<List<Map<String, Object>>>) Lists::newArrayList, List::add, List::addAll);
        configuration.set("chests", collect);
        configuration.save(file);
    }

    @Override
    public void onEnable() {
        // Recreate the config when the config version is too old.
        if (getConfig().getInt("config-version", 0) != DeathChestConfig.CONFIG_VERSION) {
            File configFile = new File(getDataFolder(), "config.yml");
            if (configFile.isFile()) {
                File oldConfigFile = new File(getDataFolder(), "config.yml.old");
                boolean b = configFile.renameTo(oldConfigFile);
                if (!b) {
                    throw new IllegalStateException("Failed to rename the configuration file to old.");
                }
            }
        }

        reload();

        this.hologramService = SupportServices.getHologramService(this, this.deathChestConfig.preferredHologramService());
        this.animationService = SupportServices.getAnimationService(this, this.deathChestConfig.preferredAnimationService());
        this.protectionService = SupportServices.getProtectionService(this);
        // Standard protection service: No service
        if (protectionService == null)
            this.protectionService = (player, location, material) -> true;

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getServicesManager().register(DeathChestService.class, this, this, ServicePriority.Normal);

        PluginCommand deathChestCommand = getCommand("deathchest");
        if (deathChestCommand != null) {
            deathChestCommand.setExecutor(this);
            deathChestCommand.setTabCompleter(this);
        }

        File savedChests = new File(getDataFolder(), "saved-chests.yml");
        int size = loadChests(savedChests).size();
        getLogger().info(size + " death chests loaded.");

        if (this.deathChestConfig.updateChecker()) {
            UpdateChecker checker = new UpdateChecker(this, RESOURCE_ID);
            checker.getVersion(version -> {
                if (getDescription().getVersion().equals(version))
                    return;
                this.newerVersion = version;
                getLogger().warning("New version " + version + " is out. You are still running " + getDescription().getVersion());
                getLogger().warning("Update the plugin at https://www.spigotmc.org/resources/death-chest.101066/");
            });
        }

        Metrics metrics = new Metrics(this, BSTATS_ID);
    }

    private Collection<DeathChest> loadChests(File file) {
        if (!file.isFile())
            return Collections.emptyList();

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        List<?> chests = configuration.getList("chests");
        if (chests == null)
            return Collections.emptyList();

        List<DeathChest> list = new ArrayList<>(chests.size());

        for (Object chest : chests) {
            if (!(chest instanceof Map<?, ?> map))
                continue;

            long expireAt = (long) map.get("expireAt");
            if (expireAt <= System.currentTimeMillis()) // Expire here
                continue;

            Location location = (Location) map.get("location");
            if (location == null)
                continue;

            long createdAt = (long) map.get("createdAt");
            String player = (String) map.get("player");
            UUID playerId = player == null ? null : UUID.fromString(player);

            List<ItemStack> stacks = (List<ItemStack>) map.get("items");
            if (stacks == null)
                continue;

            list.add(createDeathChest(location, createdAt, expireAt, playerId == null ? null : Bukkit.getOfflinePlayer(playerId), stacks.toArray(ItemStack[]::new)));
        }
        return list;
    }

    /**
     * Reloads the configuration file of the plugin
     */
    public void reload() {
        saveDefaultConfig();
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

        Duration expiration = deathChestConfig.expiration();
        if (expiration == null)
            expiration = Duration.ofSeconds(-1);

        long createdAt = System.currentTimeMillis();

        long expireAt;
        if (!expiration.isNegative() && !expiration.isZero()) {
            expireAt = createdAt + expiration.toMillis();
        } else {
            expireAt = -1; // Permanent
        }

        createDeathChest(deathLocation.getBlock().getLocation(), createdAt, expireAt, player, event.getDrops().toArray(new ItemStack[0]));

        NotificationOptions notificationOptions = deathChestConfig.notificationOptions();

        if (notificationOptions != null && notificationOptions.enabled() && notificationOptions.messages() != null) {
            StringSubstitutor substitutor = new StringSubstitutor(Map.of(
                    "x", deathLocation.getBlockX(),
                    "y", deathLocation.getBlockY(),
                    "z", deathLocation.getBlockZ()));
            for (String message : notificationOptions.messages()) {
                player.sendMessage(substitutor.replace(message));
            }
        }

        // Clears the drops
        event.getDrops().clear();
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull Location location, ItemStack @NotNull ... stacks) {
        return createDeathChest(location, null, stacks);
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull Location location, @Nullable OfflinePlayer player, ItemStack @NotNull ... stacks) {
        return createDeathChest(location, -1, player, stacks);
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull Location location, long expireAt, @Nullable OfflinePlayer player, ItemStack @NotNull ... stacks) {
        return createDeathChest(location, System.currentTimeMillis(), expireAt, player, stacks);
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull Location location, long createdAt, long expireAt, @Nullable OfflinePlayer player, ItemStack @NotNull ... stacks) {
        DeathChest build = DeathChestBuilder.builder()
                .setCreatedAt(createdAt)
                .setExpireAt(expireAt)
                .setPlayer(player)
                .setItems(stacks)
                .setAnimationService(animationService)
                .setHologramService(hologramService)
                .setBreakEffectOptions(deathChestConfig.breakEffectOptions())
                .setHologramOptions(deathChestConfig.hologramOptions())
                .setParticleOptions(deathChestConfig.particleOptions())
                .build(location, deathChestConfig.inventoryOptions());
        this.deathChests.add(build);
        return build;
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

    @Override
    public @NotNull Set<@NotNull DeathChest> getChests() {
        return this.deathChests;
    }

    @Override
    public IHologramService getHologramService() {
        return hologramService;
    }

    @Override
    public IAnimationService getAnimationService() {
        return animationService;
    }

    @Override
    public @NotNull IProtectionService getProtectionService() {
        return protectionService;
    }
}
