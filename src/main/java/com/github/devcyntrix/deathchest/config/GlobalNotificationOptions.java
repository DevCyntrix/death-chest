package com.github.devcyntrix.deathchest.config;

import com.github.devcyntrix.deathchest.DeathChestModel;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.github.devcyntrix.deathchest.controller.PlaceholderController;
import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record GlobalNotificationOptions(
        @SerializedName("enabled") boolean enabled,
        @SerializedName("exclude-died-player") boolean excludeDiedPlayer,
        @SerializedName("message") String[] message) {

    public static @NotNull GlobalNotificationOptions load(@Nullable ConfigurationSection section) {
        if (section == null)
            return new GlobalNotificationOptions(false, false, null);

        boolean enabled = section.getBoolean("enabled", false);
        boolean excludeDiedPlayer = section.getBoolean("exclude-died-player", false);
        String message = section.getString("message");
        String[] coloredMessage = null;
        if (message != null) {
            TextComponent deserialize = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
            String serialize = MiniMessage.miniMessage().serialize(deserialize)
                    .replace("\\", ""); // Necessary to combine legacy color codes with mini message
            coloredMessage = serialize.split("\n");
        }

        return new GlobalNotificationOptions(enabled, excludeDiedPlayer, coloredMessage);
    }

    public void showNotification(DeathChestModel model, Player diedPlayer, PlaceholderController controller) {

        for (String message : message()) {
            message = controller.replace(model, message);
            Component deserialize = MiniMessage.miniMessage().deserialize(message);
            JavaPlugin.getPlugin(DeathChestPlugin.class).getAudiences().all().filterAudience(audience -> {
                Optional<UUID> uuid = audience.get(Identity.UUID);
                if (!excludeDiedPlayer)
                    return true;
                return !diedPlayer.getUniqueId().equals(uuid.orElse(null));
            }).sendMessage(deserialize);
        }

    }

}
