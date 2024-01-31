package com.github.devcyntrix.deathchest;

import com.github.devcyntrix.api.event.InventoryChangeSlotItemListener;
import com.github.devcyntrix.deathchest.api.DeathChestService;
import com.github.devcyntrix.deathchest.api.animation.BreakAnimationService;
import com.github.devcyntrix.deathchest.api.audit.AuditManager;
import com.github.devcyntrix.deathchest.api.compatibility.CompatibilityLoader;
import com.github.devcyntrix.deathchest.api.compatibility.CompatibilityManager;
import com.github.devcyntrix.deathchest.api.protection.ProtectionService;
import com.github.devcyntrix.deathchest.api.report.ReportManager;
import com.github.devcyntrix.deathchest.api.storage.DeathChestStorage;
import com.github.devcyntrix.deathchest.audit.GsonAuditManager;
import com.github.devcyntrix.deathchest.blacklist.ItemBlacklist;
import com.github.devcyntrix.deathchest.blacklist.ItemBlacklistListener;
import com.github.devcyntrix.deathchest.command.CommandRegistry;
import com.github.devcyntrix.deathchest.config.*;
import com.github.devcyntrix.deathchest.controller.*;
import com.github.devcyntrix.deathchest.listener.*;
import com.github.devcyntrix.deathchest.report.GsonReportManager;
import com.github.devcyntrix.deathchest.support.lock.LWCCompatibility;
import com.github.devcyntrix.deathchest.support.lock.LocketteXCompatibility;
import com.github.devcyntrix.deathchest.support.placeholder.PlaceholderAPICompatibility;
import com.github.devcyntrix.deathchest.support.protection.WorldGuardDeathChestFlag;
import com.github.devcyntrix.deathchest.support.storage.MemoryStorage;
import com.github.devcyntrix.deathchest.support.storage.YamlStorage;
import com.github.devcyntrix.deathchest.util.adapter.DurationAdapter;
import com.github.devcyntrix.deathchest.view.chest.*;
import com.github.devcyntrix.deathchest.view.update.AdminJoinNotificationView;
import com.github.devcyntrix.deathchest.view.update.AdminNotificationView;
import com.github.devcyntrix.deathchest.view.update.ConsoleNotificationView;
import com.github.devcyntrix.hologram.api.HologramService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
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
import java.time.Duration;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

import static com.github.devcyntrix.deathchest.api.report.ReportManager.DATE_FORMAT_CONFIG;

/**
 * This plugin creates chests if a player dies and will destroy them after a specific time.
 * You can download this plugin on SpigotMC: <a href="https://www.spigotmc.org/resources/death-chest.101066/">https://www.spigotmc.org/resources/death-chest.101066/</a>
 * You are welcome to contribute to this plugin!
 */
@Getter
@Singleton
public class DeathChestPlugin extends JavaPlugin implements DeathChestService {

    public static final int RESOURCE_ID = 101066;
    public static final int BSTATS_ID = 14866;


    private DeathChestConfig deathChestConfig;

    private BreakAnimationService breakAnimationService;
    private ProtectionService protectionService;

    @Getter
    private static boolean placeholderAPIEnabled;

    private ReportManager reportManager;

    private AuditManager auditManager;

    private ItemBlacklist blacklist;

    @Getter
    private final Map<Player, DeathChestModel> lastDeathChests = new WeakHashMap<>();

    @Nullable
    private UpdateController updateController;

    private PlaceholderController placeHolderController;

    private HologramController hologramController;

    private DeathChestStorage deathChestStorage;
    private DeathChestController deathChestController;

    private LastSafeLocationController lastSafeLocationController;

    @Getter
    private BukkitAudiences audiences;

    private CompatibilityManager compatibilityManager;

    private final boolean test;

    public DeathChestPlugin(Boolean test, DeathChestConfig config) {
        this.test = test;
        this.deathChestConfig = config;
    }

    public DeathChestPlugin() {
        this.test = false;
    }

    /**
     * This method cleans the whole plugin up
     */
    @Override
    public void onDisable() {

        if (this.blacklist != null) {
            try {
                this.blacklist.save();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to save the item black list", e);
            }
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        try {
            ThiefProtectionOptions protectionOptions = getDeathChestConfig().chestOptions().thiefProtectionOptions();
            if (protectionOptions.enabled()) {
                debug(0, "Disabling chest protection...");
                debug(1, "Removing permissions...");
                if (pluginManager.getPermission(protectionOptions.permission()) != null) {
                    pluginManager.removePermission(protectionOptions.permission());
                    debug(2, "Permission \"%s\" removed.".formatted(protectionOptions.permission()));
                } else {
                    getLogger().warning("Expected configured permission but the permission \"%s\" wasn't registered.".formatted(protectionOptions.permission()));
                }
                if (pluginManager.getPermission(protectionOptions.bypassPermission()) != null) {
                    pluginManager.removePermission(protectionOptions.bypassPermission());
                    debug(2, "Permission \"%s\" removed.".formatted(protectionOptions.bypassPermission()));
                } else {
                    getLogger().warning("Expected configured permission but the permission \"%s\" wasn't registered.".formatted(protectionOptions.bypassPermission()));
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to remove the permission of the chest-protection", e);
        }

        if (this.updateController != null) {
            this.updateController.close();
            this.updateController = null;
        }

        ServicesManager servicesManager = getServer().getServicesManager();
        debug(0, "Removing death chest service...");
        servicesManager.unregisterAll(this);

        if (this.hologramController != null) {
            this.hologramController.close();
            this.hologramController = null;
        }

        if (this.deathChestController != null) {
            try {
                this.deathChestController.close();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to close the death chest controller", e);
            }
            this.deathChestController = null;
        }

        if (this.deathChestStorage != null) {
            try {
                this.deathChestStorage.close();
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Failed to close the death chest storage", e);
            }
            this.deathChestStorage = null;
        }

        if (this.auditManager != null) {
            try {
                auditManager.close();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to close the audit manager", e);
            }
            this.auditManager = null;
        }

        if (this.compatibilityManager != null) {
            this.compatibilityManager.disableCompatibilities();
        }

        if (this.audiences != null)
            this.audiences.close();
    }

    @Override
    public void onLoad() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = pluginManager.getPlugin("WorldGuard");
        if (plugin != null) {
            debug(0, "Registering death chest flag to WorldGuard...");
            WorldGuardDeathChestFlag.register();
        }

        ServicesManager servicesManager = getServer().getServicesManager();
        debug(0, "Registering death chest service...");
        servicesManager.register(DeathChestService.class, this, this, ServicePriority.Normal);
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        debug(0, "Loading configuration file...");
        if (!isTest())
            reloadConfig();


        initializeServices();

        debug(0, "Registering commands...");
        CommandRegistry.create(this).registerCommands(this);

        if (!test) {
            debug(0, "Starting metrics...");
            new Metrics(this, BSTATS_ID);
        }
    }

    private void initializeServices() {
        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.audiences = BukkitAudiences.create(this);

        debug(0, "Creating hologram controller...");
        this.hologramController = new HologramController(this);

        debug(0, "Selecting animation service...");
        this.breakAnimationService = SupportServices.getBlockBreakAnimationService(this, this.deathChestConfig.preferredBlockBreakAnimationService());
        debug(0, "Selecting protection services...");
        this.protectionService = SupportServices.getProtectionService(this);

        PluginManager pluginManager = getServer().getPluginManager();

        // Registers the protection permissions if they are not registered
        try {
            ThiefProtectionOptions protectionOptions = getDeathChestConfig().chestOptions().thiefProtectionOptions();
            if (protectionOptions.enabled()) {
                debug(0, "Configuring chest protection...");
                debug(1, "Registering permissions...");
                if (pluginManager.getPermission(protectionOptions.permission()) == null) {
                    pluginManager.addPermission(new org.bukkit.permissions.Permission(protectionOptions.permission()));
                    debug(2, "Permission \"%s\" registered.".formatted(protectionOptions.permission()));
                }
                if (pluginManager.getPermission(protectionOptions.bypassPermission()) == null) {
                    pluginManager.addPermission(new org.bukkit.permissions.Permission(protectionOptions.bypassPermission()));
                    debug(2, "Permission \"%s\" registered.".formatted(protectionOptions.bypassPermission()));
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to register the permission of the chest-protection", e);
        }
        this.blacklist = new ItemBlacklist(new File(getDataFolder(), "blacklist.yml"));


        this.reportManager = new GsonReportManager(new File(getDataFolder(), "reports"));
        debug(0, "Using gson report manager");
        this.auditManager = new GsonAuditManager(new File(getDataFolder(), "audits"));
        debug(0, "Using gson audit manager");

        debug(0, "Setting up the last safe location controller...");
        this.lastSafeLocationController = new LastSafeLocationController(this);
        try {
            this.placeHolderController = new PlaceholderController(getDeathChestConfig());

            debug(0, "Using death chest yaml storage");
            if (!test) {
                this.deathChestStorage = new YamlStorage(this.placeHolderController);
            } else {
                this.deathChestStorage = new MemoryStorage();
            }
            debug(0, "Initializing death chest storage...");
            this.deathChestStorage.init(this, deathChestStorage.getDefaultOptions());

            this.deathChestController = new DeathChestController(this, getLogger(), this.auditManager, this.deathChestStorage);

            BlockView adapter = new BlockView(this);
            this.deathChestController.registerAdapter(adapter);
            getServer().getPluginManager().registerEvents(adapter, this);

            this.deathChestController.registerAdapter(new CloseInventoryView(this));
            this.deathChestController.registerAdapter(new ExpirationView(this));


            HologramOptions hologramOptions = getDeathChestConfig().hologramOptions();
            if (hologramOptions.enabled()) {
                this.deathChestController.registerAdapter(new HologramView(this, hologramController, hologramOptions, placeHolderController));
            }

            BreakAnimationOptions breakAnimationOptions = getDeathChestConfig().breakAnimationOptions();
            if (breakAnimationOptions.enabled()) {
                this.deathChestController.registerAdapter(new BreakAnimationView(this, breakAnimationService, breakAnimationOptions));
            }

            ParticleOptions particleOptions = getDeathChestConfig().particleOptions();
            if (particleOptions.enabled()) {
                this.deathChestController.registerAdapter(new ParticleView(this, particleOptions));
            }

            debug(0, "Loading chests...");
            this.deathChestController.loadChests(); // Loads the chests to the cache
        } catch (IOException e) {
            getLogger().severe("Failed to initialize the storage. Please check your configuration file.");
            throw new RuntimeException(e);
        }

        CompatibilityLoader loader = new CompatibilityLoader();
        this.compatibilityManager = new CompatibilityManager(this, loader);
        this.compatibilityManager.registerCompatibility(LWCCompatibility.class);
        this.compatibilityManager.registerCompatibility(LocketteXCompatibility.class);
        this.compatibilityManager.registerCompatibility(PlaceholderAPICompatibility.class);
        this.compatibilityManager.enableCompatibilities();

        debug(0, "Registering event listeners...");
        pluginManager.registerEvents(new SpawnChestListener(this), this);
        pluginManager.registerEvents(new ChestInteractionListener(this), this);
        pluginManager.registerEvents(new ChestDestroyListener(this), this);
        pluginManager.registerEvents(new LastDeathChestListener(this), this);
        pluginManager.registerEvents(new WorldListener(this), this);
        pluginManager.registerEvents(new ItemBlacklistListener(blacklist), this);
        pluginManager.registerEvents(new InventoryChangeSlotItemListener(), this);
        pluginManager.registerEvents(new InventoryChangeSlotItemListener(blacklist), this);
        pluginManager.registerEvents(new PlayerNotificationListener(this), this);
        pluginManager.registerEvents(new GlobalNotificationListener(this), this);
        pluginManager.registerEvents(new LastSafeLocationListener(this), this);

        // Checks for updates
        if (this.deathChestConfig.updateChecker()) {
            debug(0, "Starting update checker...");
            this.updateController = new UpdateController(this);
            this.updateController.subscribe(new ConsoleNotificationView(this, getLogger()));
            this.updateController.subscribe(new AdminNotificationView(this));
            getServer().getPluginManager().registerEvents(new AdminJoinNotificationView(this, updateController), this);
        }
    }

    /**
     * Checks for the current config version to avoid conflicts if the user updates the plugin. The config will
     * recreate and the old config file will rename to config.yml.old.
     */
    private void checkConfigVersion() {

        // Recreate the config when the config version is too old.
        if (getConfig().getInt("config-version", 0) != DeathChestConfig.CONFIG_VERSION) {
            debug(1, "Found old configuration file.");
            File configFile = new File(getDataFolder(), "config.yml");
            if (configFile.isFile()) {
                File oldConfigFile = new File(getDataFolder(), "config.yml.old");
                boolean b = configFile.renameTo(oldConfigFile);
                debug(1, "Found old configuration file.");
                debug(1, "Renamed the config.yml -> config.yml.old");
                if (!b) {
                    throw new IllegalStateException("Failed to rename the configuration file to old.");
                }
            }
        }
    }

    @Override
    public boolean isDebugMode() {
        return deathChestConfig != null && deathChestConfig.debug() || Boolean.getBoolean("deathchest.debug");
    }

    @Override
    public @Nullable DeathChestModel getLastChest(@NotNull Player player) {
        return this.lastDeathChests.get(player);
    }

    /**
     * Creates snapshots of all current valid chests and hand over the snapshots to the storage system which saves them.
     */
    @Override
    public void saveChests() {
        this.deathChestController.saveChests();
    }

    /**
     * Reloads the configuration file of the plugin
     */
    public void reload() {
        onDisable();
        reloadConfig();
        initializeServices();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        debug(0, "Checking config version...");
        checkConfigVersion();
        saveDefaultConfig();
        debug(1, "Parsing configuration file...");
        this.deathChestConfig = DeathChestConfig.load(getConfig());
        if (isDebugMode()) {
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .setDateFormat(DATE_FORMAT_CONFIG, DATE_FORMAT_CONFIG)
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .create();
            debug(1, "Configuration: " + gson.toJson(this.deathChestConfig));
        }
    }

    /**
     * Checks if a chest can place on a certain position in the world.
     *
     * @param location the location where the chest should be placed.
     * @return true if the chest can be placed at the position
     */
    @Override
    public boolean canPlaceChestAt(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null)
            return false;
        if (location.getY() < world.getMinHeight())
            return false;
        if (location.getY() >= world.getMaxHeight())
            return false;

        return deathChestController.getChest(location) == null && !location.getBlock().getType().isSolid() && location.getBlock().getType() != Material.NETHER_PORTAL;
    }

    @Override
    public @NotNull DeathChestModel createDeathChest(@NotNull Location location, ItemStack @NotNull ... items) {
        return createDeathChest(location, null, items);
    }

    @Override
    public @NotNull DeathChestModel createDeathChest(@NotNull Location location, @Nullable Player player, ItemStack @NotNull ... items) {
        return createDeathChest(location, -1, player, items);
    }

    @Override
    public @NotNull DeathChestModel createDeathChest(@NotNull Location location, long expireAt, @Nullable Player player, ItemStack @NotNull ... items) {
        return createDeathChest(location, System.currentTimeMillis(), expireAt, player, items);
    }

    @Override
    public @NotNull DeathChestModel createDeathChest(@NotNull Location location, long createdAt, long expireAt, @Nullable Player player, boolean isProtected, ItemStack @NotNull ... items) {
        return this.deathChestController.createChest(location, createdAt, expireAt, player, isProtected, items);
    }

    @Override
    public @NotNull Stream<@NotNull DeathChestModel> getChests() {
        return this.deathChestController.getChests().stream();
    }

    @Override
    public @NotNull Stream<@NotNull DeathChestModel> getChests(@NotNull World world) {
        return this.deathChestController.getChests(world).stream();
    }

    @Override
    public HologramService getHologramService() {
        return hologramController;
    }

    @Override
    public @NotNull ProtectionService getProtectionService() {
        return protectionService;
    }

    public String getPrefix() {
        return "§cᴅᴇᴀᴛʜ ᴄʜᴇꜱᴛ §8︳ §r";
    }

    @NotNull
    @Override
    public File getFile() {
        return super.getFile();
    }
}
