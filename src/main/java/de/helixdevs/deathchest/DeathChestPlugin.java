package de.helixdevs.deathchest;

import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.DeathChestService;
import de.helixdevs.deathchest.api.DeathChestSnapshot;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import de.helixdevs.deathchest.api.storage.DeathChestStorage;
import de.helixdevs.deathchest.command.DeathChestCommand;
import de.helixdevs.deathchest.config.ChestProtectionOptions;
import de.helixdevs.deathchest.config.DeathChestConfig;
import de.helixdevs.deathchest.hologram.NativeHologramService;
import de.helixdevs.deathchest.listener.SpawnChestListener;
import de.helixdevs.deathchest.listener.UpdateNotificationListener;
import de.helixdevs.deathchest.support.storage.YamlStorage;
import de.helixdevs.deathchest.util.Metrics;
import de.helixdevs.deathchest.util.UpdateChecker;
import de.helixdevs.deathchest.util.WorldGuardDeathChestFlag;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * This plugin creates chests if a player dies and will destroy them after a specific time.
 * You can download this plugin on SpigotMC: https://www.spigotmc.org/resources/death-chest.101066/
 * You are welcome to contribute to this plugin!
 */
@Getter
public class DeathChestPlugin extends JavaPlugin implements Listener, DeathChestService {

    public static final int RESOURCE_ID = 101066;
    public static final int BSTATS_ID = 14866;

    protected final Set<DeathChest> deathChests = new CopyOnWriteArraySet<>();

    private DeathChestConfig deathChestConfig;

    private IHologramService hologramService;
    private IAnimationService animationService;
    private IProtectionService protectionService;

    @Getter
    private String newerVersion;

    @Getter
    private static boolean placeholderAPIEnabled;

    private DeathChestStorage storage;

    /**
     * This method cleanups the whole plugin
     */
    @Override
    public void onDisable() {
        // Save all chests
        try {
            saveChests();
        } catch (IOException e) {
            e.printStackTrace();
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
        HandlerList.unregisterAll((Listener) this);
    }

    @Override
    public void onLoad() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = pluginManager.getPlugin("WorldGuard");
        if (plugin != null) {
            WorldGuardDeathChestFlag.register();
        }
    }

    @Override
    public void onEnable() {
        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        checkConfigVersion();
        reload();

        this.hologramService = new NativeHologramService();
        this.animationService = SupportServices.getAnimationService(this, this.deathChestConfig.preferredAnimationService());
        this.protectionService = SupportServices.getProtectionService(this);
        // Standard protection service: No service
        if (protectionService == null)
            this.protectionService = (player, location, material) -> true;

        PluginManager pluginManager = getServer().getPluginManager();

        // Registers the protection permissions if they are not registered
        try {
            ChestProtectionOptions protectionOptions = getDeathChestConfig().chestProtectionOptions();
            if (protectionOptions.enabled()) {
                if (pluginManager.getPermission(protectionOptions.permission()) == null)
                    pluginManager.addPermission(new org.bukkit.permissions.Permission(protectionOptions.permission()));
                if (pluginManager.getPermission(protectionOptions.bypassPermission()) == null)
                    pluginManager.addPermission(new org.bukkit.permissions.Permission(protectionOptions.bypassPermission()));
            }
        } catch (Exception e) {
            getLogger().warning("Failed to register the permission of the chest-protection");
            e.printStackTrace();
        }

        pluginManager.registerEvents(new UpdateNotificationListener(this), this);
        pluginManager.registerEvents(new SpawnChestListener(this), this);
        //getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this);

        ServicesManager servicesManager = getServer().getServicesManager();
        servicesManager.register(DeathChestService.class, this, this, ServicePriority.Normal);

        // Registers the deathchest command
        PluginCommand deathChestCommand = getCommand("deathchest");
        if (deathChestCommand != null) {
            DeathChestCommand command = new DeathChestCommand(this);
            deathChestCommand.setExecutor(command);
            deathChestCommand.setTabCompleter(command);
        }

        // Initialize the storage system
        try {
            this.storage = new YamlStorage();
            this.storage.init(this, new MemoryConfiguration());
        } catch (IOException e) {
            getLogger().severe("Failed to initialize the storage system. Please check your configuration file.");
            throw new RuntimeException(e);
        }

        // Recreates the deathchests
        Set<DeathChestSnapshot> chests = this.storage.getChests();
        chests.forEach(deathChestSnapshot -> this.deathChests.add(deathChestSnapshot.createChest(this)));
        getLogger().info(this.deathChests.size() + " death chests loaded.");

        // Checks for updates
        if (this.deathChestConfig.updateChecker()) {
            startUpdateChecker();
        }

        new Metrics(this, BSTATS_ID);
    }

    /**
     * Checks for the newest version by using the SpigotMC api
     */
    private void startUpdateChecker() {
        UpdateChecker checker = new UpdateChecker(this, RESOURCE_ID);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            checker.getVersion(version -> {
                if (getDescription().getVersion().equals(version))
                    return;
                this.newerVersion = version;
                getLogger().warning("New version " + version + " is out. You are still running " + getDescription().getVersion());
                getLogger().warning("Please update the plugin at https://www.spigotmc.org/resources/death-chest.101066/");
            });
        }, 0, 20 * 60 * 30); // Every 30 minutes
    }

    /**
     * Checks for the current config version to avoid conflicts if the user updates the plugin. The config will
     * recreate and the old config file will rename to config.yml.old.
     */
    private void checkConfigVersion() {
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
    }

    /**
     * Creates snapshots of all current valid chests and hand over the snapshots to the storage system which saves them.
     *
     * @throws IOException depends on the storage system
     */
    @Override
    public void saveChests() throws IOException {
        this.storage.putAll(this.deathChests.stream().map(DeathChest::createSnapshot).collect(Collectors.toSet()));
        this.storage.save();
    }

    /**
     * Reloads the configuration file of the plugin
     */
    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        this.deathChestConfig = DeathChestConfig.load(getConfig());
    }

    /**
     * Checks if a chest can place on a certain position in the world.
     *
     * @param location the location where the chest should be placed.
     * @return true if the chest can be placed at the position
     */
    @Override
    public boolean canPlaceChest(@NotNull Location location) {
        return this.deathChests.stream().noneMatch(chest -> chest.getLocation().equals(location)) && !location.getBlock().getType().isSolid();
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull Location location, ItemStack @NotNull ... items) {
        return createDeathChest(location, null, items);
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull Location location, @Nullable OfflinePlayer player, ItemStack @NotNull ... items) {
        return createDeathChest(location, -1, player, items);
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull Location location, long expireAt, @Nullable OfflinePlayer player, ItemStack @NotNull ... items) {
        return createDeathChest(location, System.currentTimeMillis(), expireAt, player, items);
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull DeathChestSnapshot snapshot) {
        return createDeathChest(snapshot.getLocation(), snapshot.getCreatedAt(), snapshot.getExpireAt(), snapshot.getOwner(), snapshot.isProtected(), snapshot.getItems());
    }

    @Override
    public @NotNull DeathChest createDeathChest(@NotNull Location location, long createdAt, long expireAt, @Nullable OfflinePlayer player, boolean isProtected, ItemStack @NotNull ... items) {
        DeathChest build = DeathChestBuilder.builder()
                .setCreatedAt(createdAt)
                .setExpireAt(expireAt)
                .setPlayer(player)
                .setItems(items)
                .setProtected(isProtected)
                .setAnimationService(animationService)
                .setHologramService(hologramService)
                .setBreakEffectOptions(deathChestConfig.breakEffectOptions())
                .setHologramOptions(deathChestConfig.hologramOptions())
                .setParticleOptions(deathChestConfig.particleOptions())
                .build(location, deathChestConfig.inventoryOptions());
        this.deathChests.add(build);
        return build;
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

    public String getPrefix() {
        return "§cᴅᴇᴀᴛʜ ᴄʜᴇꜱᴛ §8︳ §r";
    }
}
