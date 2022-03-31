package de.helixdevs.deathchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;

public class DeathChestConfig {

    private final boolean updateCheck;
    private final String durationFormat;
    private final Duration expiration;
    private final String inventoryTitle;
    private final boolean hologram;
    private final String[] notificationMessage;

    public static DeathChestConfig load(FileConfiguration config) {
        boolean updateCheck = config.getBoolean("update-check", true);
        String format = config.getString("format", "mm:ss");
        int expirationInSeconds = config.getInt("expiration", 60 * 10);

        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', config.getString("inventory-title", "Death Chest"));

        boolean hologram = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays") && config.getBoolean("spawn-hologram", true);

        boolean notify = config.getBoolean("notify.enabled");
        String[] message = null;
        if (notify) {
            String string = config.getString("notify.message");
            if (string != null) {
                message = ChatColor.translateAlternateColorCodes('&', string).split("\n");
            }
        }

        return new DeathChestConfig(updateCheck, format, Duration.ofSeconds(expirationInSeconds), inventoryTitle, hologram, message);
    }

    public DeathChestConfig(boolean updateCheck, String durationFormat, Duration expiration, String inventoryTitle, boolean hologram, String[] notificationMessage) {
        this.updateCheck = updateCheck;
        this.durationFormat = durationFormat;
        this.expiration = expiration;
        this.inventoryTitle = inventoryTitle;
        this.hologram = hologram;
        this.notificationMessage = notificationMessage;
    }

    public boolean hasUpdateCheck() {
        return updateCheck;
    }

    public String getDurationFormat() {
        return durationFormat;
    }

    public Duration getExpiration() {
        return expiration;
    }

    public String getInventoryTitle() {
        return inventoryTitle;
    }

    public boolean hasHologram() {
        return hologram;
    }

    public String[] getNotificationMessage() {
        return notificationMessage;
    }
}
