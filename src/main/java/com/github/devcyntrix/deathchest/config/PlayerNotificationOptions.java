package com.github.devcyntrix.deathchest.config;

import com.github.devcyntrix.deathchest.DeathChestPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlayerNotificationOptions(boolean enabled, String[] messages) {

    public static @NotNull PlayerNotificationOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return new PlayerNotificationOptions(false, null);

        boolean enabled = section.getBoolean("enabled", false);
        String message = section.getString("message");
        String[] coloredMessage = null;
        if (message != null) {
            TextComponent deserialize = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
            String serialize = MiniMessage.miniMessage().serialize(deserialize)
                    .replace("\\", ""); // Necessary to combine legacy color codes with mini message
            coloredMessage = serialize.split("\n");
        }

        return new PlayerNotificationOptions(enabled, coloredMessage);
    }

    public void showNotification(Player player, StringSubstitutor substitutor) {
        for (String message : messages()) {
            message = substitutor.replace(message);

            if (DeathChestPlugin.isPlaceholderAPIEnabled())
                message = PlaceholderAPI.setPlaceholders(player, message);

            Component deserialize = MiniMessage.miniMessage().deserialize(message);
            JavaPlugin.getPlugin(DeathChestPlugin.class).getAudiences().player(player).sendMessage(deserialize);
        }
    }
}
