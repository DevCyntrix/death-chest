package de.helixdevs.deathchest.config;

import de.helixdevs.deathchest.DeathChestPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record GlobalNotificationOptions(boolean enabled, String[] messages) {

    public static @Nullable GlobalNotificationOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return null;

        boolean enabled = section.getBoolean("enabled", false);
        String message = section.getString("message");
        String[] coloredMessage = null;
        if (message != null)
            coloredMessage = ChatColor.translateAlternateColorCodes('&', message).split("\n");

        return new GlobalNotificationOptions(enabled, coloredMessage);
    }

    public void showNotification(Player diedPlayer, StringSubstitutor substitutor) {

        for (String message : messages()) {
            message = substitutor.replace(message);

            if (DeathChestPlugin.isPlaceholderAPIEnabled())
                message = PlaceholderAPI.setPlaceholders(diedPlayer, message);

            Bukkit.broadcastMessage(message);
        }

    }

}
