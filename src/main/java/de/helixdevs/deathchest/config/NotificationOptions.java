package de.helixdevs.deathchest.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public record NotificationOptions(boolean enabled, String[] messages) {

    public static @Nullable NotificationOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return null;

        boolean enabled = section.getBoolean("enabled");
        String message = section.getString("message");
        String[] coloredMessage = null;
        if (message != null)
            coloredMessage = ChatColor.translateAlternateColorCodes('&', message).split("\n");

        return new NotificationOptions(enabled, coloredMessage);
    }

}
