package de.helixdevs.deathchest.config;

import de.helixdevs.deathchest.DeathChestPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlayerNotificationOptions(boolean enabled, String[] messages) {

    public static @NotNull PlayerNotificationOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return new PlayerNotificationOptions(false, null);

        boolean enabled = section.getBoolean("enabled", false);
        String message = section.getString("message");
        String[] coloredMessage = null;
        if (message != null)
            coloredMessage = ChatColor.translateAlternateColorCodes('&', message).split("\n");

        return new PlayerNotificationOptions(enabled, coloredMessage);
    }

    public void showNotification(Player player, StringSubstitutor substitutor) {
        for (String message : messages()) {
            message = substitutor.replace(message);

            if (DeathChestPlugin.isPlaceholderAPIEnabled())
                message = PlaceholderAPI.setPlaceholders(player, message);

            player.sendMessage(message);
        }
    }
}
