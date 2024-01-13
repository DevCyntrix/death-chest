package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DeathChestConfig(
        @SerializedName("config-version") int configVersion,
        @SerializedName("debug") boolean debug,
        @SerializedName("update-checker") boolean updateChecker,
        @SerializedName("auto-update") boolean autoUpdate,
        @SerializedName("duration-format") @NotNull String durationFormat,
        @SerializedName("chest") @NotNull ChestOptions chestOptions,
        @SerializedName("inventory") @NotNull InventoryOptions inventoryOptions,
        @SerializedName("hologram") @NotNull HologramOptions hologramOptions,
        @SerializedName("particle") @NotNull ParticleOptions particleOptions,
        @SerializedName("break-effect") @NotNull BreakAnimationOptions breakAnimationOptions,
        @SerializedName("player-notification") @NotNull PlayerNotificationOptions playerNotificationOptions,
        @SerializedName("global-notification") @NotNull GlobalNotificationOptions globalNotificationOptions,
        @SerializedName("change-death-message") @NotNull ChangeDeathMessageOptions changeDeathMessageOptions,
        @SerializedName("world-filter") @NotNull WorldFilterConfig worldFilterConfig,
        @SerializedName("world-chest-protection-filter") @NotNull WorldFilterConfig worldChestProtectionFilter,
        @SerializedName("world-alias") @NotNull WorldAliasConfig worldAlias,
        @SerializedName("preferred-animation-service") @Nullable String preferredBlockBreakAnimationService) {

    public static final int CONFIG_VERSION = 3;

    public static DeathChestConfig load(FileConfiguration config) {
        int configVersion = config.getInt("config-version");
        boolean debug = config.getBoolean("debug", false);
        boolean updateCheck = config.getBoolean("update-checker", true);
        boolean autoUpdate = config.getBoolean("auto-update", true);
        String durationFormat = config.getString("duration-format", "mm:ss");

        ChestOptions chestOptions = ChestOptions.load(config.getConfigurationSection("chest"));

        // Inventory
        InventoryOptions inventoryOptions = InventoryOptions.load(config.getConfigurationSection("inventory"));

        // Hologram
        HologramOptions hologramOptions = HologramOptions.load(config.getConfigurationSection("hologram"));

        // Particle
        ParticleOptions particleOptions = ParticleOptions.load(config.getConfigurationSection("particle"));

        // Effect
        BreakAnimationOptions breakAnimationOptions = BreakAnimationOptions.load(config.getConfigurationSection("break-effect"));

        // Notification
        PlayerNotificationOptions playerNotificationOptions = PlayerNotificationOptions.load(config.getConfigurationSection("player-notification"));
        GlobalNotificationOptions globalNotificationOptions = GlobalNotificationOptions.load(config.getConfigurationSection("global-notification"));

        ChangeDeathMessageOptions changeDeathMessageOptions = ChangeDeathMessageOptions.load(config.getConfigurationSection("change-death-message"));

        WorldFilterConfig worldFilterConfig = WorldFilterConfig.load(config.getConfigurationSection("world-filter"));
        WorldFilterConfig worldChestProtectionFilterConfig = WorldFilterConfig.load(config.getConfigurationSection("world-chest-protection-filter"));
        WorldAliasConfig worldAliasConfig = WorldAliasConfig.load(config.getConfigurationSection("world-alias"));

        String preferredAnimationService = config.getString("preferred-animation-service");

        return new DeathChestConfig(configVersion, debug, updateCheck, autoUpdate, durationFormat, chestOptions, inventoryOptions, hologramOptions, particleOptions, breakAnimationOptions, playerNotificationOptions, globalNotificationOptions, changeDeathMessageOptions, worldFilterConfig, worldChestProtectionFilterConfig, worldAliasConfig, preferredAnimationService);
    }

}
