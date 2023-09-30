package com.github.devcyntrix.deathchest.config;

import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public record DeathChestConfig(
        @SerializedName("config-version") int configVersion,
        @SerializedName("debug") boolean debug,
        @SerializedName("update-checker") boolean updateChecker,
        @SerializedName("duration-format") @NotNull String durationFormat,
        @SerializedName("expiration") @Nullable Duration expiration,
        @SerializedName("no-expiration-permission") @Nullable NoExpirationPermission noExpirationPermission,
        @SerializedName("drop-items-after-expiration") boolean dropItemsAfterExpiration,
        @SerializedName("convert-exp-to-bottles") boolean convertExpToBottles,
        @SerializedName("save-all-exp") boolean saveAllExp,
        @SerializedName("inventory") @NotNull InventoryOptions inventoryOptions,
        @SerializedName("hologram") @NotNull HologramOptions hologramOptions,
        @SerializedName("particle") @NotNull ParticleOptions particleOptions,
        @SerializedName("break-effect") @NotNull BreakAnimationOptions breakAnimationOptions,
        @SerializedName("player-notification") @NotNull PlayerNotificationOptions playerNotificationOptions,
        @SerializedName("global-notification") @NotNull GlobalNotificationOptions globalNotificationOptions,
        @SerializedName("change-death-message") @NotNull ChangeDeathMessageOptions changeDeathMessageOptions,
        @SerializedName("world-filter") @NotNull WorldFilterConfig worldFilterConfig,
        @SerializedName("blast-protection") boolean blastProtection,
        @SerializedName("chest-protection") @NotNull ChestProtectionOptions chestProtectionOptions,
        @SerializedName("preferred-animation-service") @Nullable String preferredBlockBreakAnimationService) {

    public static final int CONFIG_VERSION = 2;

    public static DeathChestConfig load(FileConfiguration config) {
        int configVersion = config.getInt("config-version");
        boolean debug = config.getBoolean("debug", false);
        boolean updateCheck = config.getBoolean("update-checker", true);
        String durationFormat = config.getString("duration-format", "mm:ss");

        Duration expiration = null;
        if (config.contains("expiration")) {
            long expirationInSeconds = config.getLong("expiration", 60 * 10);
            if (expirationInSeconds > 0) {
                expiration = Duration.ofSeconds(expirationInSeconds);
            }
        }

        NoExpirationPermission permission = null;
        if (config.isString("no-expiration-permission")) {
            permission = new NoExpirationPermission(false, config.getString("no-expiration-permission"));
        } else {
            if (config.contains("no-expiration-permission.enabled") && config.contains("no-expiration-permission.enabled")) {
                permission = new NoExpirationPermission(config.getBoolean("no-expiration-permission.enabled"), config.getString("no-expiration-permission.permission"));
            }
        }

        boolean dropItemsAfterExpiration = config.getBoolean("drop-items-after-expiration", false);
        boolean convertExpToBottles = config.getBoolean("convert-exp-to-bottles", false);
        boolean saveAllExp = config.getBoolean("save-all-exp", false);

        // Inventory
        InventoryOptions inventoryOptions = InventoryOptions.load(config.getConfigurationSection("inventory"));
        if (inventoryOptions == null) {
            throw new IllegalArgumentException("Missing inventory section in configuration file");
        }

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

        boolean blastProtection = config.getBoolean("blast-protection", false);

        ChestProtectionOptions chestProtectionOptions = ChestProtectionOptions.load(config.getConfigurationSection("chest-protection"));

        String preferredAnimationService = config.getString("preferred-animation-service");

        return new DeathChestConfig(configVersion, debug, updateCheck, durationFormat, expiration, permission, dropItemsAfterExpiration, convertExpToBottles, saveAllExp, inventoryOptions, hologramOptions, particleOptions, breakAnimationOptions, playerNotificationOptions, globalNotificationOptions, changeDeathMessageOptions, worldFilterConfig, blastProtection, chestProtectionOptions, preferredAnimationService);
    }

}
