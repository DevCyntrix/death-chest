package de.helixdevs.deathchest;

import com.google.common.collect.Lists;
import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.DeathChestService;
import de.helixdevs.deathchest.api.DeathChestSnapshot;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import de.helixdevs.deathchest.command.DeathChestCommand;
import de.helixdevs.deathchest.config.ChestProtectionOptions;
import de.helixdevs.deathchest.config.DeathChestConfig;
import de.helixdevs.deathchest.listener.SpawnChestListener;
import de.helixdevs.deathchest.listener.UpdateNotificationListener;
import de.helixdevs.deathchest.util.Metrics;
import de.helixdevs.deathchest.util.UpdateChecker;
import de.helixdevs.deathchest.util.WorldGuardDeathChestFlag;
import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
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
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

/**
 * This plugin will create chests on death and will destroy them in after a specific time.
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
    private Permission permission;

    @Getter
    private String newerVersion;

    @Getter
    private static boolean placeholderAPIEnabled;

    private File savedChests;

    @Override
    public void onDisable() {
        // Save all chests
        try {
            saveChests();
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

        this.hologramService = SupportServices.getHologramService(this, this.deathChestConfig.preferredHologramService());
        this.animationService = SupportServices.getAnimationService(this, this.deathChestConfig.preferredAnimationService());
        this.protectionService = SupportServices.getProtectionService(this);
        // Standard protection service: No service
        if (protectionService == null)
            this.protectionService = (player, location, material) -> true;

        PluginManager pluginManager = getServer().getPluginManager();

        ChestProtectionOptions protectionOptions = getDeathChestConfig().chestProtectionOptions();
        if (protectionOptions.enabled()) {
            pluginManager.addPermission(new org.bukkit.permissions.Permission(protectionOptions.permission()));
            pluginManager.addPermission(new org.bukkit.permissions.Permission(protectionOptions.bypassPermission()));
        }

        pluginManager.registerEvents(new UpdateNotificationListener(this), this);
        pluginManager.registerEvents(new SpawnChestListener(this), this);
        //getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this);

        ServicesManager servicesManager = getServer().getServicesManager();
        if (pluginManager.isPluginEnabled("Vault")) {
            this.permission = servicesManager.load(Permission.class);
        }
        servicesManager.register(DeathChestService.class, this, this, ServicePriority.Normal);

        PluginCommand deathChestCommand = getCommand("deathchest");
        if (deathChestCommand != null) {
            DeathChestCommand command = new DeathChestCommand(this);
            deathChestCommand.setExecutor(command);
            deathChestCommand.setTabCompleter(command);
        }

        this.savedChests = new File(getDataFolder(), "saved-chests.yml");
        int size = loadChests().size();
        getLogger().info(size + " death chests loaded.");


        if (this.deathChestConfig.updateChecker()) {
            checkUpdates();
        }

        new Metrics(this, BSTATS_ID);
    }

    private void checkUpdates() {
        UpdateChecker checker = new UpdateChecker(this, RESOURCE_ID);
        checker.getVersion(version -> {
            if (getDescription().getVersion().equals(version))
                return;
            this.newerVersion = version;
            getLogger().warning("New version " + version + " is out. You are still running " + getDescription().getVersion());
            getLogger().warning("Update the plugin at https://www.spigotmc.org/resources/death-chest.101066/");
        });
    }

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

    @Override
    public void saveChests() throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        List<Map<String, Object>> collect = deathChests.stream()
                .map(DeathChest::createSnapshot)
                .map(DeathChestSnapshot::serialize)
                .collect((Supplier<List<Map<String, Object>>>) Lists::newArrayList, List::add, List::addAll);
        configuration.set("chests", collect);
        configuration.save(savedChests);
    }

    private Collection<DeathChest> loadChests() {
        if (!savedChests.isFile())
            return Collections.emptyList();

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(savedChests);
        List<?> chests = configuration.getList("chests");
        if (chests == null)
            return Collections.emptyList();

        List<DeathChest> list = new ArrayList<>(chests.size());

        for (Object chest : chests) {
            if (!(chest instanceof Map<?, ?> map))
                continue;

            DeathChestSnapshot deserialize = DeathChestSnapshotImpl.deserialize((Map<String, Object>) chest);
            if (deserialize == null)
                continue;

            list.add(deserialize.createChest(this));
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
    public @NotNull DeathChest createDeathChest(@NotNull DeathChestSnapshot snapshot) {
        return snapshot.createChest(this);
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
