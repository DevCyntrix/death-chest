package de.helixdevs.deathchest.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class DeathChestConfig {

    private final boolean updateCheck;
    private final String durationFormat;
    private final Duration expiration;
    private final String inventoryTitle;
    private final boolean hologram;
    private final String[] notificationMessage;

    private final String preferredHologramService;
    private final String preferredAnimationService;

    public static DeathChestConfig load(FileConfiguration config) {
        boolean updateCheck = config.getBoolean("update-check", true);
        String format = config.getString("format", "mm:ss");
        int expirationInSeconds = config.getInt("expiration", 60 * 10);

        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', config.getString("inventory-title", "Death Chest"));

        boolean hologram = config.getBoolean("spawn-hologram", true);

        boolean notify = config.getBoolean("notify.enabled");
        String[] message = null;
        if (notify) {
            String string = config.getString("notify.message");
            if (string != null) {
                message = ChatColor.translateAlternateColorCodes('&', string).split("\n");
            }
        }

        String preferredHologramService = config.getString("preferred-hologram-service");
        String preferredAnimationService = config.getString("preferred-animation-service");

        return new DeathChestConfig(updateCheck, format, Duration.ofSeconds(expirationInSeconds), inventoryTitle, hologram, message, preferredHologramService, preferredAnimationService);
    }
}
