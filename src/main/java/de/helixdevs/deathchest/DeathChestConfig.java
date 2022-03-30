package de.helixdevs.deathchest;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;

public class DeathChestConfig {

    private final String durationFormat;
    private final Duration expiration;
    private final String inventoryTitle;
    private final String[] notificationMessage;

    public static DeathChestConfig load(FileConfiguration config) {
        String format = config.getString("format", "mm:ss");
        int expirationInSeconds = config.getInt("expiration", 60 * 10);

        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', config.getString("inventory-title", "Death Chest"));

        boolean notify = config.getBoolean("notify.enabled");
        String[] message = null;
        if (notify) {
            String string = config.getString("notify.message");
            if (string != null) {
                message = ChatColor.translateAlternateColorCodes('&', string).split("\n");
            }
        }

        return new DeathChestConfig(format, Duration.ofSeconds(expirationInSeconds), inventoryTitle, message);
    }

    public DeathChestConfig(String durationFormat, Duration expiration, String inventoryTitle, String[] notificationMessage) {
        this.durationFormat = durationFormat;
        this.expiration = expiration;
        this.inventoryTitle = inventoryTitle;
        this.notificationMessage = notificationMessage;
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

    public String[] getNotificationMessage() {
        return notificationMessage;
    }
}
