package de.helixdevs.deathchest;

import com.google.common.collect.Lists;
import de.helixdevs.deathchest.api.DeathChest;
import de.helixdevs.deathchest.api.DeathChestService;
import de.helixdevs.deathchest.api.animation.IAnimationService;
import de.helixdevs.deathchest.api.hologram.IHologramService;
import de.helixdevs.deathchest.api.protection.IProtectionService;
import de.helixdevs.deathchest.command.DeathChestCommand;
import de.helixdevs.deathchest.config.DeathChestConfig;
import de.helixdevs.deathchest.listener.SpawnChestListener;
import de.helixdevs.deathchest.listener.UpdateNotificationListener;
import de.helixdevs.deathchest.util.Metrics;
import de.helixdevs.deathchest.util.UpdateChecker;
import de.helixdevs.deathchest.util.WorldGuardDeathChestFlag;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
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

    private boolean placeholderAPIEnabled;

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

    @Override
    public void onLoad() {
        WorldGuardDeathChestFlag.register();
    }

    @Override
    public void onEnable() {
        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

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

        getServer().getPluginManager().registerEvents(new UpdateNotificationListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnChestListener(this), this);
        //getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this);

        getServer().getServicesManager().register(DeathChestService.class, this, this, ServicePriority.Normal);

        PluginCommand deathChestCommand = getCommand("deathchest");
        if (deathChestCommand != null) {
            DeathChestCommand command = new DeathChestCommand(this);
            deathChestCommand.setExecutor(command);
            deathChestCommand.setTabCompleter(command);
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


    private void saveChests(File file, Collection<DeathChest> chests) throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        List<Map<String, Object>> collect = chests.stream().map(DeathChest::serialize).collect((Supplier<List<Map<String, Object>>>) Lists::newArrayList, List::add, List::addAll);
        configuration.set("chests", collect);
        configuration.save(file);
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

            long createdAt = Long.parseLong(map.get("createdAt").toString());
            long expireAt = Long.parseLong(map.get("expireAt").toString());

            if (deathChestConfig.expiration() == null && expireAt != -1) // Renew the expiration when the configuration was changed.
                expireAt = -1;
            Duration expiration = deathChestConfig.expiration();
            if (expiration != null) {
                long newExpiration = createdAt + deathChestConfig.expiration().toMillis();
                if (newExpiration != expireAt) // Renew the expiration when the configuration value was changed.
                    expireAt = newExpiration;
            }

            if (expireAt != -1 && expireAt <= System.currentTimeMillis()) // Expire here
                continue;

            Location location = (Location) map.get("location");
            if (location == null)
                continue;

            String player = (String) map.get("player");
            UUID playerId = player == null ? null : UUID.fromString(player);

            List<ItemStack> stacks = (List<ItemStack>) map.get("items");
            if (stacks == null)
                continue;

            list.add(createDeathChest(location, createdAt, expireAt, playerId == null ? null : Bukkit.getOfflinePlayer(playerId), stacks.toArray(ItemStack[]::new)));
        }
        return list;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
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

    public String getNewerVersion() {
        return newerVersion;
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
